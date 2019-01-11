package so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSql;
import org.junit.Test;
import ch.so.agi.gretl.testutil.DbDriversReachableTest;
import org.junit.experimental.categories.Category;

public class DbDriverContainedTest {

    @Category(DbDriversReachableTest.class)
    @Test
    public void SqliteDriverContainedTest() throws Exception {
        TestUtil.runJob("jobs/dbTasks_SqliteLibsPresent");
    }

    @Category(DbDriversReachableTest.class)
    @Test
    public void OracleDriverContainedTest() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_ORA_CON_URI, TestUtilSql.ORA_CON_URI)};
        TestUtil.runJob("jobs/dbTasks_OracleLibsPresent", gvs);
    }
}