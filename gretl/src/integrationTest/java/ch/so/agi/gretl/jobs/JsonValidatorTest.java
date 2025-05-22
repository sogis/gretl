package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonValidatorTest {
    @Test
    public void validationOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/JsonValidatorOk");

        // Execute and check
        IntegrationTestUtil.executeTestRunner(projectDirectory);
    }
    
    @Test
    public void validationFail() throws IOException {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/JsonValidatorFail");

        // Execute and check
        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory);
        });
        
        // Check
        String content = Files.readString(Paths.get(projectDirectory.getAbsolutePath(), "error.log"));
        assertTrue(content.contains("line 2: Test2.Topic2.ClassA: tid o1: Attribute attrStruct[0]/attrText is length restricted to 3"));
        assertTrue(content.contains("line 2: Test2.Topic2.ClassA: tid o1: Attribute attrBag[1]/attrText is length restricted to 3"));
        assertTrue(content.contains("line 2: Test2.Topic2.ClassA: tid o1: Attribute attrTextList is length restricted to 5"));
        assertTrue(content.contains("line 21: Test2.Topic2.ClassA: tid o2: Attribute attrStruct[0]/attrText is length restricted to 3"));
        assertTrue(content.contains("line 21: Test2.Topic2.ClassA: tid o2: Attribute attrBag[1]/attrText is length restricted to 3"));
        assertTrue(content.contains("line 21: Test2.Topic2.ClassA: tid o2: Attribute attrTextList is length restricted to 5"));
        assertTrue(content.contains("Test2.Topic2.ClassA: tid o1: Overlay coord1 (2460000.500, 1045000.000), coord2 (2460001.000, 1045000.000), tids o1, o2"));
        assertTrue(content.contains("Test2.Topic2.ClassA: tid o1: Intersection coord1 (2460000.500, 1045000.000), tids o1, o2"));
        assertTrue(content.contains("Test2.Topic2.ClassA: tid o1: Intersection coord1 (2460001.000, 1045000.000), tids o1, o2"));        
        assertTrue(content.contains("failed to validate AREA Test2.Topic2.ClassA.attrSurface"));
    }
}
