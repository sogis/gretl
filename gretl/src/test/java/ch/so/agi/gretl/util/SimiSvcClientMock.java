package ch.so.agi.gretl.util;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class SimiSvcClientMock implements SimiSvcApi {

    private String notifiedDataIdent=null;
    private Date notifiedPublishDate=null;
    private List<String> notifiedRegions=null;

    @Override
    public void setup(String endpoint, String usr, String pwd) {
    }

    @Override
    public String getAccessToken() throws IOException {
        return null;
    }

    @Override
    public String getLeaflet(String dataIdent, Date publishDate) throws IOException {
        return "<html lang=\"de\">\r\n" + 
                "  <head>\r\n" + 
                "    <meta charset=\"utf-8\">\r\n" + 
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + 
                "    <title>Datadoc</title>\r\n" + 
                "  </head>\r\n" + 
                "  <body>\r\n" + 
                "    <h1>Datadoc von "+dataIdent+"</h1>\r\n" + 
                "  </body>\r\n" + 
                "</html>";
    }

    @Override
    public void notifyPublication(String dataIdent, Date publishDate, List<String> publishedRegions)
            throws IOException {
        notifiedDataIdent=dataIdent;
        notifiedPublishDate=new Date(publishDate.getTime());
        if(publishedRegions!=null) {
            notifiedRegions=new java.util.ArrayList<String>(publishedRegions);
        }
    }

    public String getNotifiedDataIdent() {
        return notifiedDataIdent;
    }
    public Date getNotifiedPublishDate() {
        return notifiedPublishDate;
    }
    public List<String> getNotifiedRegions() {
        return notifiedRegions;
    }

}
