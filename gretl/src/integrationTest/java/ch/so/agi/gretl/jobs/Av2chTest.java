package ch.so.agi.gretl.jobs;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class Av2chTest {
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
