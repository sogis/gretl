package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;


// README.md eventuell
// abstract class geht erst mit 5.6 oder so. Nicht mit 5.1.1
// Dann kommen aber viele Warnungen von anderen Tasks wegen fehlendem Getter o.ae.
// Publisher-Ansatz geht nicht, weil dann wird wirklich ein Objekt vom Typ Property erwartet.
// Das ist fuer Anwender doof.

public class Curl extends DefaultTask {
    protected GretlLogger log;

    @Internal
    public String serverUrl;
    
    @Internal
    public MethodType method;
    
    @Internal
    public int expectedStatusCode;
    
    @Internal
    @Optional
    public Map<String,Object> formData; // curl [URL] -F key1=value1 -F file1=@my_file.xtf 
    
    @Internal
    @Optional
    public String data; // curl [URL] -d "key1=value1&key2=value2"
    
    @Internal
    @Optional
    public File outputFile; // curl [URL] -o
    
    @Internal
    @Optional
    public File dataBinary; // curl [URL] --data-binary
    
    @Internal
    @Optional
    public Map<String,String> header; // curl [URL] -H ... -H ...
    
    @Internal
    @Optional
    public String user;
    
    @Internal
    @Optional
    public String password;
       
    @TaskAction
    public void request() throws ClientProtocolException, IOException {
        log = LogEnvironment.getLogger(Curl.class);

        System.out.println("*********"+serverUrl+"***********");
        System.out.println("*********"+method+"***********");
        System.out.println("*********"+formData+"***********");
        
        
//        HttpRequestBase request;
//        if (method.equals(MethodType.GET)) {
//            request = new HttpGet(serverUrl);
//        } else {
//            request = new HttpPost(serverUrl);            
//        }
        
        CredentialsProvider provider = null;
        if (user != null && password != null) {
            System.out.println(user);
            System.out.println(password);
            provider = new BasicCredentialsProvider();
            provider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(user, password)
            ); 
        }

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            if (entry.getValue() instanceof String) {
                entityBuilder.addTextBody(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof File) {
                entityBuilder.addBinaryBody(entry.getKey(), (File) entry.getValue());   
            }
        }
        HttpEntity entity = entityBuilder.build();
       
        HttpPost request = new HttpPost(serverUrl);
        request.setEntity(entity);
        
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        
        if (provider != null) {
            System.out.println("*******asdfasd");
            clientBuilder.setDefaultCredentialsProvider(provider);
        }
        
        HttpClient httpClient = clientBuilder.build();

        HttpResponse httpResponse = httpClient.execute(request);
        System.out.println("POST Response Status:: "
                + httpResponse.getStatusLine().getStatusCode());

        
//        try (CloseableHttpClient httpClient = HttpClients.createDefault();
//                CloseableHttpResponse response = httpClient.execute(post)){
//
//               result = EntityUtils.toString(response.getEntity());
//           }


    }
    
    
    private static enum MethodType {
        GET, POST
    }

    
    
    
}
