package ch.so.agi.gretl.jobs;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ch.so.agi.gretl.testutil.S3Test;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class S3UploadTest {
    private String awsAccessKey = System.getProperty("awsAccessKey");
    private String awsSecretKey = System.getProperty("awsSecretKey");
    private String awsBucketName = System.getProperty("awsBucketName");

    @Test
    // TODO: funktioniert noch nicht.
    //@Category(S3Test.class)    
    public void uploadFile_Ok() throws Exception {
    
        GradleVariable[] gvs = { GradleVariable.newGradleProperty("awsAccessKey", awsAccessKey), 
                GradleVariable.newGradleProperty("awsSecretKey", awsSecretKey),
                GradleVariable.newGradleProperty("awsBucketName", awsBucketName)};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/S3UploadFile", gvs);

    
    }
}
