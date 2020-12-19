package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import ch.so.agi.gretl.testutil.S3Test;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class S3UploadTest {
    private String s3AccessKey = System.getProperty("s3AccessKey");
    private String s3SecretKey = System.getProperty("s3SecretKey");
    private String s3BucketName = System.getProperty("s3BucketName");

    @Test
    @Category(S3Test.class)    
    public void uploadDirectory_Ok() throws Exception {
        // Upload all files from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadDirectory", gvs);

        // Check result. 
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration("https://s3.amazonaws.com/", "eu-central-1"))
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
    public void uploadFileTree_Ok() throws Exception {
        // Upload all files from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFileTree", gvs);

        // Check result. 
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration("https://s3.amazonaws.com/", "eu-central-1"))
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
        
        assertTrue(keyList.contains("foo.csv"));
        assertTrue(keyList.contains("bar.csv"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(s3BucketName, "foo.csv");
        s3client.deleteObject(s3BucketName, "bar.csv");
    }
    
    @Test
    @Category(S3Test.class)    
    public void uploadFile_Ok() throws Exception {
        // Upload single file from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFile", gvs);

        // Check result. 
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration("https://s3.amazonaws.com/", "eu-central-1"))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        S3Object s3Object = s3client.getObject(s3BucketName, "bar.txt");
        InputStream is = s3Object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));        
        assertTrue(reader.readLine().equalsIgnoreCase("bar"));
        
        // Remove uploaded files from bucket.
        s3client.deleteObject(s3BucketName, "bar.txt");        
    }
    
    @Test
    @Category(S3Test.class)        
    public void uploadFile_Fail() throws Exception {
        // Upload single file from a directory.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", "login"), 
                GradleVariable.newGradleProperty("s3SecretKey", "password"),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName)
            };
        assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFileFail", gvs, new StringBuffer(), new StringBuffer()));
    }
}
