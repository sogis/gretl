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
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
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
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

public class Curl extends DefaultTask {
    private static GretlLogger log;

    private String serverUrl;
    private MethodType method;
    private Integer expectedStatusCode;
    private String expectedBody;
    private Map<String,Object> formData; // curl [URL] -F key1=value1 -F file1=@my_file.xtf
    private File outputFile; // curl [URL] -o
    private File dataBinary; // curl [URL] --data-binary / ueberschreibt formData, siehe setEntity (glaub)
    private String data; // curl [URL] --data
    private MapProperty<String, String> headers = getProject().getObjects().mapProperty(String.class, String.class); // curl [URL] -H ... -H ...
    private String user;
    private String password;

    /**
     * Die URL des Servers inklusive Pfad und Queryparameter.
     */
    @Input
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * HTTP-Request-Methode. Unterstützt werden `GET` und `POST`.
     */
    @Input
    @Optional
    public MethodType getMethod() {
        return method;
    }

    /**
     * Erwarteter Status Code, der vom Server zurückgeliefert wird.
     */
    @Input
    public Integer getExpectedStatusCode() {
        return expectedStatusCode;
    }

    /**
     * Erwarteter Text, der vom Server als Body zurückgelieferd wird.
     */
    @Input
    @Optional
    public String getExpectedBody() {
        return expectedBody;
    }

    /**
     * Form data parameters. Entspricht `curl [URL] -F key1=value1 -F file1=@my_file.xtf`.
     */
    @Input
    @Optional
    public Map<String, Object> getFormData() {
        return formData;
    }

    /**
     * Datei, in die der Output gespeichert wird. Entspricht `curl [URL] -o`.
     */
    @OutputFile
    @Optional
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Datei, die hochgeladen werden soll. Entspricht `curl [URL] --data-binary`. 
     */
    @InputFile
    @Optional
    public File getDataBinary() {
        return dataBinary;
    }
    
    /**
     * String, der via POST hochgeladen werden soll. Entspricht `curl [URL] --data`.
     */
    @Input
    @Optional
    public String getData() {
        return data;
    }

    /**
     * Request-Header. Entspricht `curl [URL] -H ... -H ....`.
     */
    @Input
    @Optional
    public MapProperty<String, String> getHeaders() {
        return headers;
    }

    /**
     * Benutzername. Wird zusammen mit `password` in einen Authorization-Header umgewandelt. Entspricht `curl [URL] -u user:password`.
     */
    @Input
    @Optional
    public String getUser() {
        return user;
    }

    /**
     * Passwort. Wird zusammen mit `user` in einen Authorization-Header umgewandelt. Entspricht `curl [URL] -u user:password`.
     */
    @Input
    @Optional
    public String getPassword() {
        return password;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

    public void setExpectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    public void setExpectedBody(String expectedBody) {
        this.expectedBody = expectedBody;
    }

    public void setFormData(Map<String, Object> formData) {
        this.formData = formData;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setDataBinary(File dataBinary) {
        this.dataBinary = dataBinary;
    }
    
    public void setData(String data) {
        this.data = data;
    }

    public void setHeaders(MapProperty<String, String> headers) {
        this.headers = headers;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @TaskAction
    public void request() throws ClientProtocolException, IOException {
        log = LogEnvironment.getLogger(Curl.class);
        
        if (serverUrl == null) {
            throw new IllegalArgumentException("serverUrl must not be null");
        }

        if (expectedStatusCode == null) {
            throw new IllegalArgumentException("expectedStatusCode must not be null");
        }
        
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
            byte[] data = Files.readAllBytes(((File)dataBinary).toPath());
            requestBuilder.setEntity(EntityBuilder.create().setBinary(data).build());            
        }
        
        if (data != null) {
            requestBuilder.setEntity(EntityBuilder.create().setText(data).build());            
        }
        
        if (headers.isPresent()) {
            for (Map.Entry<String, String> entry : headers.get().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                requestBuilder.addHeader(key, value);
                log.debug(key + " -- " + value);
            }
        }
       
        requestBuilder.setUri(serverUrl);
        
        int responseStatusCode;
        String responseContent = null;
        HttpUriRequest request = requestBuilder.build();
        log.debug(request.toString());
        try (CloseableHttpClient httpClient = HttpClients.createDefault(); 
                CloseableHttpResponse httpResponse = httpClient.execute(request)) {
          
            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                log.debug(header.getName() + " -- " + header.getValue());
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
