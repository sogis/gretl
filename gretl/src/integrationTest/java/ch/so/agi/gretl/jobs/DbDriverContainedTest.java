package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.OracleContainer;

public class DbDriverContainedTest {
    @ClassRule
    public static OracleContainer oracle =  new OracleContainer("epiclabs/docker-oracle-xe-11g")
        .withUsername("system").withPassword("oracle");

    @Test
    @Tag(TestTags.DB_DRIVERS_REACHABLE_TEST)
    public void SqliteDriverContainedTest() throws Exception {
        IntegrationTestUtil.runJob("src/integrationTest/jobs/DbTasks_SqliteLibsPresent");
    }

    @Test
    @Tag(TestTags.DB_DRIVERS_REACHABLE_TEST)
    public void OracleDriverContainedTest() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_ORA_CON_URI, oracle.getJdbcUrl())};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/DbTasks_OracleLibsPresent", gvs);
    }
}