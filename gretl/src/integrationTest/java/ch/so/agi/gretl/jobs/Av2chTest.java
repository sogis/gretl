package ch.so.agi.gretl.jobs;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;

public class Av2chTest {

    @Test
    public void transformation_Ok_with_test_kit() throws Exception{
        File projectDir = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2ch");
        File initScript = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/init.gradle");
        List<File> classpath = new ArrayList<>();
        File classpathFile = new File(System.getProperty("user.dir"),"build/pluginClassPath.txt");
        List<String> lines = Files.readAllLines(classpathFile.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            classpath.add(new File(line));
        }

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath(classpath)
            .withArguments("--init-script", initScript.getAbsolutePath(), "transform")
            .build();

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
    public void transformation_Ok() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Av2ch", gvs);
        
        File resultFile = new File("src/integrationTest/jobs/Av2ch/output/254900.itf");
        
        long resultSize = resultFile.length();
        assertTrue("Size of result file is wrong.", resultSize > 580000);

        String resultString = new String(Files.readAllBytes(resultFile.toPath()), StandardCharsets.ISO_8859_1);

        assertThat(resultString, containsString("DM01 Interlis Converter"));
        assertThat(resultString, containsString("MODL DM01AVCH24LV95D"));
        assertThat(resultString, containsString("TABL LFP3Nachfuehrung"));
        assertThat(resultString, containsString("OBJE 2540 2514 2611693.294 1233674.211 111.9 1 3 1"));
    }
    
    @Test 
    public void transformationFileSet_Ok() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Av2chFileSet", gvs);
        
        File resultFile1 = new File("src/integrationTest/jobs/Av2ch/output/254900.itf");
        File resultFile2 = new File("src/integrationTest/jobs/Av2ch/output/254900.itf");
        
        assertTrue(resultFile1.exists());
        assertTrue(resultFile2.exists());        
    }
    
    @Test
    public void transformation_Fail() throws Exception {
        GradleVariable[] gvs = null;        
        assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/Av2chFail", gvs, new StringBuffer(), new StringBuffer()));
    }
}
