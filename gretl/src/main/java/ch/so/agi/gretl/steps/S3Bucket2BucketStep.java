package ch.so.agi.gretl.steps;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class S3Bucket2BucketStep {
    private GretlLogger log;
    private String taskName;

    public S3Bucket2BucketStep() {
        this(null);
    }
    
    public S3Bucket2BucketStep(String taskName) {
        if (taskName == null) {
            taskName = S3Bucket2BucketStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    public void execute(String accessKey, String secretKey, String sourceBucket, String targetBucket, String s3EndPoint, String s3Region, Map<String, String> metaData) throws FileNotFoundException {        
        log.lifecycle(String.format("Start S3UploadStep(Name: %s SourceBucket: %s TargetBucket: %s S3EndPoint: %s S3Region: %s MetaData: %s)", taskName,
                sourceBucket, targetBucket, s3EndPoint, s3Region, metaData));
        
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(s3EndPoint, s3Region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        int copiedFiles = 0;
        
        ObjectListing listing = s3client.listObjects(sourceBucket);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
            listing = s3client.listNextBatchOfObjects(listing);
            summaries.addAll(listing.getObjectSummaries());
        }

        for (S3ObjectSummary summary : summaries) {
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(sourceBucket, summary.getKey(), targetBucket, summary.getKey());
            copyObjRequest.setAccessControlList(s3client.getObjectAcl(sourceBucket, summary.getKey()));
            s3client.copyObject(copyObjRequest);
            copiedFiles++;
        }
        log.lifecycle(taskName + ": " + copiedFiles + " Files have been copied: "+sourceBucket+" -> "+targetBucket);
    }
}
