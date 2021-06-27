package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.S3Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3DownloadStepTest {
    private String s3AccessKey = System.getProperty("s3AccessKey");
    private String s3SecretKey = System.getProperty("s3SecretKey");
    private String s3BucketName = System.getProperty("s3BucketName");

    public S3DownloadStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;
        
    @Test
    @Category(S3Test.class)
    public void downloadFile_Ok() throws Exception {        
        String s3EndPoint = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";
        String key = "download.txt";
        File downloadDir = folder.newFolder("downloadFile_Ok");  
        
        // Upload a single file.
        S3DownloadStep s3DownloadStep = new S3DownloadStep();
        s3DownloadStep.execute(s3AccessKey, s3SecretKey, s3BucketName, key, s3EndPoint, s3Region, downloadDir);
        
        // Check result. 
        String content = new String(Files.readAllBytes(Paths.get(downloadDir.getAbsolutePath(), key)));
        System.out.println(content);
        assertEquals("fubar", content.substring(0, 5));        
    }
}
