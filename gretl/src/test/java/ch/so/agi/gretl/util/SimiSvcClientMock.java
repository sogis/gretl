package ch.so.agi.gretl.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import ch.so.agi.gretl.steps.PublisherStep;
import ch.so.agi.gretl.util.publisher.PublicationLog;
import ch.so.agi.gretl.util.publisher.PublishedRegion;

public class SimiSvcClientMock implements SimiSvcApi {

    private String notifiedDataIdent=null;
    private Date notifiedPublishDate=null;
    private List<String> notifiedRegions=null;

    @Override
    public void setup(String endpoint, String usr, String pwd) {
    }
    @Override
    public void setupTokenService(String endpoint, String usr, String pwd) {
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
    public void notifyPublication(PublicationLog pub)
            throws IOException {
        notifiedDataIdent=pub.getDataIdent();
        try {
            notifiedPublishDate=PublisherStep.parsePublicationTimestamp(pub.getPublished());
        } catch (ParseException e) {
            throw new IOException(e);
        }
        if(pub.getPublishedRegions()!=null) {
            notifiedRegions=new java.util.ArrayList<String>();
            for(PublishedRegion pubRegion:pub.getPublishedRegions()) {
                notifiedRegions.add(pubRegion.getRegion());
            }
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
