package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShpValidatorTest {
    @Test
    public void validationOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/ShpValidator");

        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }

    @Test
    public void validationFail() {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/ShpValidatorFail");

        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
    }
}
