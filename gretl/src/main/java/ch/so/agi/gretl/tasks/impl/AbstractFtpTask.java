package ch.so.agi.gretl.tasks.impl;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import ch.so.agi.gretl.logging.GretlLogger;

public class AbstractFtpTask extends DefaultTask {
    protected GretlLogger log;
    
    @Input
    public String server;
    @Input
    public String user;
    @Input
    public String password;
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
   
    protected FTPClient setup() throws SocketException, IOException, Exception {
        FTPClient ftp;
        ftp = new FTPClient();

        FTPClientConfig config=new FTPClientConfig(systemType);
        ftp.configure(config);
        
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
        return ftp;
    }
    protected boolean match(String pattern, String str){
        return org.apache.tools.ant.types.selectors.SelectorUtils.match(pattern, str);    
    }
}
