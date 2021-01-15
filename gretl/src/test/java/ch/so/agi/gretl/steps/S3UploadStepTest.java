package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
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

public class S3UploadStepTest {
    private String s3AccessKey = System.getProperty("s3AccessKey");
    private String s3SecretKey = System.getProperty("s3SecretKey");
    private String s3BucketName = System.getProperty("s3BucketName");

    public S3UploadStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;
    
    @Test
    @Category(S3Test.class)
    public void uploadDirectory_Ok() throws Exception {
        File sourceObject = new File("src/test/resources/data/s3upload/");
        
        String s3EndPoint = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";
        String acl = "public-read";
        Map<String,String> metaData = new HashMap<String,String>();
        metaData.put("lastModified", "2020-08-28");
        
        // Upload files from a directory.
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3BucketName, s3EndPoint, s3Region, acl, null, metaData);
        
        // Check result. 
        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey));
        Region region = Region.of(s3Region);
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3EndPoint))
                .build(); 

        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsResponse res = s3client.listObjects(listObjects);
        List<S3Object> objects = res.contents();
        
        List<String> keyList = new ArrayList<String>();
        for (ListIterator<S3Object> iterVals = objects.listIterator(); iterVals.hasNext(); ) {
            S3Object myValue = iterVals.next();            
            keyList.add(myValue.key());
        }
  
        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());
    }
    
    @Test
    @Category(S3Test.class)
    public void uploadFile_Ok() throws Exception {
        File sourceObject = new File("src/test/resources/data/s3upload/foo.txt");
        
        String s3EndPoint = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";
        String acl = "public-read";
        Map<String,String> metaData = new HashMap<String,String>();        
        
        // Upload a single file.
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3BucketName, s3EndPoint, s3Region, acl, null, metaData);
        
        // Check result. 
        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey));
        Region region = Region.of(s3Region);
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3EndPoint))
                .build(); 

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key("foo.txt")
                .build();

        ResponseInputStream<GetObjectResponse> is = s3client.getObject(getObjectRequest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));        
        assertTrue(reader.readLine().equalsIgnoreCase("foo"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
    }
    
    @Test
    @Category(S3Test.class)
    public void uploadFile_Fail() throws Exception {
        File sourceObject = new File("src/test/resources/data/s3upload/foo.txt");
        
        String s3EndPoint = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";
        String acl = "public-read";
        Map<String,String> metaData = new HashMap<String,String>();        

        // Upload a single file.
        try {
            S3UploadStep s3UploadStep = new S3UploadStep();
            s3UploadStep.execute("login", "secret", sourceObject, s3BucketName, s3EndPoint, s3Region, acl, null, metaData);
        } catch (S3Exception e) {
            assertTrue(e.getMessage().contains("The AWS Access Key Id you provided does not exist in our records"));
        }
    }
}
