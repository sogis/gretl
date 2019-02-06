package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class IliRepositorizerTest {
    @Test
    public void exportIliModelsXmlOk() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/IliRepositorizer", gvs);
        
        String expectedString = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/IliRepositorizer/expected_ilimodels.xml")), StandardCharsets.UTF_8);
        String resultString = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/IliRepositorizer/ilimodels.xml")), StandardCharsets.UTF_8);

        assertEquals("File content differs.", expectedString, resultString);
    }
}
