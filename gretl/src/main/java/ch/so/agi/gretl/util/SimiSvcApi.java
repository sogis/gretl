package ch.so.agi.gretl.util;

import java.io.IOException;
import java.util.List;

import ch.so.agi.gretl.util.publisher.PublicationLog;

public interface SimiSvcApi {
    void setup(String endpoint, String usr, String pwd);
    void setupTokenService(String endpoint, String usr, String pwd);
    String getAccessToken() throws IOException;
    String getLeaflet(String dataIdent, java.util.Date publishDate) throws IOException;
    void notifyPublication(PublicationLog pub)
            throws IOException;
}