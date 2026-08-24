package ch.so.agi.gretl.steps.publisher;

import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.Ehi2GretlAdapter;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

/**
 * Executes a planned publisher operation sequence and emits summary/progress logs.
 */
public class OpSequenceRunner {
    private final GretlLogger log;
    private final PublisherLogFormatter logFormatter;

    public OpSequenceRunner() {
        this(LogEnvironment.getLogger(OpSequenceRunner.class));
    }

    public OpSequenceRunner(GretlLogger log) {
        this.log = Objects.requireNonNull(log, "log must not be null");
        this.logFormatter = new PublisherLogFormatter();
    }

    public void run(List<? extends Operation> operations) throws Exception {
        Objects.requireNonNull(operations, "operations must not be null");

        log.info(logFormatter.plan(operations));
        for (Operation operation : operations) {
            runOperation(operation);
        }
    }

    private void runOperation(Operation operation) throws Exception {
        Objects.requireNonNull(operation, "operation must not be null");
        // Validation and other EHI tools can restore the standard console
        // listener. Keep the configured bridge active for every Publisher
        // operation; ili2db's own temporary logger is handled separately.
        Ehi2GretlAdapter.init();
        operation.execute();
        log.info(operation.getHumanReadableName() + ": " + operation.getSuccessLogDetail());
    }
}
