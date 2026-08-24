package ch.so.agi.gretl.steps.publisher.stage.validation;

import java.nio.file.Path;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleDirectoryParameters;

public final class CacheValidatorParameters extends AbstractSingleDirectoryParameters {
    private final boolean throwOnValidationError;
    private final boolean overwriteExistingLog;
    private final String customModelDir;

    private CacheValidatorParameters(Path cachePath, boolean throwOnValidationError, boolean overwriteExistingLog,
            String customModelDir) {
        super(cachePath);
        this.throwOnValidationError = throwOnValidationError;
        this.overwriteExistingLog = overwriteExistingLog;
        this.customModelDir = customModelDir;
    }

    public static CacheValidatorParameters of(Path cachePath, boolean throwOnValidationError,
            boolean overwriteExistingLog) {
        return of(cachePath, throwOnValidationError, overwriteExistingLog, null);
    }

    public static CacheValidatorParameters of(Path cachePath, boolean throwOnValidationError,
            boolean overwriteExistingLog, String customModelDir) {
        return new CacheValidatorParameters(cachePath, throwOnValidationError, overwriteExistingLog, customModelDir);
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

    public String getCustomModelDir() {
        return customModelDir;
    }
}
