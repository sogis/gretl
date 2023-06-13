package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import java.io.IOException;

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
        // Prepare mock web server
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("\"success\":true");
        mockWebServer.enqueue(mockResponse);
        
        // Run GRETL task
        GradleVariable[] gvs = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/CurlGeodienste", gvs);
        
        // Validate result
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
    public void geodienste_Fail() throws Exception {
        // Prepare mock web server
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("\"success\":false");
        mockWebServer.enqueue(mockResponse);
        
        // Run GRETL task
        GradleVariable[] gvs = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };
        assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/CurlGeodienste", gvs, new StringBuffer(), new StringBuffer()));
    }
    
    @Test
    public void planregister_Ok() throws Exception {
        // Prepare mock web server
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(202);
        mockWebServer.enqueue(mockResponse);
        
        // Run GRETL task
        GradleVariable[] gvs = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/CurlPlanregister", gvs);
        
        // Validate result
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        
        assertEquals("/typo3/api/digiplan", recordedRequest.getPath());
//        assertEquals(recordedRequest.getHeader("Authorization").split(" ")[1].trim(),
//                Base64.getEncoder().encodeToString(("fooUser:barPwd").getBytes()));

//        Buffer bodyBuffer = recordedRequest.getBody();
//        long bodyBufferSize = bodyBuffer.size();
//        String bodyContent = bodyBuffer.readUtf8();
//        assertTrue(bodyBufferSize>500L);
//        assertTrue(bodyContent.contains("name=\"topic\""));
//        assertTrue(bodyContent.contains("npl_waldgrenzen"));
//        assertTrue(bodyContent.contains("name=\"lv95_file\"; filename=\"test.xtf.zip\""));
    }

}
