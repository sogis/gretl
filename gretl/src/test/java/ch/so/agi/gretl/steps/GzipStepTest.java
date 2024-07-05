package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GzipStepTest {

    private final GretlLogger log;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public GzipStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void compress_file_Ok() throws Exception {
        File outDirectory = folder.newFolder("transformFile_Ok");
        File dataFile = TestUtil.getResourceFile(TestUtil.PLANREGISTER_XML_PATH);
        File gzipFile = Paths.get(outDirectory.getAbsolutePath(), "planregister.xml.gz").toFile();
        
        // Transform File
        GzipStep gzipStep = new GzipStep();
        gzipStep.execute(dataFile, gzipFile);
        
        // Check result
        String fileContent = contentGzipFile(gzipFile);

        assertNotNull(fileContent);
        assertTrue(fileContent.contains("ili2pg-4.9.0-eb3a0d51869bd2adeeb51fe7aba4b526fe002c1a"));
        assertTrue(fileContent.contains("<Bezeichnung>Römersmattquellen der Wasserversorgung Bellach"));
    }
    
    private String contentGzipFile(File gzipFile) {
        try {
            File tempFile = Files.createTempFile("gziptest", ".file").toFile();

            try (
                FileInputStream fis = new FileInputStream(gzipFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                FileOutputStream fos = new FileOutputStream(tempFile)
            ) {
                byte[] buffer = new byte[1024];
                int len;

                while ((len = gis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }

                byte[] bytes = Files.readAllBytes(tempFile.toPath());
                return new String(bytes);
            }
        } catch (IOException e) {
            log.error("IOException", e);
            return null;
        }
    }
}
