package ch.so.agi.gretl.steps.publisher.out.metainfo.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

class SheetDownloaderTest {
    @TempDir
    Path tempDir;

    private MockWebServer mockWebServer;

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void downloadsSheetIntoMetaDirectory() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("<html>Alpha</html>"));
        mockWebServer.start();

        Path publicationRoot = tempDir.resolve("publication");

        new SheetDownloader().execute(SheetDownloaderParameters.of(publicationRoot, baseUrl("/doc"), "ch.so.agi.demo"));

        Path targetFile = publicationRoot.resolve("meta").resolve("datenbeschreibung.html");
        assertTrue(Files.isRegularFile(targetFile));
        assertEquals("<html>Alpha</html>", Files.readString(targetFile));
    }

    @Test
    void appendsEncodedDataIdentQueryParameter() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("<html>Encoded</html>"));
        mockWebServer.start();

        new SheetDownloader().execute(
                SheetDownloaderParameters.of(tempDir.resolve("publication"), baseUrl("/doc"), "ch.so demo/a"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/doc?dataident=ch.so+demo%2Fa", request.getPath());
        assertEquals("text/html", request.getHeader("Accept"));
    }

    @Test
    void createsMetaDirectoryWhenAbsent() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("<html>Alpha</html>"));
        mockWebServer.start();

        Path publicationRoot = Files.createDirectories(tempDir.resolve("nested").resolve("publication"));
        Path metaDirectory = publicationRoot.resolve("meta");
        assertFalse(Files.exists(metaDirectory));

        new SheetDownloader().execute(SheetDownloaderParameters.of(publicationRoot, baseUrl("/doc"), "ch.so.agi.demo"));

        assertTrue(Files.isDirectory(metaDirectory));
        assertTrue(Files.isRegularFile(metaDirectory.resolve("datenbeschreibung.html")));
    }

    @Test
    void rejectsBlankBaseUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> SheetDownloaderParameters.of(tempDir, " ", "ch.so.agi.demo"));
    }

    @Test
    void rejectsBlankDataIdent() {
        assertThrows(IllegalArgumentException.class,
                () -> SheetDownloaderParameters.of(tempDir, "http://example.com/doc", " "));
    }

    @Test
    void failsOnNonSuccessResponseWithoutCreatingSheetFile() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404).setBody("missing"));
        mockWebServer.start();

        Path publicationRoot = tempDir.resolve("publication");

        assertThrows(IOException.class,
                () -> new SheetDownloader().execute(
                        SheetDownloaderParameters.of(publicationRoot, baseUrl("/doc"), "ch.so.agi.demo")));

        assertFalse(Files.exists(publicationRoot.resolve("meta").resolve("datenbeschreibung.html")));
    }

    private String baseUrl(String path) {
        return mockWebServer.url(path).toString();
    }
}
