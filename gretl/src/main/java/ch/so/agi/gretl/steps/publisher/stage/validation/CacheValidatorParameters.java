package ch.so.agi.gretl.steps.publisher.stage.validation;

import java.nio.file.Path;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleDirectoryParameters;

public final class CacheValidatorParameters extends AbstractSingleDirectoryParameters {
    private final boolean throwOnValidationError;
    private final boolean overwriteExistingLog;

    private CacheValidatorParameters(Path cachePath, boolean throwOnValidationError, boolean overwriteExistingLog) {
        super(cachePath);
        this.throwOnValidationError = throwOnValidationError;
        this.overwriteExistingLog = overwriteExistingLog;
    }

    public static CacheValidatorParameters of(Path cachePath, boolean throwOnValidationError,
            boolean overwriteExistingLog) {
        return new CacheValidatorParameters(cachePath, throwOnValidationError, overwriteExistingLog);
    }

    public Path getCachePath() {
        return getDirectory();
    }

    public boolean isThrowOnValidationError() {
        return throwOnValidationError;
    }

    public boolean isOverwriteExistingLog() {
        return overwriteExistingLog;
    }
}
