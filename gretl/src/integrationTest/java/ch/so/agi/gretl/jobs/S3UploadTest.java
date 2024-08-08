package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class S3UploadTest {
    @Container
    public static LocalStackContainer localStackContainer = new LocalStackContainer(S3TestHelper.getLocalstackImage())
            .withServices(S3);

    private static String s3AccessKey;
    private static String s3SecretKey;
    private static String s3BucketName;
    private static URI s3Endpoint;
    private static String s3Region;
    private static S3TestHelper s3TestHelper;
    private static S3Client s3Client;

    @BeforeAll
    public static void setUp() {
        s3AccessKey = localStackContainer.getAccessKey();
        s3SecretKey = localStackContainer.getSecretKey();
        s3BucketName = System.getProperty("s3BucketName");
        s3Endpoint = localStackContainer.getEndpointOverride(S3);
        s3Region = localStackContainer.getRegion();
        s3TestHelper = new S3TestHelper(s3AccessKey, s3SecretKey, s3Region, s3Endpoint.toString());
        s3Client = s3TestHelper.getS3Client();
    }

    @Test
    @Tag(TestTags.S3_TEST)
    void uploadDirectory_Ok() throws Exception {
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());

        // Upload all files from a directory.
        GradleVariable[] gvs = {
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey),
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName),
                GradleVariable.newGradleProperty("s3Region", s3Region),
                GradleVariable.newGradleProperty("s3Endpoint", s3Endpoint.toString())
        };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadDirectory", gvs);

        // Check result.
        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsResponse res = s3Client.listObjects(listObjects);
        List<S3Object> objects = res.contents();

        List<String> keyList = new ArrayList<>();
        for (S3Object myValue : objects) {
            keyList.add(myValue.key());
        }

        assertTrue(keyList.contains("foo.txt"));
        assertTrue(keyList.contains("bar.txt"));

        // Remove uploaded files from bucket.
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.txt").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());
    }

    @Test
    @Tag(TestTags.S3_TEST)
    void uploadFileTree_Ok() throws Exception {
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);

        // Remove uploaded files from bucket.
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.csv").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.csv").build());

        // Upload all files from a directory.
        GradleVariable[] gvs = {
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey),
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName),
                GradleVariable.newGradleProperty("s3Region", s3Region),
                GradleVariable.newGradleProperty("s3Endpoint", s3Endpoint.toString())
        };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFileTree", gvs);

        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsResponse res = s3Client.listObjects(listObjects);
        List<S3Object> objects = res.contents();

        List<String> keyList = new ArrayList<>();
        for (S3Object myValue : objects) {
            keyList.add(myValue.key());
        }

        assertTrue(keyList.contains("foo.csv"));
        assertTrue(keyList.contains("bar.csv"));
        assertEquals(2, keyList.size());

        // Remove uploaded files from bucket.
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("foo.csv").build());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.csv").build());
    }

    @Test
    @Tag(TestTags.S3_TEST)
    void uploadFile_Ok() throws Exception {
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());

        // Upload single file from a directory.
        GradleVariable[] gvs = {
                GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey),
                GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName),
                GradleVariable.newGradleProperty("s3Region", s3Region),
                GradleVariable.newGradleProperty("s3Endpoint", s3Endpoint.toString())
        };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFile", gvs);

        // Check result.
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key("bar.txt")
                .build();

        ResponseInputStream<GetObjectResponse> is = s3Client.getObject(getObjectRequest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        assertTrue(reader.readLine().equalsIgnoreCase("bar"));

        // Remove uploaded files from bucket.
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3BucketName).key("bar.txt").build());
    }

    @Test
    @Tag(TestTags.S3_TEST)
    void uploadFile_Fail() throws Exception {
        // Upload single file from a directory.
        GradleVariable[] gvs = {
                GradleVariable.newGradleProperty("s3AccessKey", "login"),
                GradleVariable.newGradleProperty("s3SecretKey", "password"),
                GradleVariable.newGradleProperty("s3BucketName", s3BucketName),
                GradleVariable.newGradleProperty("s3Region", s3Region),
                GradleVariable.newGradleProperty("s3Endpoint", s3Endpoint.toString())
        };
        Assertions.assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFileFail", gvs, new StringBuffer(), new StringBuffer()));
    }
}