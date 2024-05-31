package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;

public class PostgisRasterExportStepTest {
    private GretlLogger log;
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(TestUtil.PG_DDLUSR_USR)
        .withInitScript("data/sql/init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    public PostgisRasterExportStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void export_geotiff_Ok() throws Exception {
        // Prepare
        Connector sourceDb = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
        File sqlFile = new File("src/test/resources/data/postgisrasterprocessor/prepare_raster_geotiff.sql");
        String outFileName = "outfile.tif";
        File targetFile = new File("src/test/resources/data/postgisrasterprocessor/target.tif");

        File outDirectory = folder.newFolder("build");
        //File outDirectory = Paths.get("/Users/stefan/tmp/").toFile();
        File outFile = Paths.get(outDirectory.getAbsolutePath(), outFileName).toFile();

        // Run: Calculate and export raster file from PostGIS
        PostgisRasterExportStep postgisRasterExportStep = new PostgisRasterExportStep();
        postgisRasterExportStep.execute(sourceDb, sqlFile, outFile);
                
        // Check result
        long targetFileSize = targetFile.length();
        long outFileSize = outFile.length();
        assertEquals(targetFileSize, outFileSize);
    }       
    
    @Test
    public void export_aaigrid_Ok() throws Exception {
        // Prepare
        Connector sourceDb = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
        File sqlFile = new File("src/test/resources/data/postgisrasterprocessor/prepare_raster_aaigrid.sql");
        String outFileName = "outfile.asc";
        File targetFile = new File("src/test/resources/data/postgisrasterprocessor/target.asc");

        File outDirectory = folder.newFolder("build");
        //File outDirectory = Paths.get("/Users/stefan/tmp/").toFile();
        File outFile = Paths.get(outDirectory.getAbsolutePath(), outFileName).toFile();

        // Run: Calculate and export raster file from PostGIS
        PostgisRasterExportStep postgisRasterExportStep = new PostgisRasterExportStep();
        postgisRasterExportStep.execute(sourceDb, sqlFile, outFile);
    }
}
