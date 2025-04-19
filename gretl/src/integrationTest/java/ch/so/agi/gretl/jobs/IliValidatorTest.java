package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class IliValidatorTest {
    @Test
    public void validationOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidator");
        
        // Execute and check
        IntegrationTestUtil.executeTestRunner(projectDirectory);
        
        // Check
        String content = Files.readString(Paths.get(projectDirectory.getAbsolutePath(), "ilivalidator.log"));
        assertTrue(content.contains("Info: ...validation done"));
    }
    
    @Test
    public void validationFileSetOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorFileSet");
        
        // Execute and check
        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }

    @Test
    public void validationFail() {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorFail");

        // Execute and check
        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
    }
    
    @Test
    public void validateFail_Ngk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorNgk");

        // Execute and check
        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
        
        // Check result
        String fileContent = Files.readString(Paths.get("src/integrationTest/jobs/IliValidatorNgk", "ilivalidator.log"));

        assertTrue(fileContent.contains("additional model SO_AFU_Naturgefahren_Validierung_20240515"));
        assertTrue(fileContent.contains("tid 701051de-6f2f-476d-81fb-43b8885ae7fc: Die Kennung des Auftrags im XTF muss dem Dateinamen des XTF entsprechen"));
        assertFalse(fileContent.contains("Info: assume unknown external objects"));
    }
}
