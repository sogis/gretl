package ch.so.agi.gretl.steps.publisher.out.updateremote.rotate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import ch.so.agi.gretl.util.Grooming;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LatestRotatorTest {
    private static final String DATE_TAG = "2026-06-23";

    @TempDir
    Path tempDir;

    @Test
    void promotesTempStageWhenNoCurrentPublicationExists() throws Exception {
        Path dataRoot = Files.createDirectories(tempDir.resolve("data"));
        Path tempStageRoot = Files.createDirectories(tempDir.resolve("stage"));
        Files.writeString(tempStageRoot.resolve("new.xtf.zip"), "new", StandardCharsets.UTF_8);

        new LatestRotator().rotate(dataRoot, tempStageRoot, date());

        assertTrue(Files.isRegularFile(dataRoot.resolve("aktuell").resolve("new.xtf.zip")));
        assertFalse(Files.exists(tempStageRoot));
    }

    @Test
    void movesCurrentPublicationToHistoryAndRemovesHistoricalUserFormats() throws Exception {
        Path dataRoot = Files.createDirectories(tempDir.resolve("data"));
        Path currentRoot = Files.createDirectories(dataRoot.resolve("aktuell"));
        Files.writeString(currentRoot.resolve("old.xtf.zip"), "old", StandardCharsets.UTF_8);
        Files.writeString(currentRoot.resolve("old.dxf.zip"), "dxf", StandardCharsets.UTF_8);
        Files.writeString(currentRoot.resolve("old.gpkg.zip"), "gpkg", StandardCharsets.UTF_8);
        Files.writeString(currentRoot.resolve("old.shp.zip"), "shp", StandardCharsets.UTF_8);
        Path tempStageRoot = Files.createDirectories(tempDir.resolve("stage"));
        Files.writeString(tempStageRoot.resolve("new.xtf.zip"), "new", StandardCharsets.UTF_8);

        new LatestRotator().rotate(dataRoot, tempStageRoot, date());

        Path historyRoot = dataRoot.resolve("hist").resolve(DATE_TAG);
        assertTrue(Files.isRegularFile(historyRoot.resolve("old.xtf.zip")));
        assertFalse(Files.exists(historyRoot.resolve("old.dxf.zip")));
        assertFalse(Files.exists(historyRoot.resolve("old.gpkg.zip")));
        assertFalse(Files.exists(historyRoot.resolve("old.shp.zip")));
        assertEquals("new", Files.readString(dataRoot.resolve("aktuell").resolve("new.xtf.zip")));
    }

    @Test
    void deletesCurrentPublicationWhenHistoryTargetAlreadyExists() throws Exception {
        Path dataRoot = Files.createDirectories(tempDir.resolve("data"));
        Path currentRoot = Files.createDirectories(dataRoot.resolve("aktuell"));
        Files.writeString(currentRoot.resolve("stale.xtf.zip"), "stale", StandardCharsets.UTF_8);
        Path historyTarget = Files.createDirectories(dataRoot.resolve("hist").resolve(DATE_TAG));
        Files.writeString(historyTarget.resolve("kept.xtf.zip"), "kept", StandardCharsets.UTF_8);
        Path tempStageRoot = Files.createDirectories(tempDir.resolve("stage"));
        Files.writeString(tempStageRoot.resolve("new.xtf.zip"), "new", StandardCharsets.UTF_8);

        new LatestRotator().rotate(dataRoot, tempStageRoot, date());

        assertFalse(Files.exists(currentRoot.resolve("stale.xtf.zip")));
        assertEquals("kept", Files.readString(historyTarget.resolve("kept.xtf.zip")));
        assertEquals("new", Files.readString(dataRoot.resolve("aktuell").resolve("new.xtf.zip")));
    }

    private static Date date() throws Exception {
        return Grooming.getDateFormat().parse(DATE_TAG);
    }
}
