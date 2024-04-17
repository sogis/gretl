package ch.so.agi.gretl.tasks;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.S3Bucket2BucketStep;
import ch.so.agi.gretl.util.TaskUtil;

public class S3Bucket2Bucket extends DefaultTask {
    protected GretlLogger log;

    private String accessKey;
    private String secretKey;
    private String sourceBucket;
    private String targetBucket;
    private String endPoint = "https://s3.eu-central-1.amazonaws.com";
    private String region = "eu-central-1";
    private String acl = null;
    private Map<String,String> metaData = new HashMap<String,String>();

    @Input
    public String getAccessKey() {
        return accessKey;
    }

    @Input
    public String getSecretKey() {
        return secretKey;
    }

    @Input
    public String getSourceBucket() {
        return sourceBucket;
    }

    @Input
    public String getTargetBucket() {
        return targetBucket;
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
    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setSourceBucket(String sourceBucket) {
        this.sourceBucket = sourceBucket;
    }

    public void setTargetBucket(String targetBucket) {
        this.targetBucket = targetBucket;
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

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    @TaskAction
    public void upload() {
        log = LogEnvironment.getLogger(S3Bucket2Bucket.class);

        if (accessKey == null) {
            throw new IllegalArgumentException("accessKey must not be null");
        }
        if (secretKey == null) {
            throw new IllegalArgumentException("secretKey must not be null");
        }
        if (sourceBucket == null) {
            throw new IllegalArgumentException("sourceBucket must not be null");
        }
        if (targetBucket == null) {
            throw new IllegalArgumentException("targetBucket must not be null");
        }        
        if (region == null) {
            throw new IllegalArgumentException("region must not be null");
        }                
        if (acl == null) {
            throw new IllegalArgumentException("acl must not be null");
        }
                
        try {
            S3Bucket2BucketStep s3Bucket2Bucket = new S3Bucket2BucketStep();
            s3Bucket2Bucket.execute(accessKey, secretKey, sourceBucket, targetBucket, endPoint, region, acl, metaData);
        } catch (Exception e) {
            log.error("Exception in S3Upload task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
