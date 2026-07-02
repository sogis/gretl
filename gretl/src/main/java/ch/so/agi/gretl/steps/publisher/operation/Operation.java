package ch.so.agi.gretl.steps.publisher.operation;

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

    default String getSuccessLogMessage() {
        return getHumanReadableName() + " completed successfully";
    }

    void execute(P operationParameters) throws Exception;
}
