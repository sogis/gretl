package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;

public class Av2geobauTest {
    @Test
    public void simple() throws Exception {
        File resultFile=new File("src/integrationTest/jobs/Av2geobau/empty.dxf");
        resultFile.delete();
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Av2geobau", gvs);
        assertTrue(resultFile.exists());
    }
    @Test
    public void fileSet() throws Exception {
        File resultFile1=new File("src/integrationTest/jobs/Av2geobauFileSet/empty1.dxf");
        resultFile1.delete();
        File resultFile2=new File("src/integrationTest/jobs/Av2geobauFileSet/empty2.dxf");
        resultFile2.delete();
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Av2geobauFileSet", gvs);
        assertTrue(resultFile1.exists());
        assertTrue(resultFile2.exists());
    }

}
