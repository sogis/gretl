package ch.so.agi.gretl.api;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Parameters of a Web-Service or FTP-service
 */
public class Endpoint implements Serializable{

    private String url;
    private String user;
    private String password;

    public Endpoint(File dbUri) {
        this(dbUri.getPath(), null, null);
    }
    public Endpoint(String dbUri) {
        this(dbUri, null, null);
    }

    public Endpoint(String dbUri, String dbUser) {
        this(dbUri, dbUri, null);
    }

    public Endpoint(String dbUri, String dbUser, String dbPassword) {
        this.url = dbUri;
        this.user = dbUser;
        this.password = dbPassword;
    }


    public String toString() {
        int passLength = 0;
        if (password != null)
            passLength = password.length();

        char[] starArray = new char[passLength];
        Arrays.fill(starArray, "*".toCharArray()[0]);

        String res = String.format("Connection( Url: %s, User: %s Pass: %s)", url, user,
                new String(starArray));
        return res;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
    
    
}
