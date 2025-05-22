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
public class S3DownloadTest {
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
    void downloadFile_Ok() throws Exception {
        // Prepare
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);
        File sourceObject = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DownloadFile/upload/download.txt");
        s3TestHelper.upload(sourceObject, new HashMap<>(), s3BucketName, "public-read");

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DownloadFile");

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check result
        S3Client s3client = s3TestHelper.getS3Client();

        // TODO Warum wird nicht einfach das lokale upload/download.txt verwendet?
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key("download.txt")
                .build();

        ResponseInputStream<GetObjectResponse> is = s3client.getObject(getObjectRequest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        assertTrue(reader.readLine().equalsIgnoreCase("fubar"));

        // Remove downloaded file
        Files.delete(Paths.get("src/integrationTest/jobs/S3DownloadFile/download.txt"));
    }
    
    @Test
    @Tag(TestTags.S3_TEST)
    void downloadDirectory_Ok() throws Exception {
        // Prepare
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);
        {
            File sourceObject = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DownloadDirectory/upload/foo.txt");
            s3TestHelper.upload(sourceObject, new HashMap<>(), s3BucketName, "public-read");            
        }
        {
            File sourceObject = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DownloadDirectory/upload/bar.txt");
            s3TestHelper.upload(sourceObject, new HashMap<>(), s3BucketName, "public-read");                        
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DownloadDirectory");

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check result
        {
            String content = Files.readString(new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DownloadDirectory/foo.txt").toPath());
            assertEquals("foo", content.substring(0, 3));            
        }
        {
            String content = Files.readString(new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/S3DownloadDirectory/bar.txt").toPath());
            assertEquals("bar", content.substring(0, 3));            
        }

        // Remove downloaded file
        Files.delete(Paths.get("src/integrationTest/jobs/S3DownloadDirectory/foo.txt"));
        Files.delete(Paths.get("src/integrationTest/jobs/S3DownloadDirectory/bar.txt"));
    }
}