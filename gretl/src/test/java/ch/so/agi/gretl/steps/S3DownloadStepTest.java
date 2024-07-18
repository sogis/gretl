package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.S3TestHelper;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class S3DownloadStepTest {
    @Container
    public LocalStackContainer localStackContainer = new LocalStackContainer(S3TestHelper.getLocalstackImage())
            .withServices(S3);

    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3BucketName;
    private final GretlLogger log;

    @TempDir
    public Path folder;

    public S3DownloadStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
        this.s3AccessKey = localStackContainer.getAccessKey();
        this.s3SecretKey = localStackContainer.getSecretKey();
        this.s3BucketName = System.getProperty("s3BucketName");
    }

    @Test
    @Tag(TestTags.S3_TEST)
    public void downloadFile_Ok() throws Exception {
        URI s3EndPoint = localStackContainer.getEndpointOverride(S3);
        String s3Region = localStackContainer.getRegion();
        String key = "download.txt";
        Path downloadDir = TestUtil.createTempDir(folder, "downloadFile_Ok");

        // Download a single file.
        S3DownloadStep s3DownloadStep = new S3DownloadStep();
        s3DownloadStep.execute(s3AccessKey, s3SecretKey, s3BucketName, key, s3EndPoint.toString(), s3Region, downloadDir.toFile());

        // Check result.
        String content = new String(Files.readAllBytes(Paths.get(downloadDir.toAbsolutePath().toString(), key)));
        log.debug(content);
        assertEquals("fubar", content.substring(0, 5));
    }
}