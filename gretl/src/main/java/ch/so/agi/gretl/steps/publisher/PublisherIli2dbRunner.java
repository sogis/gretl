package ch.so.agi.gretl.steps.publisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;

/**
 * Runs Publisher-owned ili2db operations with a private, temporary file log.
 *
 * <p>Without a configured log file ili2db creates its own console logger,
 * bypassing GRETL's EHI-to-Gradle logging bridge.</p>
 */
public final class PublisherIli2dbRunner {
    private PublisherIli2dbRunner() { }

    public static void run(Config config, Path workDirectory) throws Exception {
        run(config, workDirectory, configured -> Ili2db.run(configured, null));
    }

    static void run(Config config, Path workDirectory, Invocation invocation) throws Exception {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(workDirectory, "workDirectory must not be null");
        Objects.requireNonNull(invocation, "invocation must not be null");

        Files.createDirectories(workDirectory);
        Path logFile = Files.createTempFile(workDirectory, "ili2db-", ".log");
        config.setLogfile(logFile.toString());

        Throwable failure = null;
        try {
            invocation.run(config);
        } catch (Exception | Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                Files.deleteIfExists(logFile);
            } catch (IOException cleanupFailure) {
                if (failure != null) {
                    failure.addSuppressed(cleanupFailure);
                } else {
                    throw cleanupFailure;
                }
            }
        }
    }

    @FunctionalInterface
    interface Invocation {
        void run(Config config) throws Exception;
    }
}
