package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShpValidatorTest {
    @Test
    public void validationOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/ShpValidator");

        // Execute and check
        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }

    @Test
    public void validationFail() throws IOException {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/ShpValidatorFail");

        // Execute and check
        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
        
        // Check result
        String fileContent = Files.readString(Paths.get("src/integrationTest/jobs/ShpValidatorFail", "shpvalidator.log"));
        assertTrue(fileContent.contains("ShpModel.Topic12.Class1: tid o2: value rot is not a member of the enumeration in attribute aenum"));
        assertTrue(fileContent.contains("ShpModel.Topic12.Class1: tid o3: Attribute aint requires a value"));
        assertTrue(fileContent.contains("ShpModel.Topic12.Class1: tid o3: Attribute adec requires a value"));
        assertTrue(fileContent.contains("ShpModel.Topic12.Class1: tid o3: Attribute atext requires a value"));
        assertTrue(fileContent.contains("ShpModel.Topic12.Class1: tid o3: Attribute aenum requires a value"));
        assertTrue(fileContent.contains("ShpModel.Topic12.Class1: tid o3: Attribute adate requires a value"));
        assertTrue(fileContent.contains("ShpModel.Topic12.Class1: tid o3: Attribute the_geom requires a value"));
        assertTrue(fileContent.contains("...validation failed"));
    }
}
