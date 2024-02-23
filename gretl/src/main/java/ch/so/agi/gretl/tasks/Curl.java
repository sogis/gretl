package ch.so.agi.gretl.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;


// README.md eventuell
// abstract class geht erst mit 5.6 oder so. Nicht mit 5.1.1
// Dann kommen aber viele Warnungen von anderen Tasks wegen fehlendem Getter o.ae.
// Publisher-Ansatz geht nicht, weil dann wird wirklich ein Objekt vom Typ Property erwartet.
// Das ist fuer Anwender doof.

public class Curl extends DefaultTask {
    protected GretlLogger log;

    private String serverUrl;
    private MethodType method;
    private int expectedStatusCode;
    private String expectedBody;
    private Map<String,Object> formData; // curl [URL] -F key1=value1 -F file1=@my_file.xtf
    private File outputFile; // curl [URL] -o
    private File dataBinary; // curl [URL] --data-binary / ueberschreibt formData, siehe setEntity (glaub)
    private Map<String,String> headers; // curl [URL] -H ... -H ...
    private String user;
    private String password;

    @Internal
    public String getServerUrl() {
        return serverUrl;
    }

    @Internal
    public MethodType getMethod() {
        return method;
    }

    @Internal
    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    @Internal
    @Optional
    public String getExpectedBody() {
        return expectedBody;
    }

    @Internal
    @Optional
    public Map<String, Object> getFormData() {
        return formData;
    }

    @Internal
    @Optional
    public File getOutputFile() {
        return outputFile;
    }

    @Internal
    @Optional
    public File getDataBinary() {
        return dataBinary;
    }

    @Internal
    @Optional
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Internal
    @Optional
    public String getUser() {
        return user;
    }

    @Internal
    @Optional
    public String getPassword() {
        return password;
    }

    @TaskAction
    public void request() throws ClientProtocolException, IOException {
        log = LogEnvironment.getLogger(Curl.class);

        RequestBuilder requestBuilder;
        if (method.equals(MethodType.GET)) {
            requestBuilder = RequestBuilder.get();
        } else {
            requestBuilder = RequestBuilder.post();
        }
        
        if (user != null && password != null) {
            Header header = new BasicHeader("Authorization", "Basic "+ Base64.getEncoder().encodeToString((user+":"+password).getBytes()));
            requestBuilder.addHeader(header);
        }

        if (formData != null) {
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
            requestBuilder.setEntity(entity);
        }
        
        if (dataBinary != null) {
            byte[] data = Files.readAllBytes(dataBinary.toPath());
            requestBuilder.setEntity(EntityBuilder.create().setBinary(data).build());
        }
        
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                requestBuilder.addHeader(key, value);
            }
        }
       
        requestBuilder.setUri(serverUrl);
        
        int responseStatusCode;
        String responseContent = null;
        HttpUriRequest request = requestBuilder.build();
        try (CloseableHttpClient httpClient = HttpClients.createDefault(); 
                CloseableHttpResponse httpResponse = httpClient.execute(request)) {
          
            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                log.lifecycle(header.getName() + " -- " + header.getValue());
            }

            responseStatusCode = httpResponse.getStatusLine().getStatusCode();
            
            if (outputFile != null) {
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    try (FileOutputStream outstream = new FileOutputStream(outputFile)) {
                        entity.writeTo(outstream);
                    }
                }
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();
                responseContent = response.toString();                
            }            
        } 
        
        if (responseStatusCode != expectedStatusCode) {
            throw new GradleException("Wrong status code returned: " + String.valueOf(responseStatusCode));
        }
        
        if (expectedBody != null) {
            if (!responseContent.contains(expectedBody)) {
                throw new GradleException("Response body does not contain expected string: " + responseContent);
            }            
        }
    }
    
    public static enum MethodType {
        GET, POST
    }    
}
