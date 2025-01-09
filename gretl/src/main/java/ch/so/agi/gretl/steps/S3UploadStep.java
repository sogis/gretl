package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3UploadStep {
    private GretlLogger log;
    private String taskName;

    public S3UploadStep() {
        this(null);
    }
    
    public S3UploadStep(String taskName) {
        if (taskName == null) {
            this.taskName = S3UploadStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    public void execute(String accessKey, String secretKey, Object sourceObject, String bucketName, String s3EndPoint, String s3Region, String acl, String contentType, Map<String, String> metaData) throws FileNotFoundException, URISyntaxException {        
        log.lifecycle(String.format("Start S3UploadStep(Name: %s SourceObject: %s BucketName: %s S3EndPoint: %s S3Region: %s ACL: %s ContentType: %s MetaData: %s)", taskName,
                sourceObject.toString(), bucketName, s3EndPoint, s3Region, acl, contentType, metaData));
        
        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        Region region = Region.of(s3Region);
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3EndPoint))
                .build(); 
        
        int uploadedFiles = 0;
        
        List<File> files = new ArrayList<File>();
        if (sourceObject instanceof File) {
            File sourceObjectFile = (File) sourceObject;
            if (sourceObjectFile.isDirectory()) {
                String filesList[] = sourceObjectFile.list();
                for(String fileName : filesList) {
                    File file = Paths.get(sourceObjectFile.getAbsolutePath(), fileName).toFile();
                    if (file.isDirectory()) {
                        continue;
                    }
                    files.add(file);
                }
            } else {
                files.add(sourceObjectFile);
            }
        } else { 
            List<File> fileList = (ArrayList<File>) sourceObject;
            for (File fileObj : fileList) {
                files.add(fileObj);
            }
        }
        
        for (File s3file : files) {            
            software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder();
            requestBuilder
                .bucket(bucketName)
                .key(s3file.getName())
                .metadata(metaData)
                .contentLength(s3file.length());
            
            if (contentType != null) {
                requestBuilder.contentType(contentType);
            }

            s3client.putObject(requestBuilder.build(), s3file.toPath());
            
            ObjectCannedACL aclObj = ObjectCannedACL.fromValue(acl);            
            s3client.putObjectAcl(PutObjectAclRequest.builder().bucket(bucketName).key(s3file.getName()).acl(aclObj.toString()).build());
            uploadedFiles++;
        }
       
        log.lifecycle(taskName + ": " + uploadedFiles + " Files have been uploaded to: "+bucketName+".");
    }
}
