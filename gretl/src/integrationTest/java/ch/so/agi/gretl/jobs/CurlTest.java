package ch.so.agi.gretl.jobs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import java.io.IOException;

import static org.junit.Assert.*;

public class CurlTest {    
    private MockWebServer mockWebServer;
    
    @Before
    public void setup() throws IOException {
      this.mockWebServer = new MockWebServer();
      this.mockWebServer.start();
    }
    
    @After
    public void tearDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    public void geodienste_Ok() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("\"success\":true");
        mockWebServer.enqueue(mockResponse);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CurlGeodienste");
        GradleVariable[] variables = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };

        IntegrationTestUtil.executeTestRunner(projectDirectory, "uploadData", variables);
        
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        
        assertEquals("/data_agg/interlis/import", recordedRequest.getPath());
        assertEquals(recordedRequest.getHeader("Authorization").split(" ")[1].trim(),
                Base64.getEncoder().encodeToString(("fooUser:barPwd").getBytes()));

        Buffer bodyBuffer = recordedRequest.getBody();
        long bodyBufferSize = bodyBuffer.size();
        String bodyContent = bodyBuffer.readUtf8();
        assertTrue(bodyBufferSize>500L);
        assertTrue(bodyContent.contains("name=\"topic\""));
        assertTrue(bodyContent.contains("npl_waldgrenzen"));
        assertTrue(bodyContent.contains("name=\"lv95_file\"; filename=\"test.xtf.zip\""));
    }
    
    @Test
    public void geodienste_Fail() {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("\"success\":false");
        mockWebServer.enqueue(mockResponse);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CurlGeodienste");
        GradleVariable[] variables = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };

        Exception exception = assertThrows(Exception.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory, "uploadData", variables);
        });

        assertTrue(exception.getMessage().contains("Response body does not contain expected string:"));
    }
    
    @Test
    public void planregister_Ok() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(202);
        mockWebServer.enqueue(mockResponse);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CurlPlanregister");
        GradleVariable[] variables = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };

        IntegrationTestUtil.executeTestRunner(projectDirectory, "uploadData", variables);
        
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        
        assertEquals("/typo3/api/digiplan", recordedRequest.getPath());
        assertEquals(recordedRequest.getHeader("Authorization").split(" ")[1].trim(),
                Base64.getEncoder().encodeToString(("fooUser:barPwd").getBytes()));
        assertTrue(recordedRequest.getBodySize()>600L);
    }
    
    @Test
    public void downloadFile_Ok() throws Exception {

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CurlDownload");

        IntegrationTestUtil.executeTestRunner(projectDirectory, "downloadData");

        String content = new String(Files.readAllBytes(Paths.get(projectDirectory + "/README.md")));
        assertTrue(content.contains("_GRETL_"));
        assertTrue(content.contains("Licencse"));
    }
}
