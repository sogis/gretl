package ch.so.agi.gretl.tasks;
import java.io.FileNotFoundException;
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
    public Object remoteFile=null;
    @Input
    @Optional
    public String systemType=FTPClientConfig.SYST_UNIX;
    @Input
    @Optional
    public String fileSeparator=null;
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

        FTPClientConfig config=new FTPClientConfig(systemType);
        ftp.configure(config);
        
        try {
            ftp.connect(server, 21);
            
            ftp.login(user, password);
            
            int reply = ftp.getReplyCode();
            if(!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("FTP server refused connection.");
            }
            
            log.debug("systemType "+ftp.getSystemType());

            //if(ftp.features()){
            //    log.debug("features "+ftp.getReplyString()); 
            //}
            
            if (!passiveMode) {
                ftp.enterLocalActiveMode();
            } else {
                ftp.enterLocalPassiveMode();
            }  
            
            if(controlKeepAliveTimeout>0) {
                ftp.setControlKeepAliveTimeout(controlKeepAliveTimeout); 
            }

            if(fileSeparator==null) {
                fileSeparator=systemType.equalsIgnoreCase(FTPClientConfig.SYST_NT)?"\\":"/";
            }

            if(remoteFile==null) {
                for (final FTPFile f : ftp.listFiles(remoteDir)) {
                    if(f.isFile()) {
                        String remoteFileName=f.getName();
                        downloadFile(ftp, remoteFileName);
                    }
                }
                
            }else {
                if(remoteFile instanceof String) {
                    String fileName=(String)remoteFile;
                    processFile(ftp, fileName);
                }else if(remoteFile instanceof java.util.List) {
                    for(String fileName:(java.util.List<String>)remoteFile) {
                        processFile(ftp, fileName);
                    }
                }else {
                    throw new Exception("unexpected Argumenttype of remoteFile "+remoteFile.getClass());
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
    private void processFile(FTPClient ftp, String fileName) throws IOException, FileNotFoundException, Exception {
        if(fileName.contains("*") || fileName.contains("?")) {
            String remoteFilePattern=fileName;
            for (final FTPFile f : ftp.listFiles(remoteDir)) {
                if(f.isFile()) {
                    fileName=f.getName();
                    if(match(remoteFilePattern,fileName)) {
                        downloadFile(ftp, fileName);
                    }
                }
            }
        }else {
            downloadFile(ftp, fileName);
        }
    }
    private void downloadFile(FTPClient ftp, String remoteFileName)
            throws FileNotFoundException, IOException, Exception {
        String remotePath=remoteDir+fileSeparator+remoteFileName;
        String localFileName=remoteFileName;
        java.io.File localFolder=this.getProject().file(localDir);
        java.io.File localFile = new java.io.File(localFolder, localFileName);
        FileOutputStream fos=null;
        try {
            fos = new FileOutputStream(localFile);

            boolean downloadOk = ftp.retrieveFile(remotePath, fos);
            if (downloadOk == false) {
                throw new Exception("Could not retrieve file: " + remotePath);
            }
            log.info("File downloaded: " + localFile.getAbsolutePath());
        }finally {
            if(fos!=null) {
                fos.close();
            }
        }
    }
    private boolean match(String pattern, String str){
        return org.apache.tools.ant.types.selectors.SelectorUtils.match(pattern, str);    
    }
}
