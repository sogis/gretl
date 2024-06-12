package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.testutil.S3Test;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class S3Bucket2BucketStepTest {
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3SourceBucketName;
    private final String s3TargetBucketName;
    private final String s3Endpoint;
    private final String s3Region;
    private final String acl;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public S3Bucket2BucketStepTest() {
        this.s3AccessKey = System.getProperty("s3AccessKey");
        this.s3SecretKey = System.getProperty("s3SecretKey");
        this.s3SourceBucketName = "ch.so.agi.gretl.test";
        this.s3TargetBucketName = "ch.so.agi.gretl.test-copy";
        this.s3Endpoint = "https://s3.eu-central-1.amazonaws.com";
        this.s3Region = "eu-central-1";
        this.acl = "public-read";
    }
    
    @Test
    @Category(S3Test.class)
    public void copyFiles_Ok() throws Exception {
        File sourceObject = TestUtil.getResourceFile(TestUtil.S3_BUCKET_DIR_PATH);
        Map<String,String> metadata = new HashMap<>();
        S3Client s3client = getS3Client();

        deleteObjects(s3client, Arrays.asList("foo.txt", "bar.txt", "download.txt"));
        uploadFiles(sourceObject, metadata);
        copyFiles(metadata);

        // Check result. 
        ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(s3TargetBucketName).build();
        ListObjectsResponse res = s3client.listObjects(listObjects);
        List<S3Object> objects = res.contents();
        
        List<String> keyList = new ArrayList<>();
        for (S3Object s3Object : objects) {
            keyList.add(s3Object.key());
        }

        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        assertTrue(keyList.contains("download.txt"));
        assertEquals(3, keyList.size());
        deleteFiles(s3client);
    }

    private S3Client getS3Client() {
        return S3Client.builder()
                .credentialsProvider(getCredentialsProvider())
                .region(getRegion())
                .endpointOverride(URI.create(s3Endpoint))
                .build();
    }

    private AwsCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey));
    }

    private Region getRegion() {
        return Region.of(s3Region);
    }

    /**
     * Deletes objects by provided keys
     * @param s3Client the c3Client
     * @param keys list of keys to delete
     */
    private void deleteObjects(S3Client s3Client, List<String> keys) {
        Objects.requireNonNull(s3Client);
        Objects.requireNonNull(keys);

        for (String key : keys) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3TargetBucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        }
    }

    /**
     * Upload files from a directory
     * @param sourceObject directory for S3 bucket data on the local system
     * @param metadata metadata
     */
    private void uploadFiles(File sourceObject, Map<String, String> metadata) throws FileNotFoundException, URISyntaxException {
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3SourceBucketName, s3Endpoint, s3Region, acl, null, metadata);
    }

    /**
     * Copy files from one bucket to another
     * @param metadata metadata
     */
    private void copyFiles(Map<String, String> metadata) throws FileNotFoundException, UnsupportedEncodingException {
        S3Bucket2BucketStep s3Bucket2Bucket = new S3Bucket2BucketStep();
        s3Bucket2Bucket.execute(s3AccessKey, s3SecretKey, s3SourceBucketName, s3TargetBucketName, s3Endpoint, s3Region, acl, metadata);
    }

    /**
     * Delete updated files from the buckets
     * @param s3Client the S3 client
     */
    private void deleteFiles(S3Client s3Client) {
        Objects.requireNonNull(s3Client);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucketName).key("bar.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("bar.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("download.txt").build());
    }
}
