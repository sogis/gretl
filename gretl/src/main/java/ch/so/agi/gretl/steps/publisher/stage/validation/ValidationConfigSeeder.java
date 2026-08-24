package ch.so.agi.gretl.steps.publisher.stage.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.Operation;

/**
 * Copies an external validation config into the cache root where {@link CacheValidator} expects it.
 */
public final class ValidationConfigSeeder implements Operation {
    private ValidationConfigSeederParameters parameters;

    public ValidationConfigSeeder(ValidationConfigSeederParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters must not be null");
    }
    @Deprecated public ValidationConfigSeeder() { }
    @Deprecated public void execute(ValidationConfigSeederParameters parameters) throws IOException { this.parameters = parameters; execute(); }

    @Override
    public void execute() throws IOException {
        seed(parameters);
    }

    @Override
    public String getHumanReadableName() { return "Seeding validation configuration"; }

    @Override
    public String getSuccessLogDetail() { return "applied " + parameters.getValidationConfig(); }

    void seed(ValidationConfigSeederParameters operationParameters) throws IOException {
        Objects.requireNonNull(operationParameters, "operationParameters must not be null");
        Path validationConfig = operationParameters.getValidationConfig();
        Path cacheDir = operationParameters.getCacheDir();

        if (!Files.isRegularFile(validationConfig)) {
            throw new IllegalArgumentException("validationConfig must be an existing file: " + validationConfig);
        }

        Files.createDirectories(cacheDir);
        Files.copy(validationConfig, cacheDir.resolve(CacheValidator.VALIDATION_CONFIG_FILENAME),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
}
