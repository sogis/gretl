package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GpkgValidatorTest {

    @Test
    public void validationOk() throws Exception {
        GradleVariable[] gvs = null; 
        IntegrationTestUtil.runJob("src/integrationTest/jobs/GpkgValidator", gvs);
    }

    @Test
    public void validationFail() throws Exception {
        GradleVariable[] gvs = null; // {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
        assertEquals(1,IntegrationTestUtil.runJob("src/integrationTest/jobs/GpkgValidatorFail", gvs,new StringBuffer(),new StringBuffer()));
    }
}
