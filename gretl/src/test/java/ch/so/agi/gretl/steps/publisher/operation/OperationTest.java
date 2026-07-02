package ch.so.agi.gretl.steps.publisher.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OperationTest {
    @Test
    void defaultSuccessLogMessageUsesHumanReadableName() {
        assertEquals("TestOperation completed successfully", new TestOperation().getSuccessLogMessage());
    }

    private static final class TestOperation implements Operation<OperationParameters> {
        @Override
        public void execute(OperationParameters operationParameters) {
        }
    }
}
