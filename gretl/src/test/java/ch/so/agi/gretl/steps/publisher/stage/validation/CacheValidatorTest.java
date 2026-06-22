package ch.so.agi.gretl.steps.publisher.stage.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CacheValidatorTest {

    private static final Path TEST_DATA_ROOT = Path.of("src", "test", "resources", "data", "publisher");
    private static final Path TEST_ILI_DIR = TEST_DATA_ROOT.resolve("ili").toAbsolutePath().normalize();
    private static final Path TEST_FILES_DIR = TEST_DATA_ROOT.resolve("files").toAbsolutePath().normalize();

    @TempDir
    Path tempDir;

    @Test
    public void validateRootOnlyTree() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        Files.createDirectories(cacheRoot);
        copyFixture("SimpleCoord23a.xtf", cacheRoot.resolve("SimpleCoord23a.xtf"));
        writeValidationConfig(cacheRoot, "SimpleCoord23");

        CacheValidator validator = new CacheValidator(cacheRoot, true, true);

        assertDoesNotThrow(validator::validate);
        assertTrue(Files.exists(cacheRoot.resolve("SimpleCoord23a.log")));
        assertTrue(Files.size(cacheRoot.resolve("SimpleCoord23a.log")) > 0);
    }

    @Test
    public void validateNestedTreeWritesLogNextToFile() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        Path nested = cacheRoot.resolve("nested").resolve("deeper");
        Files.createDirectories(nested);
        copyFixture("SimpleCoord23a.xtf", cacheRoot.resolve("SimpleCoord23a.xtf"));
        copyFixture("SimpleCoord23b.xtf", nested.resolve("SimpleCoord23b.xtf"));
        writeValidationConfig(cacheRoot, "SimpleCoord23");

        CacheValidator validator = new CacheValidator(cacheRoot, true, true);

        assertDoesNotThrow(validator::validate);
        assertTrue(Files.exists(cacheRoot.resolve("SimpleCoord23a.log")));
        assertTrue(Files.exists(nested.resolve("SimpleCoord23b.log")));
        assertTrue(Files.size(cacheRoot.resolve("SimpleCoord23a.log")) > 0);
        assertTrue(Files.size(nested.resolve("SimpleCoord23b.log")) > 0);
    }

    @Test
    public void validateFailsWhenLogExistsAndOverwriteIsDisabled() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        Files.createDirectories(cacheRoot);
        Path transferFile = copyFixture("SimpleCoord23a.xtf", cacheRoot.resolve("SimpleCoord23a.xtf"));
        writeValidationConfig(cacheRoot, "SimpleCoord23");
        Path logFile = logFileFor(transferFile);
        Files.writeString(logFile, "sentinel", StandardCharsets.UTF_8);

        CacheValidator validator = new CacheValidator(cacheRoot, true, false);

        assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(Files.exists(logFile));
        assertEquals("sentinel", Files.readString(logFile, StandardCharsets.UTF_8));
    }

    @Test
    public void validateOverwritesExistingLogWhenEnabled() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        Files.createDirectories(cacheRoot);
        Path transferFile = copyFixture("SimpleCoord23a.xtf", cacheRoot.resolve("SimpleCoord23a.xtf"));
        writeValidationConfig(cacheRoot, "SimpleCoord23");
        Path logFile = logFileFor(transferFile);
        Files.writeString(logFile, "sentinel", StandardCharsets.UTF_8);

        CacheValidator validator = new CacheValidator(cacheRoot, true, true);

        assertDoesNotThrow(validator::validate);
        assertTrue(Files.exists(logFile));
        assertFalse(Files.readString(logFile, StandardCharsets.UTF_8).equals("sentinel"));
    }

    @Test
    public void validateFailureCanBeSuppressedOrRaised() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        Files.createDirectories(cacheRoot);
        Path invalidTransfer = cacheRoot.resolve("SimpleCoord23a.xtf");
        String invalidContent = Files.readString(TEST_FILES_DIR.resolve("SimpleCoord23a.xtf"), StandardCharsets.UTF_8)
                .replace("2460001.000", "9999999.000");
        Files.writeString(invalidTransfer, invalidContent, StandardCharsets.UTF_8);
        writeValidationConfig(cacheRoot, "SimpleCoord23");

        CacheValidator suppressingValidator = new CacheValidator(cacheRoot, false, true);
        CacheValidator failingValidator = new CacheValidator(cacheRoot, true, true);

        assertDoesNotThrow(suppressingValidator::validate);
        assertTrue(Files.exists(cacheRoot.resolve("SimpleCoord23a.log")));
        assertTrue(Files.size(cacheRoot.resolve("SimpleCoord23a.log")) > 0);

        assertThrows(IllegalStateException.class, failingValidator::validate);
    }

    private Path copyFixture(String fixtureName, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        return Files.copy(TEST_FILES_DIR.resolve(fixtureName), target);
    }

    private void writeValidationConfig(Path cacheRoot, String models) throws IOException {
        String content = "[PARAMETER]\n"
                + "ilidirs=" + TEST_ILI_DIR + "\n"
                + "models=" + models + "\n";
        Files.writeString(cacheRoot.resolve(CacheValidator.VALIDATION_CONFIG_FILENAME), content, StandardCharsets.UTF_8);
    }

    private Path logFileFor(Path transferFile) {
        String filename = transferFile.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        String logFilename = (dotIndex > 0 ? filename.substring(0, dotIndex) : filename)
                + "." + CacheValidator.VALIDATION_LOG_EXTENSION;
        return transferFile.getParent().resolve(logFilename);
    }
}
