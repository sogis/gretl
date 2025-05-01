package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

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
            this.taskName = S3DownloadStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    public void execute(String accessKey, String secretKey, String bucketName, String s3EndPoint, String s3Region, File downloadDir) throws URISyntaxException, IOException {        
        log.lifecycle(String.format("Start S3DownloadStep(Name: %s BucketName: %s S3EndPoint: %s S3Region: %s DownloadDir %s)", taskName,
                 bucketName, s3EndPoint, s3Region, downloadDir));

    }
}
