package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Av2geobauTest {
    @Test
    public void simple() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2geobau");
        cleanUpFiles(projectDirectory);
        IntegrationTestUtil.executeTestRunner(projectDirectory, "av2geobau");

        File resultFile = new File(projectDirectory + "/empty.dxf");
        assertTrue(resultFile.exists());
    }
    @Test
    public void fileSet() throws Exception {
        File projectDir = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Av2geobauFileSet");
        cleanUpFiles(projectDir);

        IntegrationTestUtil.executeTestRunner(projectDir, "av2geobau");

        File resultFile1 = new File(projectDir + "/empty1.dxf");
        File resultFile2 = new File(projectDir + "/empty2.dxf");

        assertTrue(resultFile1.exists());
        assertTrue(resultFile2.exists());
    }

    private void cleanUpFiles(File projectDirectory){
        File[] files = projectDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".dxf")) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        System.out.println("Failed to delete file: " + file.getName());
                    }
                }
            }
        }
    }
}
