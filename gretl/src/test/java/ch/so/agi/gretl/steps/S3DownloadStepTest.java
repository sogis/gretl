package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;


import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.S3Test;

public class S3DownloadStepTest {
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3BucketName;
    private final String s3Endpoint;
    private final String s3Region;
    private final GretlLogger log;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public S3DownloadStepTest() {
        this.s3AccessKey = System.getProperty("s3AccessKey");
        this.s3SecretKey = System.getProperty("s3SecretKey");
        this.s3BucketName = System.getProperty("s3BucketName");
        this.s3Endpoint = System.getProperty("s3Endpoint");
        this.s3Region = System.getProperty("s3Region");
        this.log = LogEnvironment.getLogger(S3DownloadStepTest.class);
    }
        
    @Test
    @Category(S3Test.class)
    public void downloadFile_Ok() throws Exception {
        String key = "download.txt";
        File downloadDir = folder.newFolder("downloadFile_Ok");  
        
        // Download a single file.
        S3DownloadStep s3DownloadStep = new S3DownloadStep();
        s3DownloadStep.execute(s3AccessKey, s3SecretKey, s3BucketName, key, s3Endpoint, s3Region, downloadDir);
        
        // Check result. 
        String content = new String(Files.readAllBytes(Paths.get(downloadDir.getAbsolutePath(), key)));
        log.debug(content);
        assertEquals("fubar", content.substring(0, 5));        
    }
}
