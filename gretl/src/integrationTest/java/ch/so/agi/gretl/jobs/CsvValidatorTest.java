package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.*;

public class CsvValidatorTest {
    @Test
    public void validationOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvValidator");

        BuildResult result = IntegrationTestUtil.getGradleRunner(projectDirectory, "validate").build();

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":validate")).getOutcome());
    }
    @Test
    public void validationFail() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvValidator");

        Exception exception = assertThrows(TaskExecutionException.class, () -> {
            IntegrationTestUtil.getGradleRunner(projectDirectory, "validate").build();
        });

        assertEquals("validation failed", exception.getMessage());
    }

}
