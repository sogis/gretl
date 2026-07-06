package ch.so.agi.gretl.steps.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

class OpSequenceRunnerTest {
    @Test
    void runsStepsInOrderAndLogsSummaryAndCompletion() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        List<String> executionOrder = new ArrayList<>();
        TestParameters firstParameters = new TestParameters("first");
        TestParameters secondParameters = new TestParameters("second");

        OpSequenceStep firstStep = OpSequenceStep.of(
                new RecordingOperation("Exporter", executionOrder, "exported"),
                () -> firstParameters);
        OpSequenceStep secondStep = OpSequenceStep.of(
                new RecordingOperation("Packer", executionOrder, "packed"),
                () -> secondParameters);

        new OpSequenceRunner(logger).run(List.of(firstStep, secondStep));

        assertEquals(List.of("Exporter", "Packer"), executionOrder);
        assertEquals(List.of("Publication will perform these steps: Exporter, Packer"), logger.lifecycleMessages);
        assertEquals(List.of("exported", "packed"), logger.infoMessages);
    }

    @Test
    void resolvesParametersLazilyDuringExecution() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        AtomicInteger supplierCalls = new AtomicInteger();
        List<OperationParameters> seenParameters = new ArrayList<>();
        TestParameters parameters = new TestParameters("lazy");
        OpSequenceStep step = OpSequenceStep.of(new ParameterCapturingOperation(seenParameters), () -> {
            supplierCalls.incrementAndGet();
            return parameters;
        });

        assertEquals(0, supplierCalls.get());

        new OpSequenceRunner(logger).run(List.of(step));

        assertEquals(1, supplierCalls.get());
        assertEquals(1, seenParameters.size());
        assertSame(parameters, seenParameters.get(0));
    }

    @Test
    void stopsAfterFirstFailureWithoutLoggingCompletionForFailingStep() {
        RecordingLogger logger = new RecordingLogger();
        List<String> executionOrder = new ArrayList<>();
        RuntimeException expected = new RuntimeException("boom");
        OpSequenceStep firstStep = OpSequenceStep.of(
                new RecordingOperation("Exporter", executionOrder, "exported"),
                () -> new TestParameters("first"));
        OpSequenceStep failingStep = OpSequenceStep.of(
                new FailingOperation("Packer", executionOrder, expected),
                () -> new TestParameters("second"));
        OpSequenceStep skippedStep = OpSequenceStep.of(
                new RecordingOperation("Writer", executionOrder, "written"),
                () -> new TestParameters("third"));

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> new OpSequenceRunner(logger).run(List.of(firstStep, failingStep, skippedStep)));

        assertSame(expected, actual);
        assertEquals(List.of("Exporter", "Packer"), executionOrder);
        assertEquals(List.of("Publication will perform these steps: Exporter, Packer, Writer"), logger.lifecycleMessages);
        assertEquals(List.of("exported"), logger.infoMessages);
    }

    private static final class TestParameters implements OperationParameters {
        private final String name;

        private TestParameters(String name) {
            this.name = name;
        }
    }

    private static final class RecordingOperation implements Operation<TestParameters> {
        private final String name;
        private final List<String> executionOrder;
        private final String successMessage;

        private RecordingOperation(String name, List<String> executionOrder, String successMessage) {
            this.name = name;
            this.executionOrder = executionOrder;
            this.successMessage = successMessage;
        }

        @Override
        public String getHumanReadableName() {
            return name;
        }

        @Override
        public String getSuccessLogMessage() {
            return successMessage;
        }

        @Override
        public void execute(TestParameters operationParameters) {
            executionOrder.add(name);
        }
    }

    private static final class ParameterCapturingOperation implements Operation<TestParameters> {
        private final List<OperationParameters> seenParameters;

        private ParameterCapturingOperation(List<OperationParameters> seenParameters) {
            this.seenParameters = seenParameters;
        }

        @Override
        public void execute(TestParameters operationParameters) {
            seenParameters.add(operationParameters);
        }
    }

    private static final class FailingOperation implements Operation<TestParameters> {
        private final String name;
        private final List<String> executionOrder;
        private final RuntimeException exception;

        private FailingOperation(String name, List<String> executionOrder, RuntimeException exception) {
            this.name = name;
            this.executionOrder = executionOrder;
            this.exception = exception;
        }

        @Override
        public String getHumanReadableName() {
            return name;
        }

        @Override
        public void execute(TestParameters operationParameters) {
            executionOrder.add(name);
            throw exception;
        }
    }

    private static final class RecordingLogger implements GretlLogger {
        private final List<String> infoMessages = new ArrayList<>();
        private final List<String> lifecycleMessages = new ArrayList<>();

        @Override
        public void info(String msg) {
            infoMessages.add(msg);
        }

        @Override
        public void debug(String msg) {
        }

        @Override
        public void error(String msg, Throwable thrown) {
        }

        @Override
        public void lifecycle(String msg) {
            lifecycleMessages.add(msg);
        }
    }
}
