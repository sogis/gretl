package ch.so.agi.gretl.tasks;

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

    private String accessKey;
    private String secretKey;
    private Object sourceDir;
    private Object sourceFile;
    private Object sourceFiles;
    private String bucketName;
    private String endPoint = "https://s3.eu-central-1.amazonaws.com";
    private String region = "eu-central-1";
    private String acl = null;
    private String contentType = null;
    private Map<String,String> metaData = null;

    @Input
    public String getAccessKey() {
        return accessKey;
    }

    @Input
    public String getSecretKey() {
        return secretKey;
    }
    @InputDirectory
    @Optional
    public Object getSourceDir() {
        return sourceDir;
    }
    @Input
    @Optional
    public Object getSourceFile() {
        return sourceFile;
    }

    @Input
    @Optional
    public Object getSourceFiles() {
        return sourceFiles;
    }

    @Input
    public String getBucketName() {
        return bucketName;
    }

    @Input
    @Optional
    public String getEndPoint() {
        return endPoint;
    }

    @Input
    public String getRegion() {
        return region;
    }

    @Input
    public String getAcl() {
        return acl;
    }

    @Input
    @Optional
    public String getContentType() {
        return contentType;
    }

    @Input
    @Optional
    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setSourceDir(Object sourceDir) {
        this.sourceDir = sourceDir;
    }

    public void setSourceFile(Object sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setSourceFiles(Object sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setAcl(String acl) {
        this.acl = acl;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

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
            s3UploadStep.execute(accessKey, secretKey, sourceObject, bucketName, endPoint, region, acl, contentType, metaData);
        } catch (Exception e) {
            log.error("Exception in S3Upload task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
