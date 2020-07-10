package ch.so.agi.gretl.steps;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

//import com.adobe.testing.s3mock.junit4.S3MockRule;
//import com.adobe.testing.s3mock.util.HashUtil;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//import com.amazonaws.services.s3.model.S3Object;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.S3UnitTest;

public class S3UploadStepTest {

    public S3UploadStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;
    
//    @ClassRule
//    public static final S3MockRule S3_MOCK_RULE = S3MockRule.builder().silent().build();
//
//    private static final String BUCKET_NAME = "mydemotestbucket";
//    private static final String UPLOAD_FILE_NAME = "src/test/resources/data/s3upload/foo.txt";
//
//    private final AmazonS3 s3Client = S3_MOCK_RULE.createS3Client();

    @Test
    @Category(S3UnitTest.class)
    public void uploadFile_Ok() throws Exception {
        File sourceDir = new File("src/test/resources/data/s3upload/");
        
//        final File uploadFile = new File(UPLOAD_FILE_NAME);
//        s3Client.createBucket(BUCKET_NAME);
//        s3Client.putObject(new PutObjectRequest(BUCKET_NAME, uploadFile.getName(), uploadFile));
//
//        final S3Object s3Object = s3Client.getObject(BUCKET_NAME, uploadFile.getName());
//
//        final InputStream uploadFileIs = new FileInputStream(uploadFile);
//        final String uploadHash = HashUtil.getDigest(uploadFileIs);
//        final String downloadedHash = HashUtil.getDigest(s3Object.getObjectContent());
//        uploadFileIs.close();
//
//        assertThat("Up- and downloaded Files should have equal Hashes", uploadHash, is(equalTo(downloadedHash)));
//
//        s3Client.get
        
        
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute("awsAccessKeyAda", "awsSecretKey", sourceDir.getAbsolutePath(), "ch.so.agi.test", "https://s3.amazonaws.com/", "eu-central-1", "PublicRead");
        
        
        
//        Thread.sleep(30000);
    }
}
