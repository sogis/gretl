package ch.so.agi.gretl.steps.publisher.out.metainfo.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.gretl.logging.GretlLogger;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

class JsonSectionWriterTest {
    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockWebServer mockWebServer;

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void writesFirstMatchingSectionAndUsesPathStyleUrl() throws Exception {
        startServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("["
                + "{\"ident\":\"wanted\",\"nested\":{\"values\":[1,2]}},"
                + "{\"ident\":\"wanted\",\"selected\":false}]"));
        Path target = tempDir.resolve("nested").resolve("section.json");

        newWriter().execute(parameters(target, "wanted"));

        JsonNode written = objectMapper.readTree(Files.readString(target));
        assertEquals("wanted", written.get("ident").textValue());
        assertEquals(2, written.path("nested").path("values").size());
        assertFalse(written.has("selected"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/base/my%20bucket/publication%20info.json", request.getPath());
        assertEquals("application/json", request.getHeader("Accept"));
    }

    @Test
    void warnsAndPreservesTargetWhenSourceIsUnavailable() throws Exception {
        startServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        Path target = tempDir.resolve("section.json");
        Files.writeString(target, "{\"old\":true}", StandardCharsets.UTF_8);
        RecordingLogger logger = new RecordingLogger();

        newWriter(logger).execute(parameters(target, "wanted"));

        assertEquals("{\"old\":true}", Files.readString(target));
        assertTrue(logger.lifecycleMessages.get(0).startsWith("Warning: could not load publication JSON"));
    }

    @Test
    void warnsAndDoesNotCreateTargetWhenNoSectionMatches() throws Exception {
        startServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"ident\":\"other\"}]"));
        Path target = tempDir.resolve("section.json");
        RecordingLogger logger = new RecordingLogger();

        newWriter(logger).execute(parameters(target, "wanted"));

        assertFalse(Files.exists(target));
        assertEquals("Warning: no section with ident <wanted> found in <" + sourceUrl() + ">",
                logger.lifecycleMessages.get(0));
    }

    @Test
    void failsForMalformedJsonWithoutOverwritingTarget() throws Exception {
        startServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("[{"));
        Path target = tempDir.resolve("section.json");
        Files.writeString(target, "{\"old\":true}", StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> newWriter().execute(parameters(target, "wanted")));

        assertEquals("{\"old\":true}", Files.readString(target));
    }

    @Test
    void failsForNonArrayRootWithoutOverwritingTarget() throws Exception {
        startServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"ident\":\"wanted\"}"));
        Path target = tempDir.resolve("section.json");
        Files.writeString(target, "{\"old\":true}", StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> newWriter().execute(parameters(target, "wanted")));

        assertEquals("{\"old\":true}", Files.readString(target));
    }

    @Test
    void rejectsInvalidParameters() {
        assertThrows(IllegalArgumentException.class,
                () -> JsonSectionWriterParameters.of("ftp://example.com", "bucket", "source.json", "wanted", tempDir));
        assertThrows(IllegalArgumentException.class,
                () -> JsonSectionWriterParameters.of("http://example.com?x=1", "bucket", "source.json", "wanted", tempDir));
        assertThrows(IllegalArgumentException.class,
                () -> JsonSectionWriterParameters.of("http://example.com", " ", "source.json", "wanted", tempDir));
    }

    private void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    private JsonSectionWriterParameters parameters(Path target, String ident) {
        return JsonSectionWriterParameters.of(mockWebServer.url("/base/").toString(), "my bucket",
                "publication info.json", ident, target);
    }

    private String sourceUrl() {
        return mockWebServer.url("/base/").toString().replaceFirst("/+$", "")
                + "/my%20bucket/publication%20info.json";
    }

    private JsonSectionWriter newWriter() {
        return newWriter(new RecordingLogger());
    }

    private JsonSectionWriter newWriter(RecordingLogger logger) {
        return new JsonSectionWriter(logger, objectMapper);
    }

    private static final class RecordingLogger implements GretlLogger {
        private final List<String> lifecycleMessages = new ArrayList<>();

        @Override
        public void info(String msg) {
        }

        @Override
        public void debug(String msg) {
        }

        @Override
        public void error(String msg, Throwable thrown) {
        }

        @Override
        public void lifecycle(String msg) {
            lifecycleMessages.add(msg);
        }
    }
}
