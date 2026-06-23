package ch.so.agi.gretl.steps.publisher.operation;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Base class for the common case of one required input directory and one required output directory.
 */
public abstract class AbstractSingleInputSingleOutputParameters implements OperationParameters {
    private final Path inputDir;
    private final Path outputDir;

    protected AbstractSingleInputSingleOutputParameters(Path inputDir, Path outputDir) {
        this.inputDir = Objects.requireNonNull(inputDir, "inputDir must not be null");
        this.outputDir = Objects.requireNonNull(outputDir, "outputDir must not be null");
    }

    public Path getInputDir() {
        return inputDir;
    }

    public Path getOutputDir() {
        return outputDir;
    }
}
