package ch.so.agi.gretl.jobs;


import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GzipTest {

    private static GretlLogger log;

    public GzipTest() {
        log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void compress_file_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Gzip");
        IntegrationTestUtil.executeTestRunner(projectDirectory);
                
        // Validate result
        File gzipFile = new File("src/integrationTest/jobs/Gzip/planregister.xml.gz");
        String fileContent = contentGzipFile(gzipFile);

        assertNotNull(fileContent);
        assertTrue(fileContent.contains("ili2pg-4.9.0-eb3a0d51869bd2adeeb51fe7aba4b526fe002c1a"));
        assertTrue(fileContent.contains("<Bezeichnung>Römersmattquellen der Wasserversorgung Bellach"));
    }

    private static String contentGzipFile(File gzipFile) {
        Path tempFilePath = null;

        try {
            // Create a temporary file
            tempFilePath = Files.createTempFile("gziptest", ".file");

            // Decompress the GZIP file to the temporary file
            try (FileInputStream fis = new FileInputStream(gzipFile);
                 GZIPInputStream gis = new GZIPInputStream(fis);
                 FileOutputStream fos = new FileOutputStream(tempFilePath.toFile())
            ) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }

            // Read the contents of the temporary file
            return new String(Files.readAllBytes(tempFilePath));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            // Cleanup the temporary file
            if (tempFilePath != null) {
                try {
                    Files.deleteIfExists(tempFilePath);
                } catch (IOException e) {
                    log.error("Failed to delete temporary file", e);
                }
            }
        }
    }
}
