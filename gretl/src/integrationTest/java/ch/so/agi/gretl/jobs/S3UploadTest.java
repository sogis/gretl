package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S3UploadTest {
    private final S3TestHelper s3TestHelper;
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3BucketName;


    public S3UploadTest() {
        this.s3AccessKey = System.getProperty("s3AccessKey");
        this.s3SecretKey = System.getProperty("s3SecretKey");
        this.s3BucketName = System.getProperty("s3BucketName");

        String s3Region = "eu-central-1";
        String s3Endpoint = "https://s3.eu-central-1.amazonaws.com";
        this.s3TestHelper = new S3TestHelper(this.s3AccessKey, this.s3SecretKey, s3Region, s3Endpoint);
    }

    @Test
    @Tag(TestTags.S3_TEST)
    void uploadDirectory_Ok() throws Exception {
        S3Client s3client = s3TestHelper.getS3Client();
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());

        // Upload all files from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadDirectory", gvs);

        // Check result.
        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsResponse res = s3client.listObjects(listObjects);
        List<S3Object> objects = res.contents();
        
        List<String> keyList = new ArrayList<String>();
        for (S3Object myValue : objects) {
            keyList.add(myValue.key());
        }
        
        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());
    }
    
    @Test
    @Tag(TestTags.S3_TEST)
    void uploadFileTree_Ok() throws Exception {
        // Check result. 
        S3Client s3client = s3TestHelper.getS3Client();

        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.csv").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.csv").build());   

        // Upload all files from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFileTree", gvs);


        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsResponse res = s3client.listObjects(listObjects);
        List<S3Object> objects = res.contents();

        List<String> keyList = new ArrayList<>();
        for (S3Object myValue : objects) {
            keyList.add(myValue.key());
        }
        
        assertTrue(keyList.contains("foo.csv"));
        assertTrue(keyList.contains("bar.csv"));
        assertEquals(3, keyList.size()); // TODO 3 wegen download.txt
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.csv").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.csv").build());   
    }
    
    @Test
    @Tag(TestTags.S3_TEST)
    void uploadFile_Ok() throws Exception {
        S3Client s3client = s3TestHelper.getS3Client();
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());
        
        // Upload single file from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFile", gvs);

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
    @Tag(TestTags.S3_TEST)
    void uploadFile_Fail() throws Exception {
        // Upload single file from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", "login"), 
                GradleVariable.newGradleProperty("s3SecretKey", "password"),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        Assertions.assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFileFail", gvs, new StringBuffer(), new StringBuffer()));
    }
}
