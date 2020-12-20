package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.S3Test;

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
        
        String s3EndPoint = "https://s3.amazonaws.com/";
        String s3Region = "eu-central-1";
        String acl = "PublicRead";
        Map<String,String> metaData = new HashMap<String,String>();
        metaData.put("lastModified", "2020-08-28");
        
        // Upload files from a directory.
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3BucketName, s3EndPoint, s3Region, acl, metaData);
        
        // Check result. 
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(s3EndPoint, s3Region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        ObjectListing listing = s3client.listObjects(s3BucketName);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
           listing = s3client.listNextBatchOfObjects (listing);
           summaries.addAll(listing.getObjectSummaries());
        }
        
        assertTrue(summaries.size() == 2);
        
        List<String> keyList = new ArrayList<String>();
        for (S3ObjectSummary summary : summaries) {
            keyList.add(summary.getKey());
        }
        
        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(s3BucketName, "foo.txt");
        s3client.deleteObject(s3BucketName, "bar.txt");
    }
    
    
    @Test
    @Category(S3Test.class)
    public void uploadFile_Ok() throws Exception {
        File sourceObject = new File("src/test/resources/data/s3upload/foo.txt");
        
        String s3EndPoint = "https://s3.amazonaws.com/";
        String s3Region = "eu-central-1";
        String acl = "PublicRead";
        Map<String,String> metaData = new HashMap<String,String>();        
        
        // Upload a single file.
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3BucketName, s3EndPoint, s3Region, acl, metaData);
        
        // Check result. 
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(s3EndPoint, s3Region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        S3Object s3Object = s3client.getObject(s3BucketName, "foo.txt");
        InputStream is = s3Object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));        
        assertTrue(reader.readLine().equalsIgnoreCase("foo"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(s3BucketName, "foo.txt");
    }
    
    @Test
    @Category(S3Test.class)
    public void uploadFile_Fail() throws Exception {
        File sourceObject = new File("src/test/resources/data/s3upload/foo.txt");
        
        String s3EndPoint = "https://s3.amazonaws.com/";
        String s3Region = "eu-central-1";
        String acl = "PublicRead";
        Map<String,String> metaData = new HashMap<String,String>();        

        // Upload a single file.
        try {
            S3UploadStep s3UploadStep = new S3UploadStep();
            s3UploadStep.execute("login", "secret", sourceObject, s3BucketName, s3EndPoint, s3Region, acl, metaData);
        } catch (AmazonS3Exception e) {
            assertTrue(e.getErrorCode().equalsIgnoreCase("InvalidAccessKeyId"));
        }
    }
}
