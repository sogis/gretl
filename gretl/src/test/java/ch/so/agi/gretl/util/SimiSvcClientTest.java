package ch.so.agi.gretl.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.so.agi.gretl.steps.PublisherStep;
import ch.so.agi.gretl.util.publisher.PublicationLog;
import ch.so.agi.gretl.util.publisher.PublishedRegion;

public class SimiSvcClientTest {
    public final static String OAUTH_TOKEN_GRANT_USR="admin";
    public final static String OAUTH_TOKEN_GRANT_PWD="admin";
    public final static String OAUTH_TOKEN_USR="restid2";
    public final static String OAUTH_TOKEN_PWD="restpass2";
    public final static String OAUTH_TOKEN="n0rri21Kxbuekf2wuSPj0NSuMQw";
    public final static String DOC_LEAFLET="<html/>";
    
    public static class CustomServlet extends HttpServlet {

        public String doc_dataident=null;
        public String doc_published=null;
        public String doc_contentType=null;
        public String doc_authHeaderValue=null;
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if("/doc".equals(req.getPathInfo())) {
                // curl -X GET -H "Content-Type: text/html" "http://localhost:8080/simi-svc/rest/doc?dataident=ch.so.agi.mopublic&published=2021-12-23T14:50:59.825849"
                doc_dataident=req.getParameter("dataident");
                doc_published=req.getParameter("published");
                doc_contentType=req.getContentType();
                doc_authHeaderValue=req.getHeader("Authorization");
                resp.getWriter().append(DOC_LEAFLET);
                resp.getWriter().flush();
            }
        }

        public String oauthToken_authHeaderValue=null;
        public String oauthToken_contentType=null;
        public String oauthToken_content=null;
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if("/v2/oauth/token".equals(req.getPathInfo())) {
                oauthToken_authHeaderValue=req.getHeader("Authorization");
                oauthToken_contentType=req.getContentType();
                oauthToken_content=req.getReader().readLine();
                resp.getWriter().append("{\"access_token\":\""+OAUTH_TOKEN+"\",\"token_type\":\"bearer\",\"refresh_token\":\"TOKEN\",\"expires_in\":43199,\"scope\":\"rest-api\"}");
                resp.getWriter().flush();
            }
        }

        public String pubsignal_authHeaderValue=null;
        public String pubsignal_contentType=null;
        public String pubsignal_content=null;
        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if("/pubsignal".equals(req.getPathInfo())) {
                pubsignal_authHeaderValue=req.getHeader("Authorization");
                pubsignal_contentType=req.getContentType();
                pubsignal_content=req.getReader().readLine();
            }
        }
        
    }
    private static CustomServlet servlet=null;
    private static Tomcat tomcat = null;
    @BeforeClass
    public static synchronized void setupServer() throws Exception {
        if(tomcat==null) {
            tomcat = new Tomcat();
            //tomcat.setPort(8080);
            //tomcat.setBaseDir(new java.io.File(".").getAbsolutePath());
            tomcat.getConnector(); // required to init connector
            Context context=tomcat.addContext("/simi-svc/rest",new java.io.File(".").getAbsolutePath());
            servlet=new CustomServlet();
            Wrapper wrapper = Tomcat.addServlet(context, "Test", servlet);
            wrapper.addMapping("/*");
            tomcat.getHost().setAutoDeploy(true);
            tomcat.getHost().setDeployOnStartup(true);
            tomcat.start();
        }
    }
    @AfterClass
    public static synchronized void stopServer() throws Exception {
        if(tomcat!=null) {
            tomcat.stop();
            tomcat=null;
            servlet=null;
        }
    }
    @Test
    public void getLeaflet() throws Exception {
        setupServer();
        SimiSvcApi svc=new SimiSvcClient();
        svc.setup("http://localhost:8080/simi-svc/rest",OAUTH_TOKEN_GRANT_USR,OAUTH_TOKEN_GRANT_PWD);
        svc.setupTokenService("http://localhost:8080/simi-svc/rest",OAUTH_TOKEN_USR,OAUTH_TOKEN_PWD);
        final String expected_dataIdent = "ch.so.agi.mopublic";
        final String expected_published = "2022-03-08T00:00:00.000";
        String leaflet=svc.getLeaflet(expected_dataIdent, new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(expected_published));
        Assert.assertEquals(DOC_LEAFLET, leaflet);
        Assert.assertEquals(expected_dataIdent, servlet.doc_dataident);
        Assert.assertEquals(expected_published, servlet.doc_published);
        Assert.assertEquals("text/html", servlet.doc_contentType);
        Assert.assertEquals("Bearer " + OAUTH_TOKEN, servlet.doc_authHeaderValue);
    }
    @Test
    public void getAccessToken() throws Exception {
        setupServer();
        SimiSvcApi svc=new SimiSvcClient();
        svc.setup("http://localhost:8080/simi-svc/rest",OAUTH_TOKEN_GRANT_USR,OAUTH_TOKEN_GRANT_PWD);
        svc.setupTokenService("http://localhost:8080/simi-svc/rest",OAUTH_TOKEN_USR,OAUTH_TOKEN_PWD);
        String token=svc.getAccessToken();
        Assert.assertEquals(OAUTH_TOKEN,token);
        {
            String auth = OAUTH_TOKEN_USR + ":" + OAUTH_TOKEN_PWD;
            byte[] encodedAuth = java.util.Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);
            Assert.assertEquals(authHeaderValue, servlet.oauthToken_authHeaderValue);
        }
        Assert.assertEquals("application/x-www-form-urlencoded",servlet.oauthToken_contentType);
        Assert.assertEquals("grant_type=password&username="+OAUTH_TOKEN_GRANT_USR+"&password="+OAUTH_TOKEN_GRANT_PWD,servlet.oauthToken_content);
    }
    @Test
    public void notifyPublication() throws Exception {
        setupServer();
        SimiSvcApi svc=new SimiSvcClient();
        svc.setup("http://localhost:8080/simi-svc/rest",null,null);
        final PublicationLog data = new PublicationLog("ch.so.agi.mopublic", new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2022-03-08"));
        svc.notifyPublication(data);
        Assert.assertEquals("application/json", servlet.pubsignal_contentType);
        Assert.assertNull(servlet.pubsignal_authHeaderValue);
        Assert.assertEquals(PublisherStep.publicationToString(data),servlet.pubsignal_content);
    }
    @Test
    public void notifyPublicationOAuth() throws Exception {
        setupServer();
        SimiSvcApi svc=new SimiSvcClient();
        svc.setup("http://localhost:8080/simi-svc/rest",OAUTH_TOKEN_GRANT_USR,OAUTH_TOKEN_GRANT_PWD);
        svc.setupTokenService("http://localhost:8080/simi-svc/rest",OAUTH_TOKEN_USR,OAUTH_TOKEN_PWD);
        final PublicationLog data = new PublicationLog("ch.so.agi.mopublic", new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2022-03-08"));
        svc.notifyPublication(data);
        Assert.assertEquals("application/json", servlet.pubsignal_contentType);
        Assert.assertEquals("Bearer " + OAUTH_TOKEN, servlet.pubsignal_authHeaderValue);
        Assert.assertEquals(PublisherStep.publicationToString(data),servlet.pubsignal_content);
    }
    @Test
    public void notifyPublicationRegions() throws Exception {
        setupServer();
        SimiSvcApi svc=new SimiSvcClient();
        java.util.List<String> regions=new java.util.ArrayList<String>();
        regions.add("22");
        regions.add("33");
        svc.setup("http://localhost:8080/simi-svc/rest",OAUTH_TOKEN_GRANT_USR,OAUTH_TOKEN_GRANT_PWD);
        svc.setupTokenService("http://localhost:8080/simi-svc/rest",OAUTH_TOKEN_USR,OAUTH_TOKEN_PWD);
        PublicationLog pub=new PublicationLog("ch.so.agi.mopublic", new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2022-03-08"));
        pub.addPublishedRegion(new PublishedRegion("22"));
        pub.addPublishedRegion(new PublishedRegion("33"));
        svc.notifyPublication(pub);
        Assert.assertEquals("application/json", servlet.pubsignal_contentType);
        Assert.assertEquals("Bearer " + OAUTH_TOKEN, servlet.pubsignal_authHeaderValue);
        Assert.assertEquals(PublisherStep.publicationToString(pub),servlet.pubsignal_content);
    }
}
