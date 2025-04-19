package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GpkgValidatorTest {
    @Test
    public void validationOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgValidator");

        // Execute and check
        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }
    @Test
    public void validationFail() throws IOException {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgValidatorFail");

        // Execute and check
        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
        
        // Check result
        String fileContent = Files.readString(Paths.get("src/integrationTest/jobs/GpkgValidatorFail", "gpkgvalidator.log"));

        assertTrue(fileContent.contains("GpkgModel.Topic1.Attributes: tid o2: value 0 is out of range in attribute art"));
        assertTrue(fileContent.contains("GpkgModel.Topic1.Attributes: tid o2: value <true> is not a number"));
    }
}
