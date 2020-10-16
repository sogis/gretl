package ch.so.agi.gretl.tasks;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.net.ftp.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class FtpDownload extends DefaultTask {
    protected GretlLogger log;
    
    @Input
    public String server;
    @Input
    public String user;
    @Input
    public String password;
    @OutputDirectory
    public String localDir;
    @Input
    public String remoteDir;
    @Input
    @Optional
    public boolean passiveMode=true;
    @Input
    @Optional
    public long controlKeepAliveTimeout=300; // set timeout to 5 minutes
   
    @TaskAction
    void download()
    {
        log = LogEnvironment.getLogger(FtpDownload.class);
        
        FTPClient ftp = new FTPClient();

        //  ftp.configure(config);
        
        try {
            ftp.connect(server, 21);
            ftp.login(user, password);
            
            int reply = ftp.getReplyCode();
            if(!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("FTP server refused connection.");
            }
            
            if (!passiveMode) {
                ftp.enterLocalActiveMode();
            } else {
                ftp.enterLocalPassiveMode();
            }  
            
            if(controlKeepAliveTimeout>0) {
                ftp.setControlKeepAliveTimeout(controlKeepAliveTimeout); 
            }

            for (final FTPFile f : ftp.mlistDir(remoteDir)) {
                if(f.isFile()) {
                    String remoteFileName=f.getName();
                    String localFileName=remoteFileName;
                    java.io.File localFolder=this.getProject().file(localDir);
                    java.io.File localFile = new java.io.File(localFolder, localFileName);
                    FileOutputStream fos=null;
                    try {
                        fos = new FileOutputStream(localFile);

                        boolean downloadOk = ftp.retrieveFile(remoteFileName, fos);
                        if (downloadOk == false) {
                            throw new Exception("Could not retrieve file: " + remoteFileName);
                        }
                        log.info("File downloaded: " + localFile.getAbsolutePath());
                    }finally {
                        if(fos!=null) {
                            fos.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        } finally {
            if(ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch(IOException e) {
                    // do nothing
                }
            }
        }
        
    }
}
