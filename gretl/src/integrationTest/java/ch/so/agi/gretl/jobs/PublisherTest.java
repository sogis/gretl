package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PublisherTest {
    @Test
    public void simple() throws Exception {
        String jobDirectory = "src/integrationTest/jobs/Publisher";
        
        copyFileFromResourcesToJob(jobDirectory, "files", "av_test.itf");
        copyFileFromResourcesToJob(jobDirectory, "ili", "DM.01-AV-CH_LV95_24d_ili1.ili");
        
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob(jobDirectory, gvs);
    }

    @Test
    public void regions() throws Exception {
        String jobDirectory = "src/integrationTest/jobs/PublisherRegions";
        
        copyFileFromResourcesToJob(jobDirectory, "files", "av_test.itf");
        copyFileFromResourcesToJob(jobDirectory, "files", "2501.itf");
        copyFileFromResourcesToJob(jobDirectory, "files", "2502.itf");
        copyFileFromResourcesToJob(jobDirectory, "ili", "DM.01-AV-CH_LV95_24d_ili1.ili");

        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob(jobDirectory, gvs);
    }
    
    private Path copyFileFromResourcesToJob(String jobDirectory, String resourceSubDirectory, String filename) throws IOException {
        String resourceDirectory = "src/test/resources/data/publisher/";
        Path from = Paths.get(resourceDirectory, resourceSubDirectory, filename);
        Path to = Paths.get(jobDirectory, filename);
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        return to;
    }
}
