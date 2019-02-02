package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import org.junit.Ignore;
import org.junit.Test;
import ch.so.agi.gretl.testutil.DbDriversReachableTest;
import org.junit.experimental.categories.Category;

public class DbDriverContainedTest {

    @Category(DbDriversReachableTest.class)
    @Test
    public void SqliteDriverContainedTest() throws Exception {
        IntegrationTestUtil.runJob("src/integrationTest/jobs/dbTasks_SqliteLibsPresent");
    }

    @Category(DbDriversReachableTest.class)
    @Test
    @Ignore
    public void OracleDriverContainedTest() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_ORA_CON_URI, IntegrationTestUtilSql.ORA_CON_URI)};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/dbTasks_OracleLibsPresent", gvs);
    }
}