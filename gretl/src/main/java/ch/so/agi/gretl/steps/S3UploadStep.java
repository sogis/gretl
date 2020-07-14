package ch.so.agi.gretl.steps;

import java.io.File;
import java.nio.file.Paths;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

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

    public void execute(String awsAccessKey, String awsSecretKey, String sourceObject, String bucketName, String s3EndPoint, String s3Region, String acl) {        
        log.lifecycle(String.format("Start S3UploadStep(Name: %s SourceObject: %s S3EndPoint: %s S3Region: %s ACL: %s)", taskName,
                sourceObject, bucketName, s3EndPoint, s3Region, acl));
        
        BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(s3EndPoint, s3Region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        int uploadedFiles = 0;
        
        File sourceObjectFile = new File(sourceObject);
        if (sourceObjectFile.isDirectory()) {
            File directoryPath = new File(sourceObject);
            String filesList[] = directoryPath.list();
            for(String fileName : filesList) {
                File file = Paths.get(sourceObject, fileName).toFile();
                s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                        .withCannedAcl(CannedAccessControlList.valueOf(acl)));
                uploadedFiles++;                
             }
        } else {
            s3client.putObject(new PutObjectRequest(bucketName, sourceObjectFile.getName(), sourceObjectFile)
                    .withCannedAcl(CannedAccessControlList.valueOf(acl)));
            uploadedFiles++;                            
        }

        log.lifecycle(taskName + ": " + uploadedFiles + " Files have been uploaded to: "+bucketName+".");
    }
}
