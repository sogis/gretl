package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

public class S3DeleteStep {
    private GretlLogger log;
    private String taskName;

    public S3DeleteStep() {
        this(null);
    }
    
    public S3DeleteStep(String taskName) {
        if (taskName == null) {
            this.taskName = S3DeleteStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    public void execute(String accessKey, String secretKey, String bucketName, String key, String s3EndPoint, String s3Region) throws URISyntaxException, IOException {        
        log.lifecycle(String.format("Start S3DeleteStep(Name: %s BucketName: %s Key: %s S3EndPoint: %s S3Region: %s)", taskName,
                 bucketName, key, s3EndPoint, s3Region));
        
        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        Region region = Region.of(s3Region);
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3EndPoint))
                .build(); 
        
        if (key == null) {
            deleteAllFiles(s3client, bucketName);
        } else {
            deleteSingleFile(s3client, bucketName, key);
        }
    }

    public void deleteSingleFile(S3Client s3Client, String bucketName, String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.lifecycle("Deleted file: " + key);
    }        
    
    public void deleteAllFiles(S3Client s3Client, String bucketName) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response listResponse;

        do {
            listResponse = s3Client.listObjectsV2(listRequest);

            List<ObjectIdentifier> identifiers = listResponse.contents().stream()
                    .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                    .collect(Collectors.toList());

            if (!identifiers.isEmpty()) {
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder().objects(identifiers).build())
                        .build();

                s3Client.deleteObjects(deleteRequest);
                log.lifecycle("Deleted batch of " + identifiers.size() + " objects.");
            }

            listRequest = listRequest.toBuilder()
                    .continuationToken(listResponse.nextContinuationToken())
                    .build();

        } while (listResponse.isTruncated());

        log.lifecycle("All files deleted in bucket: " + bucketName);
    }

}
