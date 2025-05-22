package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

// Noch bewusst getrennt vom bestehenden S3DownloadStep. Task sollte es nur einen geben.
// Ggf kann man die beiden Steps auch zusammenführen.
public class S3DownloadDirectoryStep {
    private GretlLogger log;
    private String taskName;

    public S3DownloadDirectoryStep() {
        this(null);
    }
    
    public S3DownloadDirectoryStep(String taskName) {
        if (taskName == null) {
            this.taskName = S3DownloadDirectoryStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    public void execute(String accessKey, String secretKey, String bucketName, String s3EndPoint, String s3Region, File downloadDir) throws URISyntaxException, IOException {        
        log.lifecycle(String.format("Start S3DownloadDirectoryStep(Name: %s BucketName: %s S3EndPoint: %s S3Region: %s DownloadDir %s)", taskName,
                 bucketName, s3EndPoint, s3Region, downloadDir));

        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        Region region = Region.of(s3Region);
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3EndPoint))
                .build(); 

        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response listRes;

            do {
                listRes = s3client.listObjectsV2(listReq);

                for (S3Object object : listRes.contents()) {
                    String key = object.key();
                    log.lifecycle("Downloading: " + key);
                    downloadObject(s3client, bucketName, key, downloadDir.getAbsolutePath());
                }

                // If there are more objects to retrieve
                listReq = listReq.toBuilder()
                        .continuationToken(listRes.nextContinuationToken())
                        .build();

            } while (listRes.isTruncated());

        } finally {
            s3client.close();
        }
    }
    
    private static void downloadObject(S3Client s3, String bucketName, String key, String downloadDir) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        File localFile = new File(Paths.get(downloadDir, key).toString());

        try (ResponseInputStream<GetObjectResponse> s3Object = s3.getObject(getObjectRequest);
             OutputStream outputStream = new FileOutputStream(localFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = s3Object.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
