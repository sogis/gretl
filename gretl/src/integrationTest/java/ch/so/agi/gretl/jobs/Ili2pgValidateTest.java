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

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class Ili2pgValidateTest {
    
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
        // Run task
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgValidate", gvs);

        // Check result
        String logFileContent = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/Ili2pgValidate/fubar.log")));
        assertTrue(logFileContent.contains("Info: ...validate done"));        
    }
    
    @Test
    public void validateData_Fail() throws Exception {
        // Run task
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
        assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgValidateFail", gvs, new StringBuffer(), new StringBuffer()));

        // Check result
        String logFileContent = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/Ili2pgValidateFail/fubar.log")));
        assertTrue(logFileContent.contains("Error: ...validate failed"));        
    }
}
