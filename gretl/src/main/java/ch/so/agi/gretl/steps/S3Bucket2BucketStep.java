package ch.so.agi.gretl.steps;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectAclRequest;
import software.amazon.awssdk.services.s3.model.GetObjectAclResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.S3Object;

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
    
    public void execute(String accessKey, String secretKey, String sourceBucket, String targetBucket, String s3EndPoint, String s3Region, String acl, Map<String, String> metaData) throws FileNotFoundException, UnsupportedEncodingException {        
        log.lifecycle(String.format("Start S3UploadStep(Name: %s SourceBucket: %s TargetBucket: %s S3EndPoint: %s S3Region: %s ACL: %s MetaData: %s)", taskName,
                sourceBucket, targetBucket, s3EndPoint, s3Region, acl, metaData));
        
        AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        Region region = Region.of(s3Region);
        S3Client s3client = S3Client.builder()
                .credentialsProvider(creds)
                .region(region)
                .endpointOverride(URI.create(s3EndPoint))
                .build(); 
        
        int copiedFiles = 0;

        ObjectCannedACL aclObj = ObjectCannedACL.fromValue(acl);            

        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(sourceBucket)
                .build();

        ListObjectsResponse res = s3client.listObjects(listObjects);
        List<S3Object> objects = res.contents();

        List<String> keyList = new ArrayList<String>();
        for (ListIterator<S3Object> iterVals = objects.listIterator(); iterVals.hasNext(); ) {
            S3Object myObject = iterVals.next();        

            String encodedUrl = encodedUrl = URLEncoder.encode(sourceBucket + "/" + myObject.key(), StandardCharsets.UTF_8.toString());
            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .copySource(encodedUrl)
                    .destinationBucket(targetBucket)
                    .destinationKey(myObject.key())
                    .acl(aclObj)
                    .build();

            CopyObjectResponse copyRes = s3client.copyObject(copyReq);
            
            copiedFiles++;
         }

        log.lifecycle(taskName + ": " + copiedFiles + " Files have been copied: "+sourceBucket+" -> "+targetBucket);
    }
}
