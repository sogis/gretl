package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class S3DownloadStepTest {

    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3BucketName;
    private final GretlLogger log;

    @TempDir
    public Path folder;

    public S3DownloadStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
        this.s3AccessKey = System.getProperty("s3AccessKey");;
        this.s3SecretKey = System.getProperty("s3SecretKey");
        this.s3BucketName = System.getProperty("s3BucketName");
    }

    @Test
    @Tag("s3Test")
    public void downloadFile_Ok() throws Exception {
        String s3EndPoint = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";
        String key = "download.txt";
        Path downloadDir = TestUtil.createTempDir(folder, "downloadFile_Ok");

        // Download a single file.
        S3DownloadStep s3DownloadStep = new S3DownloadStep();
        s3DownloadStep.execute(s3AccessKey, s3SecretKey, s3BucketName, key, s3EndPoint, s3Region, downloadDir.toFile());

        // Check result.
        String content = new String(Files.readAllBytes(Paths.get(downloadDir.toAbsolutePath().toString(), key)));
        log.debug(content);
        assertEquals("fubar", content.substring(0, 5));
    }
}