package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
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
    private File sourceDir;
    private File sourceFile;
    private FileCollection sourceFiles;
    private String bucketName;
    private String endPoint = "https://s3.eu-central-1.amazonaws.com";
    private String region = "eu-central-1";
    private String acl = null;
    private String contentType = null;
    private Map<String,String> metaData = null;

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
     * Verzeichnis mit den Dateien, die hochgeladen werden sollen.
     */
    @InputDirectory
    @Optional
    public File getSourceDir() {
        return sourceDir;
    }
    
    /**
     * Datei, die hochgeladen werden soll.
     */
    @InputFile
    @Optional
    public File getSourceFile() {
        return sourceFile;
    }

    /**
     * `FileCollection` mit den Dateien, die hochgeladen werden sollen, z.B. `fileTree("/path/to/directoy/") { include "*.itf" }`
     */
    @InputFiles
    @Optional
    public FileCollection getSourceFiles() {
        return sourceFiles;
    }

    /**
     * Name des Buckets, in dem die Dateien gespeichert werden sollen.
     */
    @Input
    public String getBucketName() {
        return bucketName;
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
     * S3-Region. `Default: eu-central-1`
     */
    @Input
    public String getRegion() {
        return region;
    }

    /**
     * Access Control Layer `[private, public-read, public-read-write, authenticated-read, aws-exec-read, bucket-owner-read, bucket-owner-full-control]`
     */
    @Input
    public String getAcl() {
        return acl;
    }

    /**
     * Content-Type
     */
    @Input
    @Optional
    public String getContentType() {
        return contentType;
    }

    /**
     * Metadaten des Objektes resp. der Objekte, z.B. `["lastModified":"2020-08-28"]`
     */
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

    public void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setSourceFiles(FileCollection sourceFiles) {
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
            List<File> files = new ArrayList<>();
            for (File fileObj : sourceFiles) {
                files.add(fileObj);
            }
            sourceObject = files;
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
