package ch.so.agi.gretl.steps.publisher;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

/**
 * Executes a planned publisher operation sequence and emits summary/progress logs.
 */
public class OpSequenceRunner {
    private final GretlLogger log;

    public OpSequenceRunner() {
        this(LogEnvironment.getLogger(OpSequenceRunner.class));
    }

    public OpSequenceRunner(GretlLogger log) {
        this.log = Objects.requireNonNull(log, "log must not be null");
    }

    public void run(List<OpSequenceStep> steps) throws Exception {
        Objects.requireNonNull(steps, "steps must not be null");

        log.lifecycle(buildSummary(steps));
        for (OpSequenceStep step : steps) {
            runStep(step);
        }
    }

    private String buildSummary(List<OpSequenceStep> steps) {
        String summary = steps.stream()
                .map(step -> step.getOperation().getHumanReadableName())
                .collect(Collectors.joining(", "));
        return "Publication will perform these steps: " + summary;
    }

    private void runStep(OpSequenceStep step) throws Exception {
        Objects.requireNonNull(step, "step must not be null");
        Operation<? extends OperationParameters> operation = step.getOperation();
        OperationParameters parameters = step.resolveParameters();
        executeOperation(operation, parameters);
        log.info(operation.getSuccessLogMessage());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void executeOperation(Operation operation, OperationParameters parameters) throws Exception {
        operation.execute(parameters);
    }
}
