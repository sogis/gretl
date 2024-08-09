package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import ch.so.agi.gretl.util.IntegrationTestUtil;


public class GzipTest {
    @Test
    public void compress_file_Ok() throws Exception {        

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Gzip");

        IntegrationTestUtil.executeTestRunner(projectDirectory, "compressFile");
                
        // Validate result
        File gzipFile = new File(projectDirectory + "/planregister.xml.gz");
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
