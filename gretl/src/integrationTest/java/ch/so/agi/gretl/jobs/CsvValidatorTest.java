package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
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

        IntegrationTestUtil.executeTestRunner(projectDirectory, "validate");
    }
    @Test
    public void validationFail() {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvValidatorFail");

        assertThrows(Exception.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory, "validate");
        });
    }

}
