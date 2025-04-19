package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsvValidatorTest {
    @Test
    public void validationOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvValidator");

        // Execute and check
        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }
    @Test
    public void validationFail() throws IOException {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvValidatorFail");

        // Execute and check
        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
        
        // Check result
        String fileContent = Files.readString(Paths.get("src/integrationTest/jobs/CsvValidatorFail", "csvvalidator.log"));

        assertTrue(fileContent.contains("CsvModel.Topic12.Class1: tid o2: value <x> is not a number"));
        assertTrue(fileContent.contains("CsvModel.Topic12.Class1: tid o2: value gruen is not a member of the enumeration in attribute attr3"));
    }
}
