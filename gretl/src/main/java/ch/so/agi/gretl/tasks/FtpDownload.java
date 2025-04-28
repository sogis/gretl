package ch.so.agi.gretl.tasks;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.net.ftp.*;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractFtpTask;
import ch.so.agi.gretl.util.TaskUtil;

public class FtpDownload extends AbstractFtpTask {
    private File localDir;
    private String remoteDir;
    private Object remoteFile = null;
    private String fileType="ASCII";

    /**
     * Lokales Verzeichnis, in dem die Dateien gespeichert werden.
     */
    @OutputDirectory
    public File getLocalDir() {
        return localDir;
    }
    
    /**
     * Verzeichnis auf dem Server.
     */
    @Input
    public String getRemoteDir() {
        return remoteDir;
    }

    /**
     * Dateiname oder Liste der Dateinamen auf dem Server (kann auch ein Muster sein (* oder ?)). Ohne diesen Parameter werden alle Dateien aus dem Remoteverzeichnis heruntergeladen.
     */
    @Input
    @Optional
    public Object getRemoteFile() {
        return remoteFile;
    }

    /**
     * `ASCII` oder `BINARY`. Default: `ASCII`.
     */
    @Input
    @Optional
    public String getFileType() {
        return fileType;
    }

    public void setLocalDir(File localDir) {
        this.localDir = localDir;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    public void setRemoteFile(Object remoteFile) {
        this.remoteFile = remoteFile;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @TaskAction
    public void download() {
        log = LogEnvironment.getLogger(FtpDownload.class);
        
        FTPClient ftp = null;
        
        try {
            ftp = setup();

            if (fileType.equalsIgnoreCase("ASCII")) {
                ftp.setFileType(FTP.ASCII_FILE_TYPE);
            } else {
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
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
            if(ftp!=null && ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch(IOException e) {
                    // do nothing
                }
                ftp=null;
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
        String remotePath = remoteDir+getFileSeparator()+remoteFileName;
        String localFileName = remoteFileName;
        File localFolder = this.getProject().file(localDir);
        File localFile = new File(localFolder, localFileName);
        FileOutputStream fos=null;
        try {
            fos = new FileOutputStream(localFile);

            boolean downloadOk = ftp.retrieveFile(remotePath, fos);
            if (downloadOk == false) {
                throw new Exception("Could not retrieve file: " + remotePath);
            }
            log.info("File downloaded: " + localFile.getAbsolutePath());
        } finally {
            if(fos!=null) {
                fos.close();
            }
        }
    }
}
