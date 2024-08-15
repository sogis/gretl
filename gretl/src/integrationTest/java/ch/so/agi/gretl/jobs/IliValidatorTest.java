package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IliValidatorTest {

    @Test
    public void validationOk() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/IliValidator", gvs);
    }

    @Test
    public void validationFileSetOk() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/IliValidatorFileSet", gvs);
    }

    @Test
    public void validationFail() throws Exception {
        GradleVariable[] gvs = null;
        assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/IliValidatorFail", gvs, new StringBuffer(), new StringBuffer()));
    }
}
