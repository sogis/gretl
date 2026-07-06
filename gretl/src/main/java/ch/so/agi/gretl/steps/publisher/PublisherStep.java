package ch.so.agi.gretl.steps.publisher;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.operation.OperationPlan;
import ch.so.agi.gretl.steps.publisher.operation.OperationSpecificInformation;

import java.io.IOException;
import java.lang.reflect.Constructor;
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

    public Path execute(Path inputDir, Path outputDir, List<OperationPlan<?>> operationPlans) throws Exception {
        Objects.requireNonNull(inputDir, "inputDir must not be null");
        Objects.requireNonNull(outputDir, "outputDir must not be null");
        Objects.requireNonNull(operationPlans, "operationPlans must not be null");

        Path currentInputDir = inputDir;
        Path currentOutputDir = outputDir;

        for (int executionOrder = 0; executionOrder < operationPlans.size(); executionOrder++) {
            OperationPlan<?> operationPlan = operationPlans.get(executionOrder);
            Operation<?> operation = instantiateOperation(operationPlan.getFullyQualifiedClassName());
            String operationName = operation.getHumanReadableName();
            currentOutputDir = createOperationOutputDir(outputDir, operationPlan.getFullyQualifiedClassName(), executionOrder + 1);

            log.lifecycle("Start " + operationName + " (" + operationPlan.getFullyQualifiedClassName() + ")");
            try {
                executeOperation(operation, currentInputDir, currentOutputDir, operationPlan.getOperationSpecificInformation());
            } catch (Exception exception) {
                log.error("Operation failed: " + operationName, exception);
                throw exception;
            } finally {
                log.lifecycle("Finish " + operationName + " (" + operationPlan.getFullyQualifiedClassName() + ")");
            }

            currentInputDir = currentOutputDir;
        }

        return currentOutputDir;
    }

    private Operation<?> instantiateOperation(String fullyQualifiedClassName) throws Exception {
        Class<?> operationClass = Class.forName(fullyQualifiedClassName);
        if (!Operation.class.isAssignableFrom(operationClass)) {
            throw new IllegalArgumentException("Class does not implement Operation: " + fullyQualifiedClassName);
        }

        Constructor<?> constructor = operationClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object operation = constructor.newInstance();
        return (Operation<?>) operation;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void executeOperation(Operation<?> operation, Path inputDir, Path outputDir, OperationSpecificInformation operationSpecificInformation) throws Exception {
        Operation rawOperation = operation;
        rawOperation.execute(inputDir, outputDir, operationSpecificInformation);
    }

    private Path createOperationOutputDir(Path outputDir, String fullyQualifiedClassName, int executionOrder) throws IOException {
        Path operationOutputDir = outputDir.resolve(createOperationOutputFolderName(fullyQualifiedClassName, executionOrder));
        Files.createDirectories(operationOutputDir);
        return operationOutputDir;
    }

    private String createOperationOutputFolderName(String fullyQualifiedClassName, int executionOrder) {
        return String.format("%03d-%s", executionOrder, fullyQualifiedClassName);
    }
}
