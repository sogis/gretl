package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PublisherTest {
    @Test
    public void simple() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Publisher");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "files", "av_test.itf");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "ili", "DM.01-AV-CH_LV95_24d_ili1.ili");

        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }

    @Test
    public void regions() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/PublisherRegions");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "files", "av_test.itf");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "files", "2501.itf");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "files", "2502.itf");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "ili", "DM.01-AV-CH_LV95_24d_ili1.ili");

        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }

    private void copyFileFromResourcesToJob(String jobDirectory, String resourceSubDirectory, String filename) throws IOException {
        String resourceDirectory = "src/test/resources/data/publisher/";
        Path from = Paths.get(resourceDirectory, resourceSubDirectory, filename);
        Path to = Paths.get(jobDirectory, filename);
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }
}
