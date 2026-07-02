package ch.so.agi.gretl.steps.publisher.out.updateremote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import ch.so.agi.gretl.util.Grooming;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RemoteUpdaterTest {
    private static final String DATE_TAG = "2026-06-23";

    @TempDir
    Path tempDir;

    @Test
    void seedsPushesAndRotatesPublication() throws Exception {
        Path remoteTargetRoot = Files.createDirectories(tempDir.resolve("remote"));
        Path dataRoot = Files.createDirectories(remoteTargetRoot.resolve("ch.so.agi.demo"));
        Path currentRoot = Files.createDirectories(dataRoot.resolve("aktuell"));
        Files.writeString(currentRoot.resolve("kept.xtf.zip"), "kept", StandardCharsets.UTF_8);
        Files.writeString(currentRoot.resolve("replaced.xtf.zip"), "old", StandardCharsets.UTF_8);

        Path localStageRoot = Files.createDirectories(tempDir.resolve("local"));
        Files.writeString(localStageRoot.resolve("replaced.xtf.zip"), "new", StandardCharsets.UTF_8);
        Files.writeString(Files.createDirectories(localStageRoot.resolve("meta")).resolve("publishdate.json"), "{}",
                StandardCharsets.UTF_8);

        new RemoteUpdater().execute(
                RemoteUpdaterParameters.of(localStageRoot, remoteTargetRoot, "ch.so.agi.demo", date()));

        Path newCurrentRoot = dataRoot.resolve("aktuell");
        assertEquals("kept", Files.readString(newCurrentRoot.resolve("kept.xtf.zip")));
        assertEquals("new", Files.readString(newCurrentRoot.resolve("replaced.xtf.zip")));
        assertTrue(Files.isRegularFile(newCurrentRoot.resolve("meta").resolve("publishdate.json")));
        assertTrue(Files.isRegularFile(dataRoot.resolve("hist").resolve(DATE_TAG).resolve("kept.xtf.zip")));
    }

    private static Date date() throws Exception {
        return Grooming.getDateFormat().parse(DATE_TAG);
    }
}
