package ch.so.agi.gretl.steps.publisher.out.updateremote.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StagePreparerTest {
    @TempDir
    Path tempDir;

    @Test
    void createsDataRootAndTempStageRoot() throws Exception {
        Path targetRoot = tempDir.resolve("remote");

        RemoteStagePaths paths = new StagePreparer().prepare(targetRoot, "ch.so.agi.demo",
                new GregorianCalendar(2026, Calendar.JUNE, 23).getTime());

        assertEquals(targetRoot.resolve("ch.so.agi.demo"), paths.getDataRoot());
        assertTrue(Files.isDirectory(paths.getDataRoot()));
        assertTrue(Files.isDirectory(paths.getTempStageRoot()));
        assertTrue(paths.getTempStageRoot().getFileName().toString().startsWith(".2026-06-23-"));
    }
}
