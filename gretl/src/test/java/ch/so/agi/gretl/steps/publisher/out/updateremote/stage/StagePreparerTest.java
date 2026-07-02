package ch.so.agi.gretl.steps.publisher.out.updateremote.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import ch.so.agi.gretl.util.Grooming;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StagePreparerTest {
    private static final String DATE_TAG = "2026-06-23";

    @TempDir
    Path tempDir;

    @Test
    void createsDataRootAndTempStageRoot() throws Exception {
        Path targetRoot = tempDir.resolve("remote");

        RemoteStagePaths paths = new StagePreparer().prepare(targetRoot, "ch.so.agi.demo", date());

        assertEquals(targetRoot.resolve("ch.so.agi.demo"), paths.getDataRoot());
        assertTrue(Files.isDirectory(paths.getDataRoot()));
        assertTrue(Files.isDirectory(paths.getTempStageRoot()));
        assertTrue(paths.getTempStageRoot().getFileName().toString().startsWith("." + DATE_TAG + "-"));
    }

    private static Date date() throws Exception {
        return Grooming.getDateFormat().parse(DATE_TAG);
    }
}
