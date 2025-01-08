package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class GzipStep {
    private GretlLogger log;
    private String taskName;

    public GzipStep() {
        this(null);
    }
    
    public GzipStep(String taskName) {
        if (taskName == null) {
            this.taskName = GzipStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    public void execute(File dataFile, File gzipFile) throws IOException {        
        FileInputStream fis = new FileInputStream(dataFile);
        FileOutputStream fos = new FileOutputStream(gzipFile);
        GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
        byte[] buffer = new byte[1024];
        int len;
        while((len=fis.read(buffer)) != -1){
            gzipOS.write(buffer, 0, len);
        }
        gzipOS.close();
        fos.close();
        fis.close();
    }
}
