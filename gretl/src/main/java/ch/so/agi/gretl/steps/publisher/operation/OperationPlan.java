package ch.so.agi.gretl.steps.publisher.operation;

import java.util.Objects;

/**
 * Describes one planned operation execution.
 *
 * @param <T> operation-specific configuration type
 */
public final class OperationPlan<T extends OperationSpecificInformation> {
    private final String fullyQualifiedClassName;
    private final T operationSpecificInformation;

    public OperationPlan(String fullyQualifiedClassName, T operationSpecificInformation) {
        this.fullyQualifiedClassName = Objects.requireNonNull(fullyQualifiedClassName, "fullyQualifiedClassName must not be null");
        if (fullyQualifiedClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullyQualifiedClassName must not be empty");
        }
        this.operationSpecificInformation = operationSpecificInformation;
    }

    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    public T getOperationSpecificInformation() {
        return operationSpecificInformation;
    }
}
