package ch.so.agi.gretl.steps.publisher;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Responsibility: provide the public entry point for the modular Publisher
 * implementation, resolve and execute the configured operations, and keep
 * callers isolated from individual submodule classes.
 */
public class PublisherStep {
    private final GretlLogger log;

    public PublisherStep() {
        this.log = LogEnvironment.getLogger(getClass());
    }

    public Path execute(Path inputDir, Path outputDir, List<? extends Operation<?>> operations) throws Exception {
        Objects.requireNonNull(inputDir, "inputDir must not be null");
        Objects.requireNonNull(outputDir, "outputDir must not be null");
        Objects.requireNonNull(operations, "operations must not be null");

        Path currentInputDir = inputDir;
        Path currentOutputDir = outputDir;

        for (int executionOrder = 0; executionOrder < operations.size(); executionOrder++) {
            Operation<?> operation = operations.get(executionOrder);
            String operationName = operation.getHumanReadableName();
            OperationParameters executionParameters = operation.resolveParameters(currentInputDir, outputDir, executionOrder + 1);

            log.lifecycle("Start " + operationName + " (" + operation.getFullyQualifiedClassName() + ")");
            try {
                if (executionParameters.getOutputDir().isPresent()) {
                    createDirectories(executionParameters.getOutputDir().get());
                }
                executeOperation(operation, executionParameters);
            } catch (Exception exception) {
                log.error("Operation failed: " + operationName, exception);
                throw exception;
            } finally {
                log.lifecycle("Finish " + operationName + " (" + operation.getFullyQualifiedClassName() + ")");
            }

            if (executionParameters.getOutputDir().isPresent()) {
                currentInputDir = executionParameters.getOutputDir().get();
                currentOutputDir = currentInputDir;
            }
        }

        return currentOutputDir;
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("failed to create directory " + directory, e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void executeOperation(Operation<?> operation, OperationParameters operationParameters) throws Exception {
        Operation rawOperation = operation;
        rawOperation.execute(operationParameters);
    }
}
