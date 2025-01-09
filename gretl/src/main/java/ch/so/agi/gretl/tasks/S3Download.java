package ch.so.agi.gretl.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.S3DownloadStep;
import ch.so.agi.gretl.util.TaskUtil;

public class S3Download extends DefaultTask {
    protected GretlLogger log;

    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String key;
    private File downloadDir;
    private String endPoint = "https://s3.eu-central-1.amazonaws.com";
    private String region = "eu-central-1";
    
    @Input
    public String getAccessKey() {
        return accessKey;
    }
    @Input
    public String getSecretKey() {
        return secretKey;
    }
    @Input
    public String getBucketName() {
        return bucketName;
    }
    @Input
    public String getKey() {
        return key;
    }
    @OutputDirectory
    public File getDownloadDir() {
        return downloadDir;
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

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDownloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @TaskAction
    public void upload() {
        log = LogEnvironment.getLogger(S3Download.class);

        if (accessKey == null) {
            throw new IllegalArgumentException("accessKey must not be null");
        }
        if (secretKey == null) {
            throw new IllegalArgumentException("secretKey must not be null");
        }
        if (downloadDir == null) {
            throw new IllegalArgumentException("downloadDir must not be null");
        }
        if (bucketName == null) {
            throw new IllegalArgumentException("bucketName must not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (region == null) {
            throw new IllegalArgumentException("region must not be null");
        }        
                
        try {
            S3DownloadStep s3DownloadStep = new S3DownloadStep();
            s3DownloadStep.execute(accessKey, secretKey, bucketName, key, endPoint, region, downloadDir);
        } catch (Exception e) {
            log.error("Exception in S3Download task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
