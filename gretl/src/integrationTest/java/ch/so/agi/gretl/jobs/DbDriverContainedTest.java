package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.ClassRule;
import org.junit.Test;
import ch.so.agi.gretl.testutil.DbDriversReachableTest;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.OracleContainer;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class DbDriverContainedTest {
    @ClassRule
    public static OracleContainer oracle =  new OracleContainer("epiclabs/docker-oracle-xe-11g")
        .withUsername("system").withPassword("oracle");

    @Category(DbDriversReachableTest.class)
    @Test
    public void SqliteDriverContainedTest() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/DbTasks_SqliteLibsPresent");

        IntegrationTestUtil.executeTestRunner(projectDirectory, "querySqliteMaster");
    }

    @Category(DbDriversReachableTest.class)
    @Test
    public void OracleDriverContainedTest() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/DbTasks_OracleLibsPresent");
        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_ORA_CON_URI, oracle.getJdbcUrl())};

        IntegrationTestUtil.executeTestRunner(projectDirectory, "queryOracleVersion", variables);
    }
}