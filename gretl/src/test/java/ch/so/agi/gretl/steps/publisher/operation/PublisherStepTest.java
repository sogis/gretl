package ch.so.agi.gretl.steps.publisher.operation;

import ch.so.agi.gretl.steps.publisher.PublisherStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublisherStepTest {
    @TempDir
    public Path tempDir;

    @BeforeEach
    public void reset() {
        FirstOperation.reset();
        SecondOperation.reset();
    }

    @Test
    public void execute_chainsInputAndOutputDirectories() throws Exception {
        Path inputDir = Files.createDirectory(tempDir.resolve("input"));
        Path outputDir = tempDir.resolve("output");

        PublisherStep publisherStep = new PublisherStep();
        List<OperationPlan<?>> operationPlans = new ArrayList<>();
        operationPlans.add(new OperationPlan<>(FirstOperation.class.getName(), new ExampleOperationInfo("first")));
        operationPlans.add(new OperationPlan<>(SecondOperation.class.getName(), new ExampleOperationInfo("second")));

        Path finalOutputDir = publisherStep.execute(inputDir, outputDir, operationPlans);

        Path firstOutputDir = outputDir.resolve("001-" + FirstOperation.class.getName());
        Path secondOutputDir = outputDir.resolve("002-" + SecondOperation.class.getName());

        assertEquals(inputDir, FirstOperation.receivedInputDir);
        assertEquals(firstOutputDir, FirstOperation.receivedOutputDir);
        assertEquals(firstOutputDir, SecondOperation.receivedInputDir);
        assertEquals(secondOutputDir, SecondOperation.receivedOutputDir);
        assertEquals(secondOutputDir, finalOutputDir);
        assertTrue(Files.exists(firstOutputDir));
        assertTrue(Files.exists(secondOutputDir));
        assertEquals("first", FirstOperation.receivedInfo.getValue());
        assertEquals("second", SecondOperation.receivedInfo.getValue());
    }

    @Test
    public void execute_rejectsClassesThatDoNotImplementOperation() {
        PublisherStep publisherStep = new PublisherStep();
        List<OperationPlan<?>> operationPlans = List.of(new OperationPlan<>(NotAnOperation.class.getName(), new ExampleOperationInfo("bad")));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> publisherStep.execute(tempDir.resolve("input"), tempDir.resolve("output"), operationPlans));

        assertTrue(exception.getMessage().contains(NotAnOperation.class.getName()));
    }

    @Test
    public void operation_defaultsUseTheImplementingClassName() {
        DefaultNamingOperation operation = new DefaultNamingOperation();

        assertEquals(DefaultNamingOperation.class.getName(), operation.getFullyQualifiedClassName());
        assertEquals(DefaultNamingOperation.class.getSimpleName(), operation.getHumanReadableName());
    }

    public static final class ExampleOperationInfo implements OperationSpecificInformation {
        private final String value;

        public ExampleOperationInfo(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class FirstOperation implements Operation<ExampleOperationInfo> {
        private static Path receivedInputDir;
        private static Path receivedOutputDir;
        private static ExampleOperationInfo receivedInfo;

        @Override
        public void execute(Path inputDir, Path outputDir, ExampleOperationInfo operationSpecificInformation) throws Exception {
            receivedInputDir = inputDir;
            receivedOutputDir = outputDir;
            receivedInfo = operationSpecificInformation;
        }

        private static void reset() {
            receivedInputDir = null;
            receivedOutputDir = null;
            receivedInfo = null;
        }
    }

    public static final class SecondOperation implements Operation<ExampleOperationInfo> {
        private static Path receivedInputDir;
        private static Path receivedOutputDir;
        private static ExampleOperationInfo receivedInfo;

        @Override
        public void execute(Path inputDir, Path outputDir, ExampleOperationInfo operationSpecificInformation) throws Exception {
            receivedInputDir = inputDir;
            receivedOutputDir = outputDir;
            receivedInfo = operationSpecificInformation;
        }

        private static void reset() {
            receivedInputDir = null;
            receivedOutputDir = null;
            receivedInfo = null;
        }
    }

    public static final class DefaultNamingOperation implements Operation<ExampleOperationInfo> {
        @Override
        public void execute(Path inputDir, Path outputDir, ExampleOperationInfo operationSpecificInformation) {
        }
    }

    public static final class NotAnOperation {
        public NotAnOperation() {
        }
    }
}
