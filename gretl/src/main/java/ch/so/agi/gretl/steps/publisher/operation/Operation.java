package ch.so.agi.gretl.steps.publisher.operation;

import java.nio.file.Path;

/**
 * Common contract for publisher operations.
 *
 * @param <T> operation-specific configuration type
 */
public interface Operation<T extends OperationSpecificInformation> {
    default String getFullyQualifiedClassName() {
        return getClass().getName();
    }

    default String getHumanReadableName() {
        return getClass().getSimpleName();
    }

    void execute(Path inputDir, Path outputDir, T stepSpecificInformation) throws Exception;
}
