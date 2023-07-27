package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3DownloadStep {
    private GretlLogger log;
    private String taskName;

    public S3DownloadStep() {
        this(null);
    }
    
    public S3DownloadStep(String taskName) {
        if (taskName == null) {
            taskName = S3DownloadStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    public void execute(String accessKey, String secretKey, String bucketName, String key, String s3EndPoint, String s3Region, File downloadDir) throws URISyntaxException, IOException {        
        log.lifecycle(String.format("Start S3DownloadStep(Name: %s BucketName: %s Key: %s S3EndPoint: %s S3Region: %s DownloadDir %s)", taskName,
                 bucketName, key, s3EndPoint, s3Region, downloadDir));
        
        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        Region region = Region.of(s3Region);
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3EndPoint))
                .build(); 
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> ris = s3client.getObject(getObjectRequest);
        File targetFile = Paths.get(downloadDir.getAbsolutePath(), key.substring(key.lastIndexOf("/")+1)).toFile();
        
        Files.copy(ris, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        log.lifecycle(taskName + ": File has been downloaded to: "+targetFile.getAbsolutePath()+".");
    }
}
