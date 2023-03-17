package ch.so.agi.gretl.jobs;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class MetaPublisherTest {
    @Test
    public void simple() throws Exception {
        String jobDirectory = "src/integrationTest/jobs/MetaPublisher/ch.so.afu.abbaustellen/gretl/afu_abbaustellen_pub";
                
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob(jobDirectory, gvs);
    }

}
