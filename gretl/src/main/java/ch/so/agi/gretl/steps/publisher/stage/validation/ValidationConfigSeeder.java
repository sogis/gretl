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
public final class ValidationConfigSeeder implements Operation<ValidationConfigSeederParameters> {
    @Override
    public void execute(ValidationConfigSeederParameters operationParameters) throws IOException {
        seed(operationParameters);
    }

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
