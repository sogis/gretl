package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S3Bucket2BucketTest {
    private final S3TestHelper s3TestHelper;
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3SourceBucket;
    private final String s3TargetBucket;

    public S3Bucket2BucketTest() {
        this.s3AccessKey = System.getProperty("s3AccessKey");
        this.s3SecretKey = System.getProperty("s3SecretKey");
        this.s3SourceBucket = "ch.so.agi.gretl.test";
        this.s3TargetBucket = "ch.so.agi.gretl.test-copy";

        String s3Endpoint = "https://s3.eu-central-1.amazonaws.com";
        String s3Region = "eu-central-1";
        this.s3TestHelper = new S3TestHelper(this.s3AccessKey, this.s3SecretKey, s3Region, s3Endpoint);
    }

    @Test
    @Tag(TestTags.S3_TEST)
    void uploadDirectory_Ok() throws Exception {
        S3Client s3client = s3TestHelper.getS3Client();

        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("foo.txt").build());
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("bar.txt").build()); 
        s3client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucket).key("download.txt").build());

        // Upload files  and copy files from one bucket to another.
        GradleVariable[] gvs = { 
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey), 
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3SourceBucket", s3SourceBucket),
                GradleVariable.newGradleProperty("s3TargetBucket", s3TargetBucket)
            };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3Bucket2Bucket", gvs);

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
