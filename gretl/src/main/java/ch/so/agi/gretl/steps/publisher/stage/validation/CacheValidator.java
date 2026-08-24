package ch.so.agi.gretl.steps.publisher.stage.validation;

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
import java.util.stream.Collectors;

import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.PredefinedModel;

import ch.so.agi.gretl.steps.publisher.operation.Operation;

/**
 *  Walks the cache directory tree and validates each transfer file that it finds.
 *  The optional validation configuration is expected at the cache root as validation.ini.
 *  Each transfer file writes its validation log into the same folder as the transfer file itself.
 */
public class CacheValidator implements Operation {
    public static final String VALIDATION_CONFIG_FILENAME = "validation.ini";
    public static final String VALIDATION_LOG_EXTENSION = "log";
    public static final String MODEL_MANIFEST_FILENAME = "models.list";

    private CacheValidatorParameters parameters;
    private int validatedFileCount;

    public CacheValidator(CacheValidatorParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters must not be null");
    }
    @Deprecated public CacheValidator() { }
    @Deprecated public void execute(CacheValidatorParameters parameters) { this.parameters = parameters; execute(); }

    @Override
    public void execute() {
        validatedFileCount = validate(parameters);
    }

    @Override
    public String getHumanReadableName() { return "Validation"; }

    @Override
    public String getSuccessLogDetail() { return "validated " + validatedFileCount + " transfer file(s)"; }

    int validate(CacheValidatorParameters operationParameters) {
        Objects.requireNonNull(operationParameters, "operationParameters must not be null");
        Path cachePath = operationParameters.getCachePath();
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
            validateTransferFile(operationParameters, transferFile, validationConfig);
        }
        return transferFiles.size();
    }

    private void validateTransferFile(CacheValidatorParameters operationParameters, Path transferFile,
            Path validationConfig) {
        Path logFile = logFileFor(transferFile);
        if (Files.exists(logFile) && !operationParameters.isOverwriteExistingLog()) {
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
        if (operationParameters.getCustomModelDir() != null) {
            settings.setValue(Validator.SETTING_ILIDIRS, operationParameters.getCustomModelDir());
        }

        Validator validator = new Validator();
        boolean validationOk = validator.validate(new String[] {transferFile.toAbsolutePath().toString()}, settings);
        if (!validationOk && operationParameters.isThrowOnValidationError()) {
            throw new IllegalStateException("validation failed for " + transferFile);
        }
        if (validationOk) writeModelManifest(validator, transferFile);
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
        Path transferDir = transferFile.getParent();
        if (transferDir == null || !"xtf".equals(transferDir.getFileName().toString())) {
            return transferDir == null ? Path.of(logFilename) : transferDir.resolve(logFilename);
        }
        Path partDir = transferDir.getParent();
        if (partDir == null) return transferDir.resolve(logFilename);
        Path iliMetaDir = partDir.resolve("ilimeta");
        try {
            Files.createDirectories(iliMetaDir);
        } catch (IOException e) {
            throw new IllegalStateException("failed to create validation metadata directory " + iliMetaDir, e);
        }
        return iliMetaDir.resolve(logFilename);
    }

    private void writeModelManifest(Validator validator, Path transferFile) {
        Path transferDir = transferFile.getParent();
        if (transferDir == null || !"xtf".equals(transferDir.getFileName().toString()) || transferDir.getParent() == null) return;
        Path manifest = transferDir.getParent().resolve("ilimeta").resolve(MODEL_MANIFEST_FILENAME);
        List<String> models = new ArrayList<String>();
        for (java.util.Iterator<Model> iterator = validator.getModel().iterator(); iterator.hasNext();) {
            Model model = iterator.next();
            if (!(model instanceof PredefinedModel) && model.getFileName() != null) models.add(model.getFileName());
        }
        try {
            Files.write(manifest, models.stream().distinct().sorted().collect(Collectors.toList()));
        } catch (IOException e) {
            throw new IllegalStateException("failed to write model manifest " + manifest, e);
        }
    }
}
