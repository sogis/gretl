package ch.so.agi.gretl.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import static org.junit.Assert.*;

public class S3UploadTest {
    private final String s3AccessKey = System.getProperty("s3AccessKey");
    private final String s3SecretKey = System.getProperty("s3SecretKey");
    private final String s3BucketName = System.getProperty("s3BucketName");

    private final GradleVariable[] gradleVariables = {
            GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey),
            GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
            GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
    };

    @Test
    @Category(S3Test.class)    
    public void uploadDirectory_Ok() throws Exception {
        S3Client s3client = getS3Client();

        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3UploadDirectory");

        IntegrationTestUtil.executeTestRunner(projectDirectory, "directoryupload", gradleVariables);

        List<String> keyList = getKeys(s3client);
        
        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());
    }
    
    @Test
    @Category(S3Test.class)        
    public void uploadFileTree_Ok() throws Exception {
        S3Client s3client = getS3Client();

        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.csv").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.csv").build());   

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3UploadFileTree");

        IntegrationTestUtil.executeTestRunner(projectDirectory, "filetreeupload", gradleVariables);

        List<String> keyList = getKeys(s3client);

        assertTrue(keyList.contains("foo.csv"));
        assertTrue(keyList.contains("bar.csv"));
        assertEquals(3, keyList.size()); // TODO 3 wegen download.txt
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.csv").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.csv").build());   
    }

    @Test
    @Category(S3Test.class)    
    public void uploadFile_Ok() throws Exception {
        S3Client s3client = getS3Client();

        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3UploadFile");

        IntegrationTestUtil.executeTestRunner(projectDirectory, "fileupload", gradleVariables);

        // Check result. 
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key("bar.txt")
                .build();
        
        ResponseInputStream<GetObjectResponse> is = s3client.getObject(getObjectRequest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));        
        assertTrue(reader.readLine().equalsIgnoreCase("bar"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());
    }
    
    @Test
    @Category(S3Test.class)        
    public void uploadFile_Fail() {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3UploadFileFail");

        GradleVariable[] variables = {
                GradleVariable.newGradleProperty("s3AccessKey", "login"), 
                GradleVariable.newGradleProperty("s3SecretKey", "password"),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };

        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory, "fileupload", variables);
        });
    }

    private S3Client getS3Client(){
        AwsCredentialsProvider credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey));
        String s3Region = "eu-central-1";
        Region region = Region.of(s3Region);
        String s3ClientUrl = "https://s3.eu-central-1.amazonaws.com";
        return S3Client.builder()
                .credentialsProvider(credentials)
                .region(region)
                .endpointOverride(URI.create(s3ClientUrl))
                .build();
    }

    private List<String> getKeys(S3Client s3client){
        List<S3Object> objects = getS3Objects(s3client);

        List<String> keyList = new ArrayList<>();
        for (S3Object myValue : objects) {
            keyList.add(myValue.key());
        }
        return keyList;
    }

    private List<S3Object> getS3Objects(S3Client s3client){
        ListObjectsRequest listObjects =  ListObjectsRequest
                .builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsResponse res = s3client.listObjects(listObjects);
        return res.contents();
    }
}
