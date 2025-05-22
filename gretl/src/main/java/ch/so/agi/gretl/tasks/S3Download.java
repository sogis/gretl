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
import ch.so.agi.gretl.steps.S3DownloadDirectoryStep;
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
     * Name des Buckets, in dem die Datei gespeichert ist.
     */
    @Input
    public String getBucketName() {
        return bucketName;
    }
    
    /**
     * Name der Datei. Wird kein Key definiert, wird der Inhalt des gesamten Buckets heruntergeladen.
     */
    @Input
    @Optional
    public String getKey() {
        return key;
    }
    
    /**
     * Verzeichnis, in das die Datei heruntergeladen werden soll.
     */
    @OutputDirectory
    public File getDownloadDir() {
        return downloadDir;
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
        if (region == null) {
            throw new IllegalArgumentException("region must not be null");
        }        
                
        try {
            if (key == null) {
                S3DownloadDirectoryStep s3DownloadDirectoryStep = new S3DownloadDirectoryStep();
                s3DownloadDirectoryStep.execute(accessKey, secretKey, bucketName, endPoint, region, downloadDir);
            } else {
                S3DownloadStep s3DownloadStep = new S3DownloadStep();
                s3DownloadStep.execute(accessKey, secretKey, bucketName, key, endPoint, region, downloadDir);                
            }
        } catch (Exception e) {
            log.error("Exception in S3Download task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
