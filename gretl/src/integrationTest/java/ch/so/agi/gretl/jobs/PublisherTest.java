package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class PublisherTest {
    @Test
    public void simple() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Publisher");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "files", "av_test.itf");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "ili", "DM.01-AV-CH_LV95_24d_ili1.ili");
        
        BuildResult result = IntegrationTestUtil.getGradleRunner(projectDirectory, "publishFile").build();

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":publishFile")).getOutcome());
    }

    @Test
    public void regions() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/PublisherRegions");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "files", "av_test.itf");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "files", "2501.itf");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "files", "2502.itf");
        copyFileFromResourcesToJob(projectDirectory.getPath(), "ili", "DM.01-AV-CH_LV95_24d_ili1.ili");

        BuildResult result = IntegrationTestUtil.getGradleRunner(projectDirectory, "printPublishedRegions").build();

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":printPublishedRegions")).getOutcome());
    }
    
    private void copyFileFromResourcesToJob(String jobDirectory, String resourceSubDirectory, String filename) throws IOException {
        String resourceDirectory = "src/test/resources/data/publisher/";
        Path from = Paths.get(resourceDirectory, resourceSubDirectory, filename);
        Path to = Paths.get(jobDirectory, filename);
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

}
