package ch.so.agi.gretl.jobs;


import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GzipTest {

    @Test
    public void compress_file_Ok() throws Exception {        
        // Run GRETL task
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Gzip", gvs);
                
        // Validate result
        File gzipFile = new File("src/integrationTest/jobs/Gzip/planregister.xml.gz");
        String fileContent = contentGzipFile(gzipFile);
        
        assertTrue(fileContent.contains("ili2pg-4.9.0-eb3a0d51869bd2adeeb51fe7aba4b526fe002c1a"));
        assertTrue(fileContent.contains("<Bezeichnung>Römersmattquellen der Wasserversorgung Bellach"));
    }

    private static String contentGzipFile(File gzipFile) {
        try {
            File tempFile = Files.createTempFile("gziptest", ".file").toFile();

            FileInputStream fis = new FileInputStream(gzipFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            fos.close();
            gis.close();
            
            byte[] bytes = Files.readAllBytes(tempFile.toPath());
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
