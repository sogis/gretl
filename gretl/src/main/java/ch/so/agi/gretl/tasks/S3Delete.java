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
import ch.so.agi.gretl.steps.S3DeleteStep;
import ch.so.agi.gretl.steps.S3DownloadDirectoryStep;
import ch.so.agi.gretl.steps.S3DownloadStep;
import ch.so.agi.gretl.util.TaskUtil;

public class S3Delete extends DefaultTask {
    protected GretlLogger log;

    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String key;
    private String endPoint = "https://s3.eu-central-1.amazonaws.com";
    private String region = "eu-central-1";
    
    /**
     * AccessKey
     */
    @Input
    public String getAccessKey() {
        return accessKey;
    }
    
    /**
     * SecretKey
     */
    @Input
    public String getSecretKey() {
        return secretKey;
    }
    
    /**
     * Name des Buckets, in die Datei oder sämtliche Dateie gelöscht werden sollen.
     */
    @Input
    public String getBucketName() {
        return bucketName;
    }
    
    /**
     * Name der Datei, die gelöscht werden soll. Wird kein Key definiert, wird der Inhalt des gesamten Buckets gelöscht.
     */
    @Input
    @Optional
    public String getKey() {
        return key;
    }
        
    /**
     * S3-Endpunkt. Default: `https://s3.eu-central-1.amazonaws.com/`
     */
    @Input
    @Optional
    public String getEndPoint() {
        return endPoint;
    }
    
    /**
     * S3-Region. Default: `eu-central-1`
     */
    @Input
    @Optional
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

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @TaskAction
    public void upload() {
        log = LogEnvironment.getLogger(S3Delete.class);

        if (accessKey == null) {
            throw new IllegalArgumentException("accessKey must not be null");
        }
        if (secretKey == null) {
            throw new IllegalArgumentException("secretKey must not be null");
        }
        if (bucketName == null) {
            throw new IllegalArgumentException("bucketName must not be null");
        }
        if (region == null) {
            throw new IllegalArgumentException("region must not be null");
        }        
                
        try {
            S3DeleteStep s3DeleteStep = new S3DeleteStep();
            s3DeleteStep.execute(accessKey, secretKey, bucketName, key, endPoint, region);
        } catch (Exception e) {
            log.error("Exception in S3Delete task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
