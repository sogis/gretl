package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class IliValidatorTest {
    @Test
    public void validationOk() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/IliValidator", gvs);
    }
    @Test
    public void validationFileSetOk() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/IliValidatorFileSet", gvs);
    }

    @Test
    public void validationFail() throws Exception {
        GradleVariable[] gvs = null;
        assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/IliValidatorFail", gvs, new StringBuffer(), new StringBuffer()));
    }
    
    @Test
    public void validateOk_Ngk() throws Exception {
        // Run validation
        GradleVariable[] gvs = null;
        assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/IliValidatorNgk", gvs, new StringBuffer(), new StringBuffer()));
        
        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get("src/integrationTest/jobs/IliValidatorNgk", "ilivalidator.log"));
        String fileContent = new String (bytes);

        assertTrue(fileContent.contains("additional model SO_AFU_Naturgefahren_Validierung_20240515"));
        assertTrue(fileContent.contains("tid 701051de-6f2f-476d-81fb-43b8885ae7fc: Die Kennung des Auftrags im XTF muss dem Dateinamen des XTF entsprechen"));
        assertFalse(fileContent.contains("Info: assume unknown external objects"));

    }
}
