package ch.so.agi.gretl.jobs;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class IliRepositorizerTest {
    @Test
    public void exportOk() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/IliRepositorizer", gvs);
    }

}
