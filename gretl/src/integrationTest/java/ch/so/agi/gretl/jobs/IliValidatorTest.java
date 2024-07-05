package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.*;

public class IliValidatorTest {
    @Test
    public void validationOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidator");
        BuildResult result = IntegrationTestUtil.getGradleRunner(projectDirectory, "validate").build();

        TaskOutcome taskOutcome = Objects.requireNonNull(result.task(":validate")).getOutcome();
        assertTrue(taskOutcome == TaskOutcome.SUCCESS || taskOutcome == TaskOutcome.UP_TO_DATE);
    }
    @Test
    public void validationFileSetOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorFileSet");
        BuildResult result = IntegrationTestUtil.getGradleRunner(projectDirectory, "validate").build();

        TaskOutcome taskOutcome = Objects.requireNonNull(result.task(":validate")).getOutcome();
        assertTrue(taskOutcome == TaskOutcome.SUCCESS || taskOutcome == TaskOutcome.UP_TO_DATE);
    }

    @Test
    public void validationFail() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorFail");

        Exception exception = assertThrows(Exception.class, () -> {
            IntegrationTestUtil.getGradleRunner(projectDirectory, "validate").build();
        });

        assertTrue(exception.getMessage().contains("validation failed"));
    }
}
