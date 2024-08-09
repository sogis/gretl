package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    @Category(S3Test.class)    
    public void uploadDirectory_Ok() throws Exception {
        String s3AccessKey = System.getProperty("s3AccessKey");
        String s3SecretKey = System.getProperty("s3SecretKey");
        String s3SourceBucket = "ch.so.agi.gretl.test";
        String s3TargetBucket = "ch.so.agi.gretl.test-copy";
        String s3ClientUrl = "https://s3.eu-central-1.amazonaws.com";

        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey));
        Region region = Region.of("eu-central-1");
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3ClientUrl))
                .build(); 

        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("bar.txt").build()); 
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("download.txt").build());

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3Bucket2Bucket");

        // Upload files  and copy files from one bucket to another.
        GradleVariable[] variables = {
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey),
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3SourceBucket", s3SourceBucket),
                GradleVariable.newGradleProperty("s3TargetBucket", s3TargetBucket)
            };

        IntegrationTestUtil.executeTestRunner(projectDirectory, "copyfiles", variables).build();

        // Check result. 
        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3TargetBucket)
                .build();

        ListObjectsResponse res = s3client.listObjects(listObjects);
        List<S3Object> objects = res.contents();
        
        List<String> keyList = new ArrayList<String>();
        for (S3Object myObject : objects) {
            keyList.add(myObject.key());
        }

        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        assertTrue(keyList.contains("download.txt"));
        assertEquals(3, keyList.size());
        
        // Remove uploaded files from buckets.
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucket).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucket).key("bar.txt").build());

        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("bar.txt").build()); 
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("download.txt").build());
    }    
}
