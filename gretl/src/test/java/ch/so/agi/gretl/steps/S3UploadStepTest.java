package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

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

public class S3UploadStepTest {

    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3BucketName;
    private final String s3Endpoint;
    private final String s3Region;
    private final String acl;
    private final S3TestHelper s3TestHelper;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

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
    @Category(S3Test.class)
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
    @Category(S3Test.class)
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
    @Category(S3Test.class)
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
