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
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidator");
        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }
    @Test
    public void validationFileSetOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorFileSet");
        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }

    @Test
    public void validationFail() {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorFail");

        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
    }
    
    @Test
    public void validateFail_Ngk() throws Exception {
        // Run validation
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorNgk");

        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
        
        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get("src/integrationTest/jobs/IliValidatorNgk", "ilivalidator.log"));
        String fileContent = new String (bytes);

        assertTrue(fileContent.contains("additional model SO_AFU_Naturgefahren_Validierung_20240515"));
        assertTrue(fileContent.contains("tid 701051de-6f2f-476d-81fb-43b8885ae7fc: Die Kennung des Auftrags im XTF muss dem Dateinamen des XTF entsprechen"));
        assertFalse(fileContent.contains("Info: assume unknown external objects"));
    }

}
