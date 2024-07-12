package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3UploadStepTest {
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3BucketName;
    private final String s3Endpoint;
    private final String s3Region;
    private final String acl;
    private final S3TestHelper s3TestHelper;

    @TempDir
    public Path folder;

    public S3UploadStepTest() {
        this.s3AccessKey = System.getProperty("s3AccessKey");
        this.s3SecretKey = System.getProperty("s3SecretKey");
        this.s3BucketName = System.getProperty("s3BucketName");
        this.s3Endpoint = "https://s3.eu-central-1.amazonaws.com";
        this.s3Region = "eu-central-1";
        this.acl = "public-read";
        this.s3TestHelper = new S3TestHelper(this.s3AccessKey, this.s3SecretKey, this.s3Region, this.s3Endpoint);
    }
    
    @Test
    @Tag(TestTags.S3_TEST)
    public void uploadDirectory_Ok() throws Exception {
        File sourceObject = TestUtil.getResourceFile("data/s3upload/");
        Map<String,String> metaData = new HashMap<String, String>() {{
            put("lastModified", "2020-08-28");
        }};

        S3Client s3Client = s3TestHelper.getS3Client();
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());

        // Upload files from a directory.
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3BucketName, s3Endpoint, s3Region, acl, null, metaData);
        
        // Check result. 
        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsResponse res = s3Client.listObjects(listObjects);
        List<S3Object> objects = res.contents();
        
        List<String> keyList = new ArrayList<>();
        for (S3Object obj : objects) {
            keyList.add(obj.key());
        }
  
        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        
        // Remove uploaded files from bucket.
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());
    }
    
    @Test
    @Tag(TestTags.S3_TEST)
    public void uploadFile_Ok() throws Exception {
        File sourceObject = TestUtil.getResourceFile("data/s3upload/foo.txt");
        Map<String,String> metaData = new HashMap<>();
        S3Client s3Client = s3TestHelper.getS3Client();

        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        
        // Upload a single file.
        s3TestHelper.upload(sourceObject, metaData, s3BucketName, acl);
        
        // Check result. 
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key("foo.txt")
                .build();

        ResponseInputStream<GetObjectResponse> is = s3Client.getObject(getObjectRequest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));        
        assertTrue(reader.readLine().equalsIgnoreCase("foo"));
        
        // Remove uploaded files from bucket.
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
    }
    
    @Test
    @Tag(TestTags.S3_TEST)
    public void uploadFile_Fail() throws Exception {
        File sourceObject = TestUtil.getResourceFile("data/s3upload/foo.txt");
        Map<String,String> metaData = new HashMap<>();

        try {
            // Upload a single file
            S3UploadStep s3UploadStep = new S3UploadStep();
            s3UploadStep.execute("login", "secret", sourceObject, s3BucketName, s3Endpoint, s3Region, acl, null, metaData);
        } catch (S3Exception e) {
            assertTrue(e.getMessage().contains("The AWS Access Key Id you provided does not exist in our records"));
        }
    }
}
