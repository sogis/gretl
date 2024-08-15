package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

class CurlTest {
    private static MockWebServer mockWebServer;
    
    @BeforeAll
    public static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    public static void teardown() throws IOException {
        mockWebServer.shutdown();
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
        
        Assertions.assertEquals("/data_agg/interlis/import", recordedRequest.getPath());
        Assertions.assertEquals(recordedRequest.getHeader("Authorization").split(" ")[1].trim(), Base64.getEncoder().encodeToString(("fooUser:barPwd").getBytes()));

        Buffer bodyBuffer = recordedRequest.getBody();
        long bodyBufferSize = bodyBuffer.size();
        String bodyContent = bodyBuffer.readUtf8();
        Assertions.assertTrue(bodyBufferSize>500L);
        Assertions.assertTrue(bodyContent.contains("name=\"topic\""));
        Assertions.assertTrue(bodyContent.contains("npl_waldgrenzen"));
        Assertions.assertTrue(bodyContent.contains("name=\"lv95_file\"; filename=\"test.xtf.zip\""));
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
        Assertions.assertEquals(1, IntegrationTestUtil.runJob("src/integrationTest/jobs/CurlGeodienste", gvs, new StringBuffer(), new StringBuffer()));
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
        
        Assertions.assertEquals("/typo3/api/digiplan", recordedRequest.getPath());
        Assertions.assertEquals(recordedRequest.getHeader("Authorization").split(" ")[1].trim(), Base64.getEncoder().encodeToString(("fooUser:barPwd").getBytes()));
        Assertions.assertTrue(recordedRequest.getBodySize()>600L);
    }
    
    @Test
    public void downloadFile_Ok() throws Exception {
        // Run GRETL task
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/CurlDownload", gvs);

        // Validate result
        String content = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/CurlDownload/README.md")));
        Assertions.assertTrue(content.contains("_GRETL_"));
        Assertions.assertTrue(content.contains("Licencse"));
    }
}
