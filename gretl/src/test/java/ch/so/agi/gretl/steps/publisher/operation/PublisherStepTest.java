package ch.so.agi.gretl.steps.publisher.operation;

import ch.so.agi.gretl.steps.publisher.PublisherStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        SideEffectOperation.reset();
        MultiInputOperation.reset();
    }

    @Test
    public void execute_chainsInputAndOutputDirectories() throws Exception {
        Path inputDir = Files.createDirectory(tempDir.resolve("input"));
        Path outputDir = tempDir.resolve("output");

        PublisherStep publisherStep = new PublisherStep();
        List<Operation<?>> operations = new ArrayList<>();
        operations.add(new FirstOperation());
        operations.add(new SecondOperation());

        Path finalOutputDir = publisherStep.execute(inputDir, outputDir, operations);

        Path firstOutputDir = outputDir.resolve("001-" + FirstOperation.class.getName());
        Path secondOutputDir = outputDir.resolve("002-" + SecondOperation.class.getName());

        assertEquals(inputDir, FirstOperation.receivedParameters.getInputDir());
        assertEquals(firstOutputDir, FirstOperation.receivedParameters.getOutputDirRequired());
        assertEquals(firstOutputDir, SecondOperation.receivedParameters.getInputDir());
        assertEquals(secondOutputDir, SecondOperation.receivedParameters.getOutputDirRequired());
        assertEquals(secondOutputDir, finalOutputDir);
        assertTrue(Files.exists(firstOutputDir));
        assertTrue(Files.exists(secondOutputDir));
        assertEquals("first", FirstOperation.receivedParameters.getValue());
        assertEquals("second", SecondOperation.receivedParameters.getValue());
    }

    @Test
    public void execute_allowsOperationsWithoutInputOrOutput() throws Exception {
        Path inputDir = Files.createDirectory(tempDir.resolve("input"));
        Path outputDir = tempDir.resolve("output");

        PublisherStep publisherStep = new PublisherStep();
        List<Operation<?>> operations = List.of(new SideEffectOperation());

        Path finalOutputDir = publisherStep.execute(inputDir, outputDir, operations);

        assertTrue(SideEffectOperation.receivedParameters.getInputDirs().isEmpty());
        assertTrue(SideEffectOperation.receivedParameters.getOutputDir().isEmpty());
        assertEquals(outputDir, finalOutputDir);
    }

    @Test
    public void execute_supportsNamedInputsWithoutOutput() throws Exception {
        Path inputDir = Files.createDirectory(tempDir.resolve("input"));
        Path referenceDir = Files.createDirectory(tempDir.resolve("reference"));
        Path outputDir = tempDir.resolve("output");

        PublisherStep publisherStep = new PublisherStep();
        List<Operation<?>> operations = List.of(new MultiInputOperation(referenceDir));

        Path finalOutputDir = publisherStep.execute(inputDir, outputDir, operations);

        assertEquals(inputDir, MultiInputOperation.receivedParameters.getRequiredInputDir("source"));
        assertEquals(referenceDir, MultiInputOperation.receivedParameters.getRequiredInputDir("reference"));
        assertTrue(MultiInputOperation.receivedParameters.getOutputDir().isEmpty());
        assertEquals(outputDir, finalOutputDir);
    }

    @Test
    public void singleInputSingleOutputParameters_requireNonNullDirectories() {
        assertThrows(NullPointerException.class,
                () -> new TestSingleInputSingleOutputParameters(null, tempDir.resolve("output")) {});
        assertThrows(NullPointerException.class,
                () -> new TestSingleInputSingleOutputParameters(tempDir.resolve("input"), null) {});
    }

    @Test
    public void operation_defaultsUseTheImplementingClassName() {
        DefaultNamingOperation operation = new DefaultNamingOperation();

        assertEquals(DefaultNamingOperation.class.getName(), operation.getFullyQualifiedClassName());
        assertEquals(DefaultNamingOperation.class.getSimpleName(), operation.getHumanReadableName());
    }

    public static final class FirstOperation implements Operation<FirstParameters> {
        private static FirstParameters receivedParameters;

        @Override
        public FirstParameters resolveParameters(Path inputDir, Path outputDir, int executionOrder) {
            return new FirstParameters(inputDir,
                    outputDir.resolve(String.format("%03d-%s", executionOrder, getFullyQualifiedClassName())),
                    "first");
        }

        @Override
        public void execute(FirstParameters operationParameters) {
            receivedParameters = operationParameters;
        }

        private static void reset() {
            receivedParameters = null;
        }
    }

    public static final class SecondOperation implements Operation<SecondParameters> {
        private static SecondParameters receivedParameters;

        @Override
        public SecondParameters resolveParameters(Path inputDir, Path outputDir, int executionOrder) {
            return new SecondParameters(inputDir,
                    outputDir.resolve(String.format("%03d-%s", executionOrder, getFullyQualifiedClassName())),
                    "second");
        }

        @Override
        public void execute(SecondParameters operationParameters) {
            receivedParameters = operationParameters;
        }

        private static void reset() {
            receivedParameters = null;
        }
    }

    public static final class SideEffectOperation implements Operation<EmptyParameters> {
        private static EmptyParameters receivedParameters;

        @Override
        public EmptyParameters resolveParameters(Path inputDir, Path outputDir, int executionOrder) {
            return new EmptyParameters("side-effect");
        }

        @Override
        public void execute(EmptyParameters operationParameters) {
            receivedParameters = operationParameters;
        }

        private static void reset() {
            receivedParameters = null;
        }
    }

    public static final class MultiInputOperation implements Operation<MultiInputParameters> {
        private static MultiInputParameters receivedParameters;
        private final Path referenceDir;

        public MultiInputOperation(Path referenceDir) {
            this.referenceDir = Objects.requireNonNull(referenceDir, "referenceDir must not be null");
        }

        @Override
        public MultiInputParameters resolveParameters(Path inputDir, Path outputDir, int executionOrder) {
            Map<String, Path> inputDirs = new LinkedHashMap<>();
            inputDirs.put("source", inputDir);
            inputDirs.put("reference", referenceDir);
            return new MultiInputParameters(inputDirs, "multi");
        }

        @Override
        public void execute(MultiInputParameters operationParameters) {
            receivedParameters = operationParameters;
        }

        private static void reset() {
            receivedParameters = null;
        }
    }

    public static final class DefaultNamingOperation implements Operation<EmptyParameters> {
        @Override
        public EmptyParameters resolveParameters(Path inputDir, Path outputDir, int executionOrder) {
            return new EmptyParameters("default");
        }

        @Override
        public void execute(EmptyParameters operationParameters) {
        }
    }

    public static abstract class TestSingleInputSingleOutputParameters extends AbstractSingleInputSingleOutputParameters {
        protected TestSingleInputSingleOutputParameters(Path inputDir, Path outputDir) {
            super(inputDir, outputDir);
        }
    }

    public static final class FirstParameters extends AbstractSingleInputSingleOutputParameters {
        private final String value;

        public FirstParameters(Path inputDir, Path outputDir, String value) {
            super(inputDir, outputDir);
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class SecondParameters extends AbstractSingleInputSingleOutputParameters {
        private final String value;

        public SecondParameters(Path inputDir, Path outputDir, String value) {
            super(inputDir, outputDir);
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class EmptyParameters implements OperationParameters {
        private final String value;

        public EmptyParameters(String value) {
            this.value = value;
        }

        @Override
        public Map<String, Path> getInputDirs() {
            return Map.of();
        }

        @Override
        public Optional<Path> getOutputDir() {
            return Optional.empty();
        }

        public String getValue() {
            return value;
        }
    }

    public static final class MultiInputParameters implements OperationParameters {
        private final Map<String, Path> inputDirs;
        private final String value;

        public MultiInputParameters(Map<String, Path> inputDirs, String value) {
            this.inputDirs = Map.copyOf(inputDirs);
            this.value = value;
        }

        @Override
        public Map<String, Path> getInputDirs() {
            return inputDirs;
        }

        @Override
        public Optional<Path> getOutputDir() {
            return Optional.empty();
        }

        public String getValue() {
            return value;
        }
    }
}
