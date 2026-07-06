package ch.so.agi.gretl.steps.publisher;

import java.util.Objects;
import java.util.function.Supplier;

import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

/**
 * One planned publisher step consisting of an operation and lazily resolved parameters.
 */
public final class OpSequenceStep {
    private final Operation<? extends OperationParameters> operation;
    private final Supplier<? extends OperationParameters> parametersSupplier;

    private OpSequenceStep(Operation<? extends OperationParameters> operation,
            Supplier<? extends OperationParameters> parametersSupplier) {
        this.operation = Objects.requireNonNull(operation, "operation must not be null");
        this.parametersSupplier = Objects.requireNonNull(parametersSupplier, "parametersSupplier must not be null");
    }

    public static <P extends OperationParameters> OpSequenceStep of(Operation<P> operation, Supplier<P> parametersSupplier) {
        return new OpSequenceStep(operation, parametersSupplier);
    }

    public Operation<? extends OperationParameters> getOperation() {
        return operation;
    }

    public OperationParameters resolveParameters() {
        return parametersSupplier.get();
    }
}
