package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import ch.so.agi.gretl.testutil.DbDriversReachableTest;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.OracleContainer;

public class DbDriverContainedTest {
    @ClassRule
    public static OracleContainer oracle =  new OracleContainer("epiclabs/docker-oracle-xe-11g")
        .withUsername("system").withPassword("oracle");

    @Category(DbDriversReachableTest.class)
    @Test
    public void SqliteDriverContainedTest() throws Exception {
        IntegrationTestUtil.runJob("src/integrationTest/jobs/DbTasks_SqliteLibsPresent");
    }

    @Category(DbDriversReachableTest.class)
    @Test
    public void OracleDriverContainedTest() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_ORA_CON_URI, oracle.getJdbcUrl())};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/DbTasks_OracleLibsPresent", gvs);
    }
}