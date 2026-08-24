package ch.so.agi.gretl.steps.publisher.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OperationTest {
    @Test
    void successDetailDoesNotRepeatHumanReadableName() {
        TestOperation operation = new TestOperation();
        assertEquals("Test operation", operation.getHumanReadableName());
        assertEquals("completed its work", operation.getSuccessLogDetail());
    }

    private static final class TestOperation implements Operation {
        @Override
        public String getHumanReadableName() { return "Test operation"; }
        @Override
        public String getSuccessLogDetail() { return "completed its work"; }
        @Override
        public void execute() { }
    }
}
