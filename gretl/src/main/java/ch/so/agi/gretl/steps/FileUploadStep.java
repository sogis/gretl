package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class FileUploadStep {
    private GretlLogger log;
    private String taskName;

    public FileUploadStep() {
        this(null);
    }
    
    public FileUploadStep(String taskName) {
        if (taskName == null) {
            this.taskName = XslTransformerStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    // Es braucht wohl sowas wie ne List mit Paaren (key/value) damit man es generisch machen kann. 
    
    public void formUpload(File uploadFile, String username, String password, String serverUrl) throws IOException, SaxonApiException {
        log.lifecycle(String.format("Start FileUploadStep(Name: %s UploadFile: %s Username: %s Password: %s ServerUrl %s)", taskName,
                uploadFile, username, "********", serverUrl));

    }
    
    // public void binaryUpload()


}
