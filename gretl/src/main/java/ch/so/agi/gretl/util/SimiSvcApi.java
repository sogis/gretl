package ch.so.agi.gretl.util;

import java.io.IOException;
import java.util.List;

public interface SimiSvcApi {
    void setup(String endpoint, String usr, String pwd);
    String getAccessToken() throws IOException;
    String getLeaflet(String dataIdent, java.util.Date publishDate) throws IOException;
    void notifyPublication(String dataIdent, java.util.Date publishDate, List<String> publishedRegions)
            throws IOException;
}