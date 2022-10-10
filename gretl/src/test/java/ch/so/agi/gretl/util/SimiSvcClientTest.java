package ch.so.agi.gretl.util;

import org.junit.Ignore;
import org.junit.Test;

import ch.so.agi.gretl.util.publisher.PublicationLog;
import ch.so.agi.gretl.util.publisher.PublishedRegion;

@Ignore
public class SimiSvcClientTest {
    @Test
    public void getLeaflet() throws Exception {
        SimiSvcApi svc=new SimiSvcClient();
        svc.setup("http://localhost:8080/simi-svc/rest",null,null);
        String leaflet=svc.getLeaflet("ch.so.agi.mopublic", new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2022-03-08"));
        System.err.println(leaflet);
    }
    @Test
    public void getAccessToken() throws Exception {
        SimiSvcApi svc=new SimiSvcClient();
        svc.setup("http://localhost:8080/simi-svc/rest","admin","admin");
        String token=svc.getAccessToken();
        System.err.println(token);
    }
    @Test
    public void notifyPublication() throws Exception {
        SimiSvcApi svc=new SimiSvcClient();
        svc.setup("http://localhost:8080/simi-svc/rest",null,null);
        svc.notifyPublication(new PublicationLog("ch.so.agi.mopublic", new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2022-03-08")));
    }
    @Test
    public void notifyPublicationOAuth() throws Exception {
        SimiSvcApi svc=new SimiSvcClient();
        svc.setup("http://localhost:8080/simi-svc/rest","admin","admin");
        svc.notifyPublication(new PublicationLog("ch.so.agi.mopublic", new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2022-03-08")));
    }
    @Test
    public void notifyPublicationRegions() throws Exception {
        SimiSvcApi svc=new SimiSvcClient();
        java.util.List<String> regions=new java.util.ArrayList<String>();
        regions.add("22");
        regions.add("33");
        svc.setup("http://localhost:8080/simi-svc/rest",null,null);
        PublicationLog pub=new PublicationLog("ch.so.agi.mopublic", new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2022-03-08"));
        pub.addPublishedRegion(new PublishedRegion("22"));
        pub.addPublishedRegion(new PublishedRegion("33"));
        svc.notifyPublication(pub);
    }
}
