package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class S3DownloadTest {
    @Container
    public LocalStackContainer localStackContainer = new LocalStackContainer(S3TestHelper.getLocalstackImage())
            .withServices(S3);

    private final S3TestHelper s3TestHelper;
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3BucketName;

    public S3DownloadTest() {
        this.s3AccessKey = localStackContainer.getAccessKey();
        this.s3SecretKey = localStackContainer.getSecretKey();
        this.s3BucketName = System.getProperty("s3BucketName");

        final URI s3Endpoint = localStackContainer.getEndpointOverride(S3);
        final String s3Region = localStackContainer.getRegion();
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
