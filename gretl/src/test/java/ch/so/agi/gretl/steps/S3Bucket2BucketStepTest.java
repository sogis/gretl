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
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.S3Test;

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
        
        String s3EndPoint = "https://s3.amazonaws.com/";
        String s3Region = "eu-central-1";
        String acl = "PublicRead";
        Map<String,String> metaData = new HashMap<String,String>();
        
        // Upload files from a directory.
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3SourceBucketName, s3EndPoint, s3Region, acl, null, metaData);
        
        // Copy files from one bucket to another.
        S3Bucket2BucketStep s3Bucket2Bucket = new S3Bucket2BucketStep();
        s3Bucket2Bucket.execute(s3AccessKey, s3SecretKey, s3SourceBucketName, s3TargetBucketName, s3EndPoint, s3Region, metaData);
        
        // Check result. 
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(s3EndPoint, s3Region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        ObjectListing listing = s3client.listObjects(s3SourceBucketName);
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
        
        // Remove uploaded files from buckets.
        s3client.deleteObject(s3SourceBucketName, "foo.txt");
        s3client.deleteObject(s3SourceBucketName, "bar.txt");
        
        s3client.deleteObject(s3TargetBucketName, "foo.txt");
        s3client.deleteObject(s3TargetBucketName, "bar.txt");
    }    
}
