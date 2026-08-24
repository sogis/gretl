package ch.so.agi.gretl.steps.publisher.out.metainfo.metafolder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class MetaFolderWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void copiesResolvedModelsOnceWithoutLegacyFilesForLocalPublication() throws Exception {
        Path cache = Files.createDirectories(tempDir.resolve("cache"));
        Path fixtureRoot = Path.of("src/test/resources/data/publisher").toAbsolutePath();
        Files.copy(fixtureRoot.resolve("files/SimpleCoord23a.xtf"), cache.resolve("first.xtf"));
        Files.copy(fixtureRoot.resolve("files/SimpleCoord23b.xtf"), cache.resolve("second.xtf"));

        new MetaFolderWriter().execute(MetaFolderWriterParameters.of(cache, "ch.so.agi.demo",
                fixtureRoot.resolve("ili").toString(), null, null, null, null));

        Path copiedModel = cache.resolve("meta/SimpleCoord23.ili");
        assertTrue(Files.isRegularFile(copiedModel));
        assertArrayEquals(Files.readAllBytes(fixtureRoot.resolve("ili/SimpleCoord23.ili")),
                Files.readAllBytes(copiedModel));
        assertFalse(Files.exists(cache.resolve("meta/publishdate.json")));
        assertFalse(Files.exists(cache.resolve("meta/datenbeschreibung.html")));
        assertFalse(Files.exists(cache.resolve("meta/metainfo.json")));
    }

    @Test
    void leavesMetaAbsentWhenModelResolutionFails() throws Exception {
        Path cache = Files.createDirectories(tempDir.resolve("cache"));
        Files.writeString(cache.resolve("broken.xtf"), "<TRANSFER>not a valid transfer</TRANSFER>");

        try {
            new MetaFolderWriter().execute(MetaFolderWriterParameters.of(cache, "ch.so.agi.demo", null,
                    null, null, null, null));
        } catch (IllegalStateException expected) {
            assertFalse(Files.exists(cache.resolve("meta")));
            return;
        }
        throw new AssertionError("model resolution should fail");
    }

    @Test
    void writesMatchingJsonSectionIntoMetaFolder() throws Exception {
        Path cache = Files.createDirectories(tempDir.resolve("cache"));
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            server.enqueue(new MockResponse().setResponseCode(200)
                    .setBody("[{\"ident\":\"other\"},{\"ident\":\"ch.so.agi.demo\",\"title\":\"Demo\"}]"));

            new MetaFolderWriter().execute(MetaFolderWriterParameters.of(cache, "ch.so.agi.demo", null, null,
                    server.url("/").toString(), "bucket", "publication.json"));

            assertTrue(Files.isDirectory(cache.resolve("meta")));
            assertTrue(Files.isRegularFile(cache.resolve("meta/metainfo.json")));
            assertTrue(new ObjectMapper().readTree(Files.readString(cache.resolve("meta/metainfo.json")))
                    .path("title").asText().equals("Demo"));
        }
    }
}
