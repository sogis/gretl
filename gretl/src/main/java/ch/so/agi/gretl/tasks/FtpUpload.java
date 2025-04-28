package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractFtpTask;
import ch.so.agi.gretl.util.TaskUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FtpUpload extends AbstractFtpTask {
    private File localFile;
    private String remoteDir;
    private String fileType="ASCII";
    
    /**
     * Lokale Datei, die auf den FTP-Server hochgeladen werden soll.
     */
    @InputFile
    public File getLocalFile() {
        return localFile;
    }

    /**
     * Verzeichnis auf dem FTP-Server, in dem die lokale Datei gespeichert werden soll.
     */
    @Input
    public String getRemoteDir() {
        return remoteDir;
    }
    
    /**
     * `ASCII` oder `BINARY`. Default: `ASCII`.
     */
    @Input
    @Optional
    public String getFileType() {
        return fileType;
    }
    
    public void setLocalFile(File localFile) {
        this.localFile = localFile;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    @TaskAction
    public void upload() {
        log = LogEnvironment.getLogger(FtpUpload.class);

        FTPClient ftp = null;

        try {
            ftp = setup();
            
            if (fileType.equalsIgnoreCase("ASCII")) {
                ftp.setFileType(FTP.ASCII_FILE_TYPE);
            } else {
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
            }
            
            try (InputStream inputStream = new FileInputStream(localFile)) {
                String remotePath = remoteDir + getFileSeparator() + localFile.getName();
                //System.out.println("remotePath: " + remotePath);
                boolean done = ftp.storeFile(remotePath, inputStream);
                if (done) {
                    return;
                } else {
                    throw new GradleException("Could not upload the file.");
                }
            } 
        } catch (Exception e) {
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        } finally {
            if (ftp != null && ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch(IOException e) {
                    // do nothing
                }
                ftp = null;
            }
        }
    }

}
