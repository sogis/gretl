package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class IliValidatorTest {
    @Test
    public void validationOk() throws Exception {
        GradleVariable[] gvs = null; // {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
        TestUtil.runJob("jobs/iliValidator", gvs);
    }
    @Test
    public void validationFail() throws Exception {
        GradleVariable[] gvs = null; // {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
        assertEquals(1,TestUtil.runJob("jobs/iliValidatorFail", gvs,new StringBuffer(),new StringBuffer()));
    }

}
