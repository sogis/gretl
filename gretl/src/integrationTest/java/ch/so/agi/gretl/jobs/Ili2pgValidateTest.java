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
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class Ili2pgValidateTest {
    private final GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void validateData_Ok() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgValidate");
        
        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check result
        String logFileContent = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/Ili2pgValidate/fubar.log")));
        assertTrue(logFileContent.contains("Info: ...validate done"));        
    }
    
    @Test
    public void validateData_Fail() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgValidateFail");

        // Execute test
        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);
        });

        // Check result
        String logFileContent = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/Ili2pgValidateFail/fubar.log")));
        assertTrue(logFileContent.contains("Error: ...validate failed"));        
    }
}
