package ch.so.agi.gretl.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.testutil.TestUtil;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

@Testcontainers
public class S3DeleteStepTest {
    @Container
    public static LocalStackContainer localStackContainer = new LocalStackContainer(S3TestHelper.getLocalstackImage())
            .withServices(S3);

    private static String s3AccessKey;
    private static String s3SecretKey;
    private static String s3BucketName;
    private static URI s3Endpoint;
    private static String s3Region;
    private static S3TestHelper s3TestHelper;
    private final GretlLogger log;

    @TempDir
    public Path folder;

    public S3DeleteStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        s3AccessKey = localStackContainer.getAccessKey();
        s3SecretKey = localStackContainer.getSecretKey();
        s3BucketName = "ch.so.agi.gretl.test";
        s3Endpoint = localStackContainer.getEndpointOverride(S3);
        s3Region = localStackContainer.getRegion();
        s3TestHelper = new S3TestHelper(s3AccessKey, s3SecretKey, s3Region, s3Endpoint.toString());
    }

    @Test
    @Tag(TestTags.S3_TEST)
    public void deleteFile_Ok() throws Exception {
        // Prepare
        S3Client s3Client = s3TestHelper.getS3Client();
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);

        String key = "foo.txt";

        File sourceObject = TestUtil.getResourceFile("data/s3upload/foo.txt");
        s3TestHelper.upload(sourceObject, new HashMap<>(), s3BucketName, "public-read");

        // Execute
        S3DeleteStep s3DeleteStep = new S3DeleteStep();
        s3DeleteStep.execute(s3AccessKey, s3SecretKey, s3BucketName, key, s3Endpoint.toString(), s3Region);
        
        // Check result
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        assertEquals(0, listResponse.contents().size());
    }
    
    @Test
    @Tag(TestTags.S3_TEST)
    public void deleteDirectory_Ok() throws Exception {
        // Prepare
        S3Client s3Client = s3TestHelper.getS3Client();
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);

        s3TestHelper.upload(TestUtil.getResourceFile("data/s3upload/foo.txt"), new HashMap<>(), s3BucketName, "public-read");
        s3TestHelper.upload(TestUtil.getResourceFile("data/s3upload/bar.txt"), new HashMap<>(), s3BucketName, "public-read");

        // Execute
        S3DeleteStep s3DeleteStep = new S3DeleteStep();
        s3DeleteStep.execute(s3AccessKey, s3SecretKey, s3BucketName, null, s3Endpoint.toString(), s3Region);
        
        // Check result
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        assertEquals(0, listResponse.contents().size());
    }

}
