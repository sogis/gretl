package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class GzipStepTest {
    
    public GzipStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;
    
    @Test
    public void gzip_file_Ok() throws Exception {
        File outDirectory = folder.newFolder("transformFile_Ok");
        //File outDirectory = new File("/Users/stefan/tmp/gzip/");
        File dataFile = new File("src/test/resources/data/gzip/planregister.xml");
        File gzipFile = Paths.get(outDirectory.getAbsolutePath(), "planregister.xml.gz").toFile();
        
        // Transform File
        GzipStep gzipStep = new GzipStep();
        gzipStep.execute(dataFile, gzipFile);
        
        // Check result
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
