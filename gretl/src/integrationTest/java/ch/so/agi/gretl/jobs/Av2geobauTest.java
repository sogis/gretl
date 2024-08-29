package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Av2geobauTest {
    @Test
    public void simple() throws Exception {
        File resultFile = new File("src/integrationTest/jobs/Av2geobau/empty.dxf");
        resultFile.delete();
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Av2geobau", null);
        assertTrue(resultFile.exists());
    }

    @Test
    public void fileSet() throws Exception {
        File resultFile1 = new File("src/integrationTest/jobs/Av2geobauFileSet/empty1.dxf");
        resultFile1.delete();
        File resultFile2 = new File("src/integrationTest/jobs/Av2geobauFileSet/empty2.dxf");
        resultFile2.delete();
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Av2geobauFileSet", null);
        assertTrue(resultFile1.exists());
        assertTrue(resultFile2.exists());
    }
}
