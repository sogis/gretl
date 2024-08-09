package ch.so.agi.gretl.util;

import org.gradle.testkit.runner.TaskOutcome;

public  class TaskOutcomeConverter {
    public static int convertTaskOutcomeToInteger(TaskOutcome taskOutcome){
        switch (taskOutcome) {
            case SUCCESS:
                return 0;
            case FAILED:
                return 1;
            case UP_TO_DATE:
                return 2;
            case SKIPPED:
                return 3;
            case FROM_CACHE:
                return 4;
            case NO_SOURCE:
                return 5;
            default:
                throw new IllegalArgumentException("Unknown TaskOutcome: " + taskOutcome);
        }
    }
}
