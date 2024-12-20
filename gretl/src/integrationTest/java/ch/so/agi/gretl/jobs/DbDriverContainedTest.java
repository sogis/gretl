package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

//@Disabled
@Testcontainers
public class DbDriverContainedTest {

    @Container
    public static OracleContainer oracle =  new OracleContainer("epiclabs/docker-oracle-xe-11g")
        .withUsername("system").withPassword("oracle");

    @Test
    @Tag(TestTags.DB_DRIVERS_REACHABLE_TEST)
    public void SqliteDriverContainedTest() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/DbTasks_SqliteLibsPresent");
        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }

    @Test
    @Tag(TestTags.DB_DRIVERS_REACHABLE_TEST)
    public void OracleDriverContainedTest() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/DbTasks_OracleLibsPresent");
        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_ORA_CON_URI, oracle.getJdbcUrl())};

        IntegrationTestUtil.executeTestRunner(projectDirectory, variables);
    }
}