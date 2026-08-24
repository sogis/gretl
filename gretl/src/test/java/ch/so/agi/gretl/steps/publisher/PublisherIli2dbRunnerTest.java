package ch.so.agi.gretl.steps.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.ehi.ili2db.gui.Config;

class PublisherIli2dbRunnerTest {
    @TempDir
    private Path tempDir;

    @Test
    void configuresTemporaryLogInPublisherWorkDirectoryAndCleansItUp() throws Exception {
        Config config = new Config();
        Path workDirectory = tempDir.resolve("publisher-work");
        AtomicReference<Path> configuredLog = new AtomicReference<Path>();

        PublisherIli2dbRunner.run(config, workDirectory, configured -> {
            Path logFile = Path.of(configured.getLogfile());
            configuredLog.set(logFile);
            assertTrue(Files.isRegularFile(logFile));
            assertEquals(workDirectory, logFile.getParent());
        });

        assertFalse(Files.exists(configuredLog.get()));
    }

    @Test
    void cleansUpTemporaryLogWhenIli2dbFails() throws Exception {
        Config config = new Config();
        Path workDirectory = tempDir.resolve("publisher-work");
        AtomicReference<Path> configuredLog = new AtomicReference<Path>();

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> PublisherIli2dbRunner.run(config, workDirectory, configured -> {
                    configuredLog.set(Path.of(configured.getLogfile()));
                    throw new IllegalStateException("ili2db failed");
                }));

        assertEquals("ili2db failed", failure.getMessage());
        assertFalse(Files.exists(configuredLog.get()));
    }
}
