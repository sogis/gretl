package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ch.so.agi.gretl.testutil.S3Test;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3DownloadTest {
    @Test
    @Category(S3Test.class)
    public void downloadFile_Ok() throws Exception {
        String s3AccessKey = System.getProperty("s3AccessKey");
        String s3SecretKey = System.getProperty("s3SecretKey");
        String s3BucketName = System.getProperty("s3BucketName");
        String s3ClientUrl = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DownloadFile");
        GradleVariable[] variables = {
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        IntegrationTestUtil.getGradleRunner(projectDirectory, "filedownload", variables).build();

        // Check result. 
        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey));
        Region region = Region.of(s3Region);
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3ClientUrl))
                .build(); 

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key("download.txt")
                .build();
        
        ResponseInputStream<GetObjectResponse> is = s3client.getObject(getObjectRequest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));        
        assertTrue(reader.readLine().equalsIgnoreCase("fubar"));
        
        // Remove downloaded file.
        Files.delete(Paths.get( projectDirectory+ "/download.txt"));
    }    
}
