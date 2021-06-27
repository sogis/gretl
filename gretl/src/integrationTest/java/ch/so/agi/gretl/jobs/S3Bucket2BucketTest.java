package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.junit.Test;
import org.junit.experimental.categories.Category;

//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.model.ObjectListing;
//import com.amazonaws.services.s3.model.S3Object;
//import com.amazonaws.services.s3.model.S3ObjectSummary;

import ch.so.agi.gretl.testutil.S3Test;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Bucket2BucketTest {
    private String s3AccessKey = System.getProperty("s3AccessKey");
    private String s3SecretKey = System.getProperty("s3SecretKey");
    private String s3SourceBucket = "ch.so.agi.gretl.test";
    private String s3TargetBucket = "ch.so.agi.gretl.test-copy";

    @Test
    @Category(S3Test.class)    
    public void uploadDirectory_Ok() throws Exception {
        // Upload files  and copy files from one bucket to another.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3SourceBucket", s3SourceBucket),
                GradleVariable.newGradleProperty("s3TargetBucket", s3TargetBucket)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3Bucket2Bucket", gvs);

        // Check result. 
        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey));
        Region region = Region.of("eu-central-1");
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create("https://s3.eu-central-1.amazonaws.com"))
                .build(); 


        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3TargetBucket)
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
        assertTrue(keyList.contains("download.txt"));
        assertTrue(keyList.size() == 3);
        
        // Remove uploaded files from buckets.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucket).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucket).key("bar.txt").build());

        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("bar.txt").build()); 
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("download.txt").build());
    }    
}
