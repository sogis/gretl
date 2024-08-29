package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class S3Bucket2BucketStepTest {
    @Container
    public static LocalStackContainer localStackContainer = new LocalStackContainer(S3TestHelper.getLocalstackImage())
            .withServices(S3);

    private static String s3AccessKey;
    private static String s3SecretKey;
    private static String s3SourceBucketName;
    private static String s3TargetBucketName;
    private static URI s3Endpoint;
    private static String s3Region;
    private static String acl;
    private static S3TestHelper s3TestHelper;

    @TempDir
    public Path folder;

    @BeforeAll
    public static void setUpClass() {
        s3AccessKey = localStackContainer.getAccessKey();
        s3SecretKey = localStackContainer.getSecretKey();
        s3SourceBucketName = "ch.so.agi.gretl.test";
        s3TargetBucketName = "ch.so.agi.gretl.test-copy";
        s3Endpoint = localStackContainer.getEndpointOverride(S3);
        s3Region = localStackContainer.getRegion();
        acl = "public-read";
        s3TestHelper = new S3TestHelper(s3AccessKey, s3SecretKey, s3Region, s3Endpoint.toString());
    }

    @Test
    @Tag(TestTags.S3_TEST)
    public void copyFiles_Ok() throws Exception {
        S3Client s3Client = s3TestHelper.getS3Client();
        s3TestHelper.createBucketIfNotExists(s3Client, s3SourceBucketName);
        s3TestHelper.createBucketIfNotExists(s3Client, s3TargetBucketName);

        File sourceObject = TestUtil.getResourceFile(TestUtil.S3_BUCKET_DIR_PATH);
        Map<String,String> metadata = new HashMap<>();
        deleteObjects(s3Client, Arrays.asList("foo.txt", "bar.txt"));
        s3TestHelper.upload(sourceObject, metadata, s3SourceBucketName, acl);
        copyFiles(metadata);

        // Check result. 
        ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(s3TargetBucketName).build();
        ListObjectsResponse res = s3Client.listObjects(listObjects);
        List<S3Object> objects = res.contents();
        
        List<String> keyList = new ArrayList<>();
        for (S3Object s3Object : objects) {
            keyList.add(s3Object.key());
        }

        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        assertEquals(2, keyList.size());
        deleteFiles(s3Client);
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
     * Copy files from one bucket to another
     * @param metadata metadata
     */
    private void copyFiles(Map<String, String> metadata) throws FileNotFoundException, UnsupportedEncodingException {
        S3Bucket2BucketStep s3Bucket2Bucket = new S3Bucket2BucketStep();
        s3Bucket2Bucket.execute(s3AccessKey, s3SecretKey, s3SourceBucketName, s3TargetBucketName, s3Endpoint.toString(), s3Region, acl, metadata);
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
    }
}
