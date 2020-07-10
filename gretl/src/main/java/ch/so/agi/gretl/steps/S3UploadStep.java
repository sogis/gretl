package ch.so.agi.gretl.steps;

import com.amazonaws.services.s3.model.CannedAccessControlList;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class S3UploadStep {
    private GretlLogger log;
    private String taskName;

    public S3UploadStep() {
        this(null);
    }
    
    public S3UploadStep(String taskName) {
        if (taskName == null) {
            taskName = DatabaseDocumentExportStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    public void execute(String awsAccessKeyAda, String awsSecretKey, String sourceDir, String bucketName, String s3EndPoint, String s3Region, String acl) {
        System.out.println("sourceDir: " + sourceDir);
        
        
        CannedAccessControlList cacl = CannedAccessControlList.valueOf(acl);
        System.out.println(cacl.toString());
        
    }
}
