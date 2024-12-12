package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class PostgisRasterExportTest {
    private final GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void exportTiff() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/PostgisRasterTiffExport");
        String exportFileName = "export.tif";
        String targetFileName = "target.tif";
        
        // Delete existing file from previous test runs.
        File file = new File(projectDirectory, exportFileName);
        Files.deleteIfExists(file.toPath());

        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        long targetFileSize = new File(projectDirectory, targetFileName).length();
        long exportFileSize = new File(projectDirectory, exportFileName).length();

        assertEquals(targetFileSize, exportFileSize);
    }

    // At the moment this tests if GDAL drivers are enabled in PostGIS.
    @Test
    public void exportGeoTiff() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/PostgisRasterGeotiffExport");
        String exportFileName = "export.tif";
        String targetFileName = "target.tif";

        // Delete existing file from previous test runs.
        File file = new File(projectDirectory, exportFileName);
        Files.deleteIfExists(file.toPath());

        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        long targetFileSize = new File(projectDirectory, targetFileName).length();
        long exportFileSize = new File(projectDirectory, exportFileName).length();

        assertEquals(targetFileSize, exportFileSize);
    }
}
