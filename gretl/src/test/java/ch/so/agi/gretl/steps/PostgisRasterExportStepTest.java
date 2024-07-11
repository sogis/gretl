package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class PostgisRasterExportStepTest {
    
    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName(TestUtil.PG_DB_NAME)
            .withUsername(TestUtil.PG_DDLUSR_USR)
            .withInitScript(TestUtil.PG_INIT_SCRIPT_PATH)
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    private Connector connector;

    @TempDir
    public Path folder;

    @BeforeEach
    public void before() {
        this.connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
    }

    @AfterEach
    public void after() throws Exception {
        if (!this.connector.isClosed()) {
            this.connector.close();
        }
    }
    
    @Test
    public void export_geotiff_Ok() throws Exception {
        // Prepare
        File sqlFile = TestUtil.getResourceFile(TestUtil.RASTER_GEOTIFF_SQL_PATH);
        String outFileName = "outfile.tif";
        File targetFile = TestUtil.getResourceFile(TestUtil.TARGET_TIF_PATH);
        Path outDirectory = TestUtil.createTempDir(folder, "build");
        File outFile = Paths.get(outDirectory.toAbsolutePath().toString(), outFileName).toFile();

        // Run: Calculate and export raster file from PostGIS
        PostgisRasterExportStep postgisRasterExportStep = new PostgisRasterExportStep();
        postgisRasterExportStep.execute(this.connector, sqlFile, outFile);
                
        // Check result
        long targetFileSize = targetFile.length();
        long outFileSize = outFile.length();
        assertEquals(targetFileSize, outFileSize);
    }

    @Test
    public void export_aaigrid_Ok() throws Exception {
        // Prepare
        File sqlFile = TestUtil.getResourceFile("data/postgisrasterprocessor/prepare_raster_aaigrid.sql");
        String outFileName = "outfile.asc";

        // TODO this file is missing in the resources, what should be actually tested here?
        File targetFile = new File("src/test/resources/data/postgisrasterprocessor/target.asc");

        Path outDirectory = TestUtil.createTempDir(folder, "build");
        File outFile = Paths.get(outDirectory.toAbsolutePath().toString(), outFileName).toFile();

        // Run: Calculate and export raster file from PostGIS
        PostgisRasterExportStep postgisRasterExportStep = new PostgisRasterExportStep();
        postgisRasterExportStep.execute(this.connector, sqlFile, outFile);
    }
}
