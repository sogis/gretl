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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CurlGeodienste");
        GradleVariable[] variables = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };

        // Execute task
        IntegrationTestUtil.executeTestRunner(projectDirectory, variables);
        
        // Validate result
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        
        Assertions.assertEquals("/data_agg/interlis/import", recordedRequest.getPath());
        Assertions.assertEquals(recordedRequest.getHeader("Authorization").split(" ")[1].trim(), Base64.getEncoder().encodeToString(("fooUser:barPwd").getBytes()));

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

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CurlGeodienste");
        GradleVariable[] variables = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };

        // Execute task
        assertThrows(Throwable.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory, variables);
        });
    }
    
    @Test
    public void planregister_Ok() throws Exception {
        // Prepare mock web server
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(202);
        mockWebServer.enqueue(mockResponse);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CurlPlanregister");
        GradleVariable[] variables = { GradleVariable.newGradleProperty("mockWebServerPort", String.valueOf(mockWebServer.getPort())) };

        // Execute task
        IntegrationTestUtil.executeTestRunner(projectDirectory, variables);
        
        // Validate result
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        
        Assertions.assertEquals("/typo3/api/digiplan", recordedRequest.getPath());
        Assertions.assertEquals(recordedRequest.getHeader("Authorization").split(" ")[1].trim(), Base64.getEncoder().encodeToString(("fooUser:barPwd").getBytes()));
        Assertions.assertTrue(recordedRequest.getBodySize()>600L);
    }
    
    @Test
    public void downloadFile_Ok() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CurlDownload");

        // Execute task
        IntegrationTestUtil.executeTestRunner(projectDirectory);

        // Validate result
        String content = new String(Files.readAllBytes(Paths.get(projectDirectory + "/README.md")));
        assertTrue(content.contains("_GRETL_"));
        assertTrue(content.contains("Licencse"));
    }
}
