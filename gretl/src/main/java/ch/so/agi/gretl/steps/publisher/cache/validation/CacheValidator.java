package ch.so.agi.gretl.steps.publisher.cache.validation;

import ch.ehi.basics.settings.Settings;
import org.interlis2.validator.Validator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 *  Walks the cache directory tree and validates each transfer file that it finds.
 *  The optional validation configuration is expected at the cache root as validation.ini.
 *  Each transfer file writes its validation log into the same folder as the transfer file itself.
 */
public class CacheValidator {
    public static final String VALIDATION_CONFIG_FILENAME = "validation.ini";
    public static final String VALIDATION_LOG_EXTENSION = "log";

    private final Path cachePath;
    private final boolean throwOnValidationError;
    private final boolean overwriteExistingLog;

    public CacheValidator(Path cache, boolean throwOnValidationError, boolean overwriteExistingLog) {
        this.cachePath = Objects.requireNonNull(cache, "cache must not be null");
        this.throwOnValidationError = throwOnValidationError;
        this.overwriteExistingLog = overwriteExistingLog;
    }

    public void validate() {
        if (!Files.exists(cachePath)) {
            throw new IllegalArgumentException("cache path does not exist: " + cachePath);
        }
        if (!Files.isDirectory(cachePath)) {
            throw new IllegalArgumentException("cache path must be a directory: " + cachePath);
        }

        final Path validationConfig = cachePath.resolve(VALIDATION_CONFIG_FILENAME);
        if (Files.exists(validationConfig) && !Files.isRegularFile(validationConfig)) {
            throw new IllegalArgumentException("validation config must be a file: " + validationConfig);
        }

        List<Path> transferFiles = new ArrayList<>();
        try {
            Files.walkFileTree(cachePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile() && isTransferFile(file)) {
                        transferFiles.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("failed to walk cache tree " + cachePath, e);
        }

        transferFiles.sort(Comparator.naturalOrder());
        for (Path transferFile : transferFiles) {
            validateTransferFile(transferFile, validationConfig);
        }
    }

    private void validateTransferFile(Path transferFile, Path validationConfig) {
        Path logFile = logFileFor(transferFile);
        if (Files.exists(logFile) && !overwriteExistingLog) {
            throw new IllegalStateException("validation log already exists: " + logFile);
        }

        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_DISABLE_STD_LOGGER, Validator.TRUE);
        settings.setValue(Validator.SETTING_LOGFILE, logFile.toString());
        if (Files.exists(validationConfig)) {
            try {
                Settings validationConfigSettings = new Settings();
                validationConfigSettings.load(validationConfig.toFile());
                copySettings(validationConfigSettings, settings);
            } catch (IOException e) {
                throw new IllegalStateException("failed to read validation config " + validationConfig, e);
            }
        }

        boolean validationOk = new Validator().validate(new String[] {transferFile.toAbsolutePath().toString()}, settings);
        if (!validationOk && throwOnValidationError) {
            throw new IllegalStateException("validation failed for " + transferFile);
        }
    }

    private void copySettings(Settings source, Settings target) {
        for (String key : source.getValues()) {
            String translatedKey = translateKey(key);
            if (translatedKey != null) {
                target.setValue(translatedKey, source.getValue(key));
            }
        }
        for (String key : source.getTransientValues()) {
            target.setTransientObject(key, source.getTransientObject(key));
        }
    }

    private String translateKey(String key) {
        if ("ilidirs".equals(key) || "modeldir".equals(key)) {
            return Validator.SETTING_ILIDIRS;
        }
        if ("models".equals(key) || "modelNames".equals(key) || "modelnames".equals(key)) {
            return Validator.SETTING_MODELNAMES;
        }
        if ("config".equals(key) || "configFile".equals(key)) {
            return Validator.SETTING_CONFIGFILE;
        }
        return key;
    }

    private boolean isTransferFile(Path file) {
        String filename = file.getFileName().toString().toLowerCase();
        return filename.endsWith(".xtf") || filename.endsWith(".itf");
    }

    private Path logFileFor(Path transferFile) {
        String filename = transferFile.getFileName().toString();
        int extensionIndex = filename.lastIndexOf('.');
        String logFilename = (extensionIndex > 0 ? filename.substring(0, extensionIndex) : filename)
                + "." + VALIDATION_LOG_EXTENSION;
        return transferFile.getParent() == null ? Path.of(logFilename) : transferFile.getParent().resolve(logFilename);
    }
}
