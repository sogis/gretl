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
    private String server;
    private String user;
    private String password;
    private String systemType=FTPClientConfig.SYST_UNIX;
    private String fileSeparator=null;
    private Boolean passiveMode=true;
    private Long controlKeepAliveTimeout=300L; // set timeout to 5 minutes

    /**
     * Name des Servers (ohne ftp://).
     */
    @Input
    public String getServer(){
        return server;
    }

    /**
     * Benutzername auf dem Server.
     */
    @Input
    public String getUser(){
       return user;
    }

    /**
     * Passwort für den Zugriff auf dem Server.
     */
    @Input
    public String getPassword(){
        return this.password;
    }

    /**
     * `UNIX` oder `WINDOWS`. Default: `UNIX`.
     */
    @Input
    @Optional
    public String getSystemType(){
        return systemType;
    }

    /**
     * Default: `/`. (Falls systemType Windows ist, ist der Default `\`.
     */
    @Input
    @Optional
    public String getFileSeparator(){
        return this.fileSeparator;
    }

    /**
     * Aktiv- oder Passiv-Verbindungsmodus. Default: Passiv (true)
     */
    @Input
    @Optional
    public Boolean getPassiveMode(){
        return passiveMode;
    }

    /**
     * Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default: 300s (=5 Minuten)
     */
    @Input
    @Optional
    public Long getControlKeepAliveTimeout(){
        return controlKeepAliveTimeout;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public void setFileSeparator(String fileSeparator) {
        this.fileSeparator = fileSeparator;
    }

    public void setPassiveMode(Boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public void setControlKeepAliveTimeout(Long controlKeepAliveTimeout) {
        this.controlKeepAliveTimeout = controlKeepAliveTimeout;
    }

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
