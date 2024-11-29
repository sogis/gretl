package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
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

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class S3DownloadStepTest {
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

    public S3DownloadStepTest() {
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
    public void downloadFile_Ok() throws Exception {
        // Prepare
        S3Client s3Client = s3TestHelper.getS3Client();
        s3TestHelper.createBucketIfNotExists(s3Client, s3BucketName);

        String key = "foo.txt";
        Path downloadDir = TestUtil.createTempDir(folder, "downloadFile_Ok");

        File sourceObject = TestUtil.getResourceFile("data/s3upload/foo.txt");
        s3TestHelper.upload(sourceObject, new HashMap<>(), s3BucketName, "public-read");

        // Download a single file.
        S3DownloadStep s3DownloadStep = new S3DownloadStep();
        s3DownloadStep.execute(s3AccessKey, s3SecretKey, s3BucketName, key, s3Endpoint.toString(), s3Region, downloadDir.toFile());

        // Check result.
        String content = new String(Files.readAllBytes(Paths.get(downloadDir.toAbsolutePath().toString(), key)));
        log.debug(content);
        assertEquals("foo", content.substring(0, 3));
    }
}