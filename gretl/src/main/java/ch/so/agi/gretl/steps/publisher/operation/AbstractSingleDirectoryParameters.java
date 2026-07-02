package ch.so.agi.gretl.steps.publisher.operation;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Base class for the common case of one required directory parameter.
 */
public abstract class AbstractSingleDirectoryParameters implements OperationParameters {
    private final Path directory;

    protected AbstractSingleDirectoryParameters(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory must not be null");
    }

    public Path getDirectory() {
        return directory;
    }
}
