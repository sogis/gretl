package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class S3DownloadTest {
    private final S3TestHelper s3TestHelper;
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3BucketName;

    public S3DownloadTest() {
        this.s3AccessKey = System.getProperty("s3AccessKey");
        this.s3SecretKey = System.getProperty("s3SecretKey");
        this.s3BucketName = System.getProperty("s3BucketName");

        String s3Endpoint = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";
        this.s3TestHelper = new S3TestHelper(this.s3AccessKey, this.s3SecretKey, s3Region, s3Endpoint);
    }


    @Test
    @Tag(TestTags.S3_TEST)
    void downloadFile_Ok() throws Exception {
        // Download single file from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3DownloadFile", gvs);

        // Check result.
        S3Client s3client = s3TestHelper.getS3Client();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key("download.txt")
                .build();
        
        ResponseInputStream<GetObjectResponse> is = s3client.getObject(getObjectRequest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));        
        assertTrue(reader.readLine().equalsIgnoreCase("fubar"));
        
        // Remove downloaded file.
        Files.delete(Paths.get("src/integrationTest/jobs/S3DownloadFile/download.txt"));
    }    
}
