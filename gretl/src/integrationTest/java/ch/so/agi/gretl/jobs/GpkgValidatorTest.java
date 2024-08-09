package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.*;

public class GpkgValidatorTest {
    @Test
    public void validationOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgValidator");

        BuildResult result = IntegrationTestUtil.executeTestRunner(projectDirectory, "validate").build();

        TaskOutcome taskOutcome = Objects.requireNonNull(result.task(":validate")).getOutcome();
        assertTrue(taskOutcome == TaskOutcome.SUCCESS || taskOutcome == TaskOutcome.UP_TO_DATE);
    }
    @Test
    public void validationFail() {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgValidatorFail");

        Exception exception = assertThrows(Exception.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory, "validate").build();
        });

        assertTrue(exception.getMessage().contains("validation failed"));
    }

}
