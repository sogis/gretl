package ch.so.agi.gretl.testutil;

import ch.so.agi.gretl.steps.S3UploadStep;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

public class S3TestHelper {
    private static final DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:0.11.2");
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3Region;
    private final URI s3Endpoint;

    public S3TestHelper(String s3AccessKey, String s3SecretKey, String s3Region, String s3Endpoint) {
        Objects.requireNonNull(s3AccessKey);
        Objects.requireNonNull(s3SecretKey);
        Objects.requireNonNull(s3Region);
        Objects.requireNonNull(s3Endpoint);
        this.s3AccessKey = s3AccessKey;
        this.s3SecretKey = s3SecretKey;
        this.s3Region = s3Region;
        this.s3Endpoint = URI.create(s3Endpoint);
    }

    public static DockerImageName getLocalstackImage() {
        return localstackImage;
    }

    /**
     * Upload files from a directory
     * @param sourceObject directory for S3 bucket data on the local system
     * @param metadata metadata
     */
    public void upload(
            File sourceObject,
            Map<String, String> metadata,
            String s3BucketName,
            String acl
    ) throws FileNotFoundException, URISyntaxException {
        S3UploadStep s3UploadStep = new S3UploadStep();
        s3UploadStep.execute(s3AccessKey, s3SecretKey, sourceObject, s3BucketName, s3Endpoint.toString(), s3Region, acl, null, metadata);
    }

    public S3Client getS3Client() {
        return S3Client.builder()
                .credentialsProvider(getCredentialsProvider())
                .region(getRegion())
                .endpointOverride(s3Endpoint)
                .build();
    }

    public void createBucketIfNotExists(S3Client client, String bucketName) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    private AwsCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3AccessKey, s3SecretKey)
        );
    }

    private Region getRegion() {
        return Region.of(s3Region);
    }
}
