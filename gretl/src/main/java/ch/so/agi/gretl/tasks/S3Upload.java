package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    public String accessKey;
    
    @Input
    public String secretKey;
        
    @InputDirectory
    public Object sourceDir;
    
    @Input
    public Object sourceFile;
    
    @Input 
    public Object sourceFiles;

    @Input
    public String bucketName;
    
    @Input
    @Optional
    public String endPoint = "https://s3.amazonaws.com/";
    
    @Input
    public String region = "eu-central-1";
    
    @Input
    @Optional    
    public String acl = null;
    
    @Input
    @Optional        
    public Map<String,String> metaData = null;
    
    @TaskAction
    public void upload() {
        log = LogEnvironment.getLogger(S3Upload.class);

        if (accessKey == null) {
            throw new IllegalArgumentException("accessKey must not be null");
        }
        if (secretKey == null) {
            throw new IllegalArgumentException("secretKey must not be null");
        }
        if (sourceDir == null && sourceFile == null && sourceFiles == null) {
            throw new IllegalArgumentException("either sourceDir or sourceFile or sourceFiles must not be null");
        }
        if (bucketName == null) {
            throw new IllegalArgumentException("bucketName must not be null");
        }
        if (region == null) {
            throw new IllegalArgumentException("region must not be null");
        }        
        if (acl == null) {
            throw new IllegalArgumentException("acl must not be null");
        }
        if (metaData == null) {
            metaData = new HashMap<String,String>();
        }
        
        Object sourceObject;
        if(sourceDir != null) {
            sourceObject = sourceDir;
        } else if (sourceFiles != null) {
            sourceObject = sourceFiles;
        } else {
            sourceObject = sourceFile;
        }
        
        try {
            S3UploadStep s3UploadStep = new S3UploadStep();
            s3UploadStep.execute(accessKey, secretKey, sourceObject, bucketName, endPoint, region, acl, metaData);
        } catch (Exception e) {
            log.error("Exception in S3Upload task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
