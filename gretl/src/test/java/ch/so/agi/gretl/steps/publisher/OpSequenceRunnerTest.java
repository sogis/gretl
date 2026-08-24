package ch.so.agi.gretl.steps.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.steps.publisher.operation.Operation;

class OpSequenceRunnerTest {
    @Test
    void runsOperationsInOrderAndPrefixesTheirSuccessDetails() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        List<String> executed = new ArrayList<>();
        new OpSequenceRunner(logger).run(List.of(new RecordingOperation("Export", "exported 3 objects", executed),
                new RecordingOperation("Package", "created 1 archive", executed)));
        assertEquals(List.of("Export", "Package"), executed);
        assertEquals(List.of("Publication plan: Export, Package", "Export: exported 3 objects", "Package: created 1 archive"), logger.info);
    }

    @Test
    void doesNotLogCompletionForFailingOperation() {
        RecordingLogger logger = new RecordingLogger();
        RuntimeException failure = new RuntimeException("boom");
        RuntimeException actual = assertThrows(RuntimeException.class, () -> new OpSequenceRunner(logger).run(List.of(
                new RecordingOperation("Export", "exported", new ArrayList<>()), new FailingOperation(failure))));
        assertSame(failure, actual);
        assertEquals(List.of("Publication plan: Export, Package", "Export: exported"), logger.info);
    }

    private static final class RecordingOperation implements Operation {
        private final String name, detail; private final List<String> executed;
        RecordingOperation(String name, String detail, List<String> executed) { this.name = name; this.detail = detail; this.executed = executed; }
        public String getHumanReadableName() { return name; }
        public String getSuccessLogDetail() { return detail; }
        public void execute() { executed.add(name); }
    }
    private static final class FailingOperation implements Operation {
        private final RuntimeException failure; FailingOperation(RuntimeException failure) { this.failure = failure; }
        public String getHumanReadableName() { return "Package"; }
        public String getSuccessLogDetail() { return "packaged"; }
        public void execute() { throw failure; }
    }
    private static final class RecordingLogger implements GretlLogger {
        final List<String> info = new ArrayList<>();
        public void info(String msg) { info.add(msg); } public void debug(String msg) { } public void lifecycle(String msg) { }
        public void error(String msg, Throwable thrown) { }
    }
}
