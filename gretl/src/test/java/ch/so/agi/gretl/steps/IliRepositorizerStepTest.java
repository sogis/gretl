package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class IliRepositorizerStepTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;

    public IliRepositorizerStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    @Test
    public void createIliModelsXmlOk() throws Exception {
        File iliModelsFile = folder.newFile("ilimodels.xml");
        
        IliRepositorizerStep iliRepositorizer = new IliRepositorizerStep();
        iliRepositorizer.build(iliModelsFile.getAbsolutePath(), new File("src/test/data/IliRepositorizer/models/"));
        
        String expectedString = new String(Files.readAllBytes(Paths.get("src/test/data/IliRepositorizer/ilimodels.xml")), StandardCharsets.UTF_8);
        String resultString = new String(Files.readAllBytes(Paths.get(iliModelsFile.getAbsolutePath())), StandardCharsets.UTF_8);

        assertEquals("File content differs.", expectedString, resultString);        
    }
}
