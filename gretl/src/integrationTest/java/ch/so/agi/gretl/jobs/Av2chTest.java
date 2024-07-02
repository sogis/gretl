package ch.so.agi.gretl.jobs;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.Test;

import ch.so.agi.gretl.util.IntegrationTestUtil;

import org.gradle.testkit.runner.GradleRunner;

import static org.junit.Assert.*;

public class Av2chTest {
    @Test
    public void transformation_Ok() throws Exception {
        File projectDir = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2ch");
        IntegrationTestUtil.getGradleRunner(projectDir, "transform").build();

        File resultFile = new File(projectDir + "/output/254900.itf");

        long resultSize = resultFile.length();
        assertTrue("Size of result file is wrong.", resultSize > 580000);

        String resultString = new String(Files.readAllBytes(resultFile.toPath()), StandardCharsets.ISO_8859_1);

        assertTrue(resultString.contains("DM01 Interlis Converter"));
        assertTrue(resultString.contains("MODL DM01AVCH24LV95D"));
        assertTrue(resultString.contains("TABL LFP3Nachfuehrung"));
        assertTrue(resultString.contains("OBJE 2540 2514 2611693.294 1233674.211 111.9 1 3 1"));
    }
    
    @Test 
    public void transformationFileSet_Ok() throws Exception {

        File projectDir = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2chFileSet");

        try{
            IntegrationTestUtil.getGradleRunner(projectDir, "transform").build();
        } catch (UnexpectedBuildFailure e) {
            System.out.println("Build failed with message: " + e.getMessage());
        }

        File resultFile1 = new File("src/integrationTest/jobs/Av2chFileSet/output/252400.itf");
        File resultFile2 = new File("src/integrationTest/jobs/Av2chFileSet/output/254900.itf");
        
        assertTrue(resultFile1.exists());
        assertTrue(resultFile2.exists());        
    }
    
    @Test
    public void transformation_Fail() throws Exception {
        File projectDir = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2chFail");
        try {
            IntegrationTestUtil.getGradleRunner(projectDir, "transform").build();
            fail("Expected an UnexpectedBuildFailure exception to be thrown");
        } catch (UnexpectedBuildFailure e) {
            System.out.println("Build failed with message: " + e.getMessage());
        }
    }
}
