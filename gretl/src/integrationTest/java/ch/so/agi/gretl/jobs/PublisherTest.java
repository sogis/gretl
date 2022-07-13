package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PublisherTest {
    @Test
    public void simple() throws Exception {
        String jobDirectory = "src/integrationTest/jobs/Publisher";
        
        copyFileFromResourcesToJob(jobDirectory, "av_test.itf");
        copyFileFromResourcesToJob(jobDirectory, "DM.01-AV-CH_LV95_24d_ili1.ili");
        
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob(jobDirectory, gvs);
    }
    @Test
    public void regions() throws Exception {
        String jobDirectory = "src/integrationTest/jobs/PublisherRegions";
        
        copyFileFromResourcesToJob(jobDirectory, "av_test.itf");
        copyFileFromResourcesToJob(jobDirectory, "2501.itf");
        copyFileFromResourcesToJob(jobDirectory, "2502.itf");
        copyFileFromResourcesToJob(jobDirectory, "DM.01-AV-CH_LV95_24d_ili1.ili");

        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob(jobDirectory, gvs);
    }
    
    private Path copyFileFromResourcesToJob(String jobDirectory, String filename) throws IOException {
        String resourceDirectory = "src/test/resources/data/publisher/";
        Path from = Paths.get(resourceDirectory, filename);
        Path to = Paths.get(jobDirectory, filename);
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        return to;
    }

}
