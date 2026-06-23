package ch.so.agi.gretl.steps.publisher.operation;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Common contract for publisher operations.
 */
public interface Operation<P extends OperationParameters> {
    default String getFullyQualifiedClassName() {
        return getClass().getName();
    }

    default String getHumanReadableName() {
        return getClass().getSimpleName();
    }

    P resolveParameters(Path inputDir, Path outputDir, int executionOrder) throws IOException;

    void execute(P operationParameters) throws Exception;
}
