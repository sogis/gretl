package ch.so.agi.gretl.jobs;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

import ch.so.agi.gretl.util.IntegrationTestUtil;

import static org.junit.Assert.*;

public class Av2chTest {
    @Test
    public void transformation_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2ch");
        IntegrationTestUtil.executeTestRunner(projectDirectory, "transform");

        File resultFile = new File(projectDirectory + "/output/254900.itf");

        String resultString = new String(Files.readAllBytes(resultFile.toPath()), StandardCharsets.ISO_8859_1);
        long resultSize = resultFile.length();

        assertTrue("Size of result file is wrong.", resultSize > 580000);
        assertTrue(resultString.contains("DM01 Interlis Converter"));
        assertTrue(resultString.contains("MODL DM01AVCH24LV95D"));
        assertTrue(resultString.contains("TABL LFP3Nachfuehrung"));
        assertTrue(resultString.contains("OBJE 2540 2514 2611693.294 1233674.211 111.9 1 3 1"));
    }
    
    @Test 
    public void transformationFileSet_Ok() throws Exception {

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2chFileSet");

        IntegrationTestUtil.executeTestRunner(projectDirectory, "transform");

        File resultFile1 = new File("src/integrationTest/jobs/Av2chFileSet/output/252400.itf");
        File resultFile2 = new File("src/integrationTest/jobs/Av2chFileSet/output/254900.itf");
        
        assertTrue(resultFile1.exists());
        assertTrue(resultFile2.exists());        
    }
    
    @Test
    public void transformation_Fail() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2chFail");

        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory, "transform");
        });
    }
}
