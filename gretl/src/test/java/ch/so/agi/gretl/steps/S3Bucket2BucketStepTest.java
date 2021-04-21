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

//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.model.ObjectListing;
//import com.amazonaws.services.s3.model.S3ObjectSummary;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.S3Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Bucket2BucketStepTest {
    private String s3AccessKey = System.getProperty("s3AccessKey");
    private String s3SecretKey = System.getProperty("s3SecretKey");
    private String s3SourceBucketName = "ch.so.agi.gretl.test";
    private String s3TargetBucketName = "ch.so.agi.gretl.test-copy";

    public S3Bucket2BucketStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;
    
    @Test
    @Category(S3Test.class)
    public void copyFiles_Ok() throws Exception {
        File sourceObject = new File("src/test/resources/data/s3bucket2bucket/");
        
        String s3EndPoint = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";
        String acl = "public-read";
        Map<String,String> metaData = new HashMap<String,String>();
        
        // Upload files from a directory.
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3SourceBucketName, s3EndPoint, s3Region, acl, null, metaData);
        
        // Copy files from one bucket to another.
        S3Bucket2BucketStep s3Bucket2Bucket = new S3Bucket2BucketStep();
        s3Bucket2Bucket.execute(s3AccessKey, s3SecretKey, s3SourceBucketName, s3TargetBucketName, s3EndPoint, s3Region, acl, metaData);
        
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
                .bucket(s3TargetBucketName)
                .build();

        ListObjectsResponse res = s3client.listObjects(listObjects);
        List<S3Object> objects = res.contents();
        
        List<String> keyList = new ArrayList<String>();
        for (ListIterator<S3Object> iterVals = objects.listIterator(); iterVals.hasNext(); ) {
            S3Object myObject = iterVals.next();            
            keyList.add(myObject.key());
        }

        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        assertTrue(keyList.size() == 2);
        
        // Remove uploaded files from buckets.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucketName).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucketName).key("bar.txt").build());

        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("bar.txt").build());
    }    
}
