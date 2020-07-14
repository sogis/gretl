package ch.so.agi.gretl.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.S3UploadStep;
import ch.so.agi.gretl.util.TaskUtil;

public class S3Upload extends DefaultTask {
    protected GretlLogger log;

    @Input
    public String awsAccessKey;
    
    @Input
    public String awsSecretKey;
        
    @InputDirectory
    public File sourceDir;
    
    @Input
    public File sourceFile;

    @Input
    public String bucketName;
    
    @Input
    @Optional
    public String endPoint = "https://s3.amazonaws.com/";
    
    @Input
    @Optional
    public String region = "eu-central-1";
    
    @Input
    public String acl;

    @TaskAction
    public void upload() {
        log = LogEnvironment.getLogger(S3Upload.class);

        if (awsAccessKey == null) {
            throw new IllegalArgumentException("awsAccessKey must not be null");
        }
        if (awsSecretKey == null) {
            throw new IllegalArgumentException("awsSecretKey must not be null");
        }
        if (sourceDir == null && sourceFile == null) {
            throw new IllegalArgumentException("either sourceDir or sourceFile must not be null");
        }
        if (bucketName == null) {
            throw new IllegalArgumentException("bucketName must not be null");
        }
        if (acl == null) {
            throw new IllegalArgumentException("acl must not be null");
        }
        
        File sourceObject;
        if(sourceDir != null) {
            sourceObject = sourceDir;
        } else {
            sourceObject = sourceFile;
        }
        
        try {
            S3UploadStep s3UploadStep = new S3UploadStep();
            s3UploadStep.execute(awsAccessKey, awsSecretKey, sourceObject.getAbsolutePath(), bucketName, endPoint, region, acl);
        } catch (Exception e) {
            log.error("Exception in DatabaseDocumentExport task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
