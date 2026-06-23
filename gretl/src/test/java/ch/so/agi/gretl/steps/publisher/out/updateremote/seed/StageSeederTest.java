package ch.so.agi.gretl.steps.publisher.out.updateremote.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StageSeederTest {
    @TempDir
    Path tempDir;

    @Test
    void copiesTopLevelZipFilesFromCurrentPublication() throws Exception {
        Path dataRoot = Files.createDirectories(tempDir.resolve("data"));
        Path currentRoot = Files.createDirectories(dataRoot.resolve("aktuell"));
        Files.writeString(currentRoot.resolve("existing.xtf.zip"), "old", StandardCharsets.UTF_8);
        Files.writeString(currentRoot.resolve("ignore.txt"), "ignore", StandardCharsets.UTF_8);
        Files.createDirectories(currentRoot.resolve("meta"));
        Files.writeString(currentRoot.resolve("meta").resolve("meta.zip"), "nested", StandardCharsets.UTF_8);
        Path tempStageRoot = tempDir.resolve("stage");

        new StageSeeder().seed(dataRoot, tempStageRoot);

        assertTrue(Files.isRegularFile(tempStageRoot.resolve("existing.xtf.zip")));
        assertEquals("old", Files.readString(tempStageRoot.resolve("existing.xtf.zip")));
        assertFalse(Files.exists(tempStageRoot.resolve("ignore.txt")));
        assertFalse(Files.exists(tempStageRoot.resolve("meta")));
    }

    @Test
    void ignoresMissingCurrentPublication() throws Exception {
        Path dataRoot = Files.createDirectories(tempDir.resolve("data"));
        Path tempStageRoot = tempDir.resolve("stage");

        new StageSeeder().seed(dataRoot, tempStageRoot);

        assertFalse(Files.exists(tempStageRoot));
    }
}
