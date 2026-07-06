package ch.so.agi.gretl.steps.publisher.stage.validation;

import java.nio.file.Path;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

public final class ValidationConfigSeederParameters implements OperationParameters {
    private final Path validationConfig;
    private final Path cacheDir;

    private ValidationConfigSeederParameters(Path validationConfig, Path cacheDir) {
        this.validationConfig = Objects.requireNonNull(validationConfig, "validationConfig must not be null");
        this.cacheDir = Objects.requireNonNull(cacheDir, "cacheDir must not be null");
    }

    public static ValidationConfigSeederParameters of(Path validationConfig, Path cacheDir) {
        return new ValidationConfigSeederParameters(validationConfig, cacheDir);
    }

    public Path getValidationConfig() {
        return validationConfig;
    }

    public Path getCacheDir() {
        return cacheDir;
    }
}
