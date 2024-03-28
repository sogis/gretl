package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractFtpTask;
import ch.so.agi.gretl.util.TaskUtil;

public class FtpDelete extends AbstractFtpTask {
    private String remoteDir;
    private Object remoteFile;

    @Input
    public String getRemoteDir() {
        return remoteDir;
    }

    @Optional
    @Input
    public Object getRemoteFile() {
        return remoteFile;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    public void setRemoteFile(Object remoteFile) {
        this.remoteFile = remoteFile;
    }

    @TaskAction
    void download() {
        log = LogEnvironment.getLogger(FtpDelete.class);
        
        FTPClient ftp = null;

        try {
            ftp = setup();
            
            if(remoteFile == null) {
                for (final FTPFile f : ftp.listFiles(remoteDir)) {
                    if (f.isFile()) {
                        String remoteFileName = f.getName();
                        ftp.deleteFile(remoteDir + getFileSeparator() + remoteFileName);
                        //System.out.println("**** 1 DELETE REMOTE FILE: " + remoteDir + fileSeparator + remoteFileName);
                    }
                }
            } else {
                if (remoteFile instanceof String) {
                    String fileName = (String)remoteFile;
                    processFile(ftp, fileName);
                } else if (remoteFile instanceof FileCollection) {
                    Set<File> files = ((FileTree)remoteFile).getFiles();
                    for (File file : files) {
                        processFile(ftp, file.getName());
                    }
                } else if (remoteFile instanceof java.util.List) {
                    for (String fileName : (java.util.List<String>)remoteFile) {
                        processFile(ftp, fileName);
                    }
                } else {
                    throw new Exception("unexpected Argumenttype of remoteFile "+remoteFile.getClass());
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
    
    private void processFile(FTPClient ftp, String fileName) throws IOException, FileNotFoundException, Exception {
        if (fileName.contains("*") || fileName.contains("?")) {
            String remoteFilePattern = fileName;
            for (final FTPFile f : ftp.listFiles(remoteDir)) {
                if (f.isFile()) {
                    fileName = f.getName();
                    if (match(remoteFilePattern, fileName)) {
                        ftp.deleteFile(remoteDir + getFileSeparator() + fileName);
                        //System.out.println("**** 2 DELETE REMOTE FILE: " + remoteDir + fileSeparator + fileName);
                    }
                }
            }
        } else {
            ftp.deleteFile(remoteDir + getFileSeparator() + fileName);
            //System.out.println("**** 3 DELETE REMOTE FILE: " + remoteDir + fileSeparator + fileName);
        }
    }
}
