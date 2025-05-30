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

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class S3DeleteTest {
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

    private final GradleVariable[] gradleVariables = {
            GradleVariable.newGradleProperty("s3AccessKey", s3AccessKey),
            GradleVariable.newGradleProperty("s3SecretKey", s3SecretKey),
            GradleVariable.newGradleProperty("s3BucketName", s3BucketName),
            GradleVariable.newGradleProperty("s3Region", s3Region),
            GradleVariable.newGradleProperty("s3Endpoint", s3Endpoint.toString())
    };

    @BeforeAll
    public static void setUp() {
        s3AccessKey = localStackContainer.getAccessKey();
        s3SecretKey = localStackContainer.getSecretKey();
        s3BucketName = "ch.so.agi.gretl.test";
        s3Endpoint = localStackContainer.getEndpointOverride(S3);
        s3Region = localStackContainer.getRegion();
        s3TestHelper = new S3TestHelper(s3AccessKey, s3SecretKey, s3Region, s3Endpoint.toString());
        s3Client = s3TestHelper.getS3Client();
    }

    @Test
    @Tag(TestTags.S3_TEST)
    void deleteFile_Ok() throws Exception {
        // Prepare
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);
        File sourceObject = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DeleteFile/upload/foo.txt");
        s3TestHelper.upload(sourceObject, new HashMap<>(), s3BucketName, "public-read");

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DeleteFile");

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check result       
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        assertEquals(0, listResponse.contents().size());
    }
    
    @Test
    @Tag(TestTags.S3_TEST)
    void deleteDirectory_Ok() throws Exception {
        // Prepare
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);
        {
            File sourceObject = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DeleteDirectory/upload/foo.txt");
            s3TestHelper.upload(sourceObject, new HashMap<>(), s3BucketName, "public-read");            
        }
        {
            File sourceObject = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DeleteDirectory/upload/bar.txt");
            s3TestHelper.upload(sourceObject, new HashMap<>(), s3BucketName, "public-read");                        
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DeleteDirectory");

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check result       
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        assertEquals(0, listResponse.contents().size());
    }
}