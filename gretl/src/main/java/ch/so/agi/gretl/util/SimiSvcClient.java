package ch.so.agi.gretl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.PublisherStep;
import ch.so.agi.gretl.util.publisher.PublicationLog;

public class SimiSvcClient implements SimiSvcApi {
    private String endpoint=null;
    private String usr=null;
    private String pwd=null;
    private String tokenEndpoint=null;
    private String tokenUsr="restid";
    private String tokenPwd="restpass";
    private String token=null;
    private GretlLogger log;
    
    @Override
    public void setup(String endpoint,String usr,String pwd) {
        this.log = LogEnvironment.getLogger(this.getClass());
        this.endpoint=endpoint;
        this.tokenEndpoint=endpoint;
        this.usr=usr;
        this.pwd=pwd;
    }
    @Override
    public void setupTokenService(String endpoint,String usr,String pwd) {
        this.tokenEndpoint=endpoint;
        this.tokenUsr=usr;
        this.tokenPwd=pwd;
    }
    /*
    curl -X POST \
      http://localhost:8080/app/rest/v2/oauth/token \
      -u 'restid:restpass' \
      -H 'Content-Type: application/x-www-form-urlencoded' \
      -d 'grant_type=password&username=admin&password=admin'
      
   {"access_token":"n0rri21Kxbuekf2wuSPj0NSuMQw","token_type":"bearer","refresh_token":"lPgeTNwWTajKE1LEva4TkpHsSk4","expires_in":43199,"scope":"rest-api"}   
      
*/
    @Override
    public String getAccessToken() throws IOException 
    {
        if(usr==null) {
            return null;
        }
        StringBuilder response=new StringBuilder();

        int status=doHttpRequest(response,"POST",tokenEndpoint+"/v2/oauth/token","grant_type=password&username="+usr+"&password="+pwd,"application/x-www-form-urlencoded",tokenUsr,tokenPwd);
        if(status!=HttpURLConnection.HTTP_OK) {
            if(response.length()>0) {
                log.info(response.toString());
            }
            throw new IOException("failed to get oauth token from service at "+tokenEndpoint);
        }
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map map = mapper.readValue(new java.io.StringReader(response.toString()), java.util.Map.class);
        String accessToken=(String)map.get("access_token");
        return accessToken;
    }
    // curl -X GET -H "Content-Type: text/html" "http://localhost:8080/simi-svc/rest/doc?dataident=ch.so.agi.mopublic&published=2021-12-23T14:50:59.825849"
    @Override
    public String getLeaflet(String dataIdent,java.util.Date publishDate) throws IOException 
    {
        StringBuilder response=new StringBuilder();
        String versionTag=new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(publishDate);

        if(usr!=null && token==null) {
            token=getAccessToken();
        }
        int status=doHttpRequest(response,"GET",endpoint+"/doc?dataident="+dataIdent+"&published="+versionTag,null,"text/html",null,null);
        if(status!=HttpURLConnection.HTTP_OK) {
            if(response.length()>0) {
                log.info(response.toString());
            }
            throw new IOException("failed to send get doc request to service at "+endpoint);
        }
        return response.toString();
    }
    // curl -X PUT -H "Content-Type: application/json" --data "{\"dataIdent\":\"ch.so.afu.gewaesserschutz\",\"published\":\"2021-12-23T14:54:49.050062\", \"partIdentifiers\":[\"224\",\"225\"]}" "http://localhost:8080/simi-svc/rest/pubsignal"
    @Override
    public void notifyPublication(PublicationLog pub) throws IOException
    {
        if(usr!=null && token==null) {
            token=getAccessToken();
        }
        String request=PublisherStep.publicationToString(pub);
        StringBuilder response=new StringBuilder();
        int status=doHttpRequest(response,"PUT",endpoint+"/pubsignal",request,"application/json",null,null);
        if(status!=HttpURLConnection.HTTP_OK) {
            if(response.length()>0) {
                log.info(response.toString());
            }
            throw new IOException("failed to notify publication to service at "+endpoint);
        }
    }
    private int doHttpRequest(StringBuilder response,String requestMethod,String endpoint,String request,String contentType,String usr,String pwd) throws IOException {
        HttpURLConnection conn=null;
        try {
            //
            // java  -Dhttp.proxyHost=myproxyserver.com  -Dhttp.proxyPort=80 MyJavaApp
            //
            // System.setProperty("http.proxyHost", "myProxyServer.com");
            // System.setProperty("http.proxyPort", "80");
            //
            // System.setProperty("java.net.useSystemProxies", "true");
            //
            // since 1.5 
            // Proxy instance, proxy ip = 123.0.0.1 with port 8080
            // Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("123.0.0.1", 8080));
            // URL url = new URL("http://www.yahoo.com");
            // HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
            // uc.connect();
            // 
            java.net.URL url = new java.net.URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw e;
        }
        if(request!=null) {
            conn.setDoOutput(true);
        }
        try {
            conn.setRequestMethod(requestMethod);
        } catch (ProtocolException e) {
            throw new IllegalArgumentException(e);
        }
        String authHeaderValue=null;
        if(usr!=null) {
            String auth = usr + ":" + pwd;
            byte[] encodedAuth = java.util.Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            authHeaderValue = "Basic " + new String(encodedAuth);
        }else if(token!=null) {
            authHeaderValue = "Bearer " + token;
        }
        conn.setRequestProperty("Content-Type",contentType);
        if(authHeaderValue!=null) {
            conn.setRequestProperty("Authorization", authHeaderValue);
        }
        if(request!=null) {
            try {
                conn.getOutputStream().write(request.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        int responseCode=HttpURLConnection.HTTP_INTERNAL_ERROR;
        InputStreamReader in=null;
        java.io.StringWriter fos=null;
        try{
            if(response!=null) {
                fos=new java.io.StringWriter();
                try {
                    String encoding=conn.getContentEncoding();
                    if(encoding==null){
                        encoding="UTF-8";
                    }
                    responseCode=conn.getResponseCode();
                    InputStream inStream = conn.getErrorStream();
                    if(inStream==null){
                        inStream=conn.getInputStream();
                    }
                    if(inStream!=null){
                        in=new java.io.InputStreamReader(inStream,encoding);
                    }
                } catch (IOException e) {
                    throw e;
                }
                if(in!=null){
                    try {
                        char[] buf = new char[1024];
                        int i = 0;
                        while ((i = in.read(buf)) != -1) {
                            fos.write(buf, 0, i);
                        }
                        fos.flush();
                    } catch (IOException e) {
                        throw new IllegalArgumentException("failed to read response",e);
                    }
                    response.append(fos.toString());
                }
            }
            return responseCode;
        }finally{
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                }
                in=null;
            }
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                }
                fos=null;
            }
        }
        
    }
    
     
}