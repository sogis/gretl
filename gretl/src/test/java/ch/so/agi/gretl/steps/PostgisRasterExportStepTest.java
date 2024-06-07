package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;

public class PostgisRasterExportStepTest {
    
    @ClassRule
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName(TestUtil.PG_DB_NAME)
            .withUsername(TestUtil.PG_DDLUSR_USR)
            .withInitScript(TestUtil.PG_INIT_SCRIPT_PATH)
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    private Connector connector;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void before() {
        this.connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
    }

    @After
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
        File outDirectory = folder.newFolder("build");
        File outFile = Paths.get(outDirectory.getAbsolutePath(), outFileName).toFile();

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
        File sqlFile = new File("src/test/resources/data/postgisrasterprocessor/prepare_raster_aaigrid.sql");
        String outFileName = "outfile.asc";

        // TODO this file is missing in the resources, what should be actually tested here?
        File targetFile = new File("src/test/resources/data/postgisrasterprocessor/target.asc");

        File outDirectory = folder.newFolder("build");
        //File outDirectory = Paths.get("/Users/stefan/tmp/").toFile();
        File outFile = Paths.get(outDirectory.getAbsolutePath(), outFileName).toFile();

        // Run: Calculate and export raster file from PostGIS
        PostgisRasterExportStep postgisRasterExportStep = new PostgisRasterExportStep();
        postgisRasterExportStep.execute(this.connector, sqlFile, outFile);
    }
}
