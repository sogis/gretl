package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class S3Bucket2BucketTest {
    @Container
    public static LocalStackContainer localStackContainer = new LocalStackContainer(S3TestHelper.getLocalstackImage())
            .withServices(S3);

    private static String s3AccessKey;
    private static String s3SecretKey;
    private static URI s3Endpoint;
    private static String s3Region;
    private static String acl;
    private static String s3SourceBucketName;
    private static String s3TargetBucketName;
    private static S3TestHelper s3TestHelper;

    @BeforeAll
    public static void setUp() throws Exception {
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
    void uploadDirectory_Ok() throws Exception {
        S3Client s3Client = s3TestHelper.getS3Client();
        s3TestHelper.createBucketIfNotExists(s3Client, s3SourceBucketName);
        s3TestHelper.createBucketIfNotExists(s3Client, s3TargetBucketName);

        List<File> files = new ArrayList<File>() {{
            add(TestUtil.getResourceFile(TestUtil.S3_BUCKET_DIR_PATH + "/foo.txt"));
            add(TestUtil.getResourceFile(TestUtil.S3_BUCKET_DIR_PATH + "/bar.txt"));
        }};

        for (File file : files) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3SourceBucketName)
                    .key(file.getName())
                    .contentLength(file.length())
                    .build();

            s3Client.putObject(request, file.toPath());
        }

        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("bar.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("download.txt").build());

        // Upload files  and copy files from one bucket to another.
        GradleVariable[] gvs = {
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey),
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3SourceBucket", s3SourceBucketName),
                GradleVariable.newGradleProperty("s3TargetBucket", s3TargetBucketName),
                GradleVariable.newGradleProperty("s3Endpoint", s3Endpoint.toString()),
                GradleVariable.newGradleProperty("s3Region", s3Region),
                GradleVariable.newGradleProperty("s3Acl", acl)
        };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3Bucket2Bucket", gvs);

        // Check result.
        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3TargetBucketName)
                .build();

        ListObjectsResponse res = s3Client.listObjects(listObjects);
        List<S3Object> objects = res.contents();

        List<String> keyList = new ArrayList<>();
        for (S3Object myObject : objects) {
            keyList.add(myObject.key());
        }

        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));
        assertEquals(2, keyList.size());

        // Remove uploaded files from buckets.
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3SourceBucketName).key("bar.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3TargetBucketName).key("bar.txt").build());
    }
}