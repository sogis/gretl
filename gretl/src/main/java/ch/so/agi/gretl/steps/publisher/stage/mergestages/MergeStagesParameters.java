package ch.so.agi.gretl.steps.publisher.stage.mergestages;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

public final class MergeStagesParameters implements OperationParameters {
    private final List<Path> inputDirs;
    private final Path outputDir;

    private MergeStagesParameters(List<Path> inputDirs, Path outputDir) {
        Objects.requireNonNull(inputDirs, "inputDirs must not be null");
        if (inputDirs.isEmpty()) {
            throw new IllegalArgumentException("inputDirs must not be empty");
        }
        this.inputDirs = Collections.unmodifiableList(new ArrayList<Path>(inputDirs));
        this.outputDir = Objects.requireNonNull(outputDir, "outputDir must not be null");
    }

    public static MergeStagesParameters of(List<Path> inputDirs, Path outputDir) {
        return new MergeStagesParameters(inputDirs, outputDir);
    }

    public List<Path> getInputDirs() {
        return inputDirs;
    }

    public Path getOutputDir() {
        return outputDir;
    }
}
