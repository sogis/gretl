package ch.so.agi.gretl.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.*;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractFtpTask;
import ch.so.agi.gretl.util.TaskUtil;

public class FtpList extends AbstractFtpTask {
    private String remoteDir;

    public List<String> files;

    /**
     * Verzeichnis auf dem Server.
     */
    @Input
    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    @TaskAction
    void list() {
        log = LogEnvironment.getLogger(FtpList.class);
        
        FTPClient ftp = null;
        
        files=new ArrayList<String>();
        
        try {
            ftp = setup();
            
            for (final FTPFile f : ftp.listFiles(remoteDir)) {
                if(f.isFile()) {
                    String remoteFileName=f.getName();
                    files.add(remoteFileName);
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
}
