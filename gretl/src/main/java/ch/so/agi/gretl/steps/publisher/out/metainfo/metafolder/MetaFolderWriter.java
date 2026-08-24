package ch.so.agi.gretl.steps.publisher.out.metainfo.metafolder;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.PredefinedModel;
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.stage.validation.CacheValidator;
import org.interlis2.validator.Validator;

/** Recreates the legacy publication {@code meta/} folder from staged transfer files. */
public final class MetaFolderWriter implements Operation {
    static final String META_DIR = "meta";
    static final String METAINFO_FILE = "metainfo.json";
    private MetaFolderWriterParameters parameters;
    private int modelFileCount;

    public MetaFolderWriter(MetaFolderWriterParameters parameters) {
        this.parameters = java.util.Objects.requireNonNull(parameters, "parameters must not be null");
    }
    @Deprecated public MetaFolderWriter() { }
    @Deprecated public void execute(MetaFolderWriterParameters parameters) throws Exception { this.parameters = parameters; execute(); }

    @Override
    public void execute() throws Exception {
        List<Path> modelFiles = resolveModelFiles(parameters);
        modelFileCount = modelFiles.size();
        Path metaDir = parameters.getPublicationRoot().resolve(META_DIR);
        prepareMetaDirectory(metaDir);
        for (Path modelFile : modelFiles) {
            Files.copy(modelFile, metaDir.resolve(modelFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        if (parameters.shouldWriteJson()) {
            new JsonSectionWriter(JsonSectionWriterParameters.of(parameters.getJsonmetaAddress(),
                    parameters.getJsonmetaBucket(), parameters.getJsonmetaFileName(), parameters.getIdent(),
                    metaDir.resolve(METAINFO_FILE))).execute();
        }
    }

    @Override
    public String getHumanReadableName() { return "Metadata folder writer (meta/)"; }

    @Override
    public String getSuccessLogDetail() {
        return "built metadata folder with " + modelFileCount + " model file(s)"
                + (parameters.shouldWriteJson() ? " and JSON metadata" : "");
    }

    private List<Path> resolveModelFiles(MetaFolderWriterParameters parameters) throws IOException {
        Set<Path> modelFiles = new LinkedHashSet<>();
        for (Path transferFile : discoverTransferFiles(parameters.getRawRoot())) {
            Path transferDir = transferFile.getParent();
            if (transferDir == null || !"xtf".equals(transferDir.getFileName().toString())) {
                modelFiles.addAll(resolveLegacyModelFiles(transferFile, parameters));
                continue;
            }
            Path manifest = transferDir.getParent().resolve("ilimeta").resolve(CacheValidator.MODEL_MANIFEST_FILENAME);
            if (!Files.isRegularFile(manifest)) {
                throw new IllegalStateException("missing validation model manifest " + manifest);
            }
            for (String value : Files.readAllLines(manifest, StandardCharsets.UTF_8)) {
                if (!value.trim().isEmpty()) modelFiles.add(Path.of(value).toAbsolutePath().normalize());
            }
        }
        return new ArrayList<>(modelFiles);
    }

    private Set<Path> resolveLegacyModelFiles(Path transferFile, MetaFolderWriterParameters parameters) throws IOException {
        Validator validator = new Validator();
        if (!validator.validate(new String[] { transferFile.toAbsolutePath().toString() }, settings(parameters))) {
            throw new IllegalStateException("validation failed while resolving models for " + transferFile);
        }
        Set<Path> models = new LinkedHashSet<>();
        for (Iterator<Model> iterator = validator.getModel().iterator(); iterator.hasNext();) {
            Model model = iterator.next();
            if (!(model instanceof PredefinedModel) && model.getFileName() != null) models.add(Path.of(model.getFileName()).toAbsolutePath().normalize());
        }
        return models;
    }

    private Settings settings(MetaFolderWriterParameters parameters) throws IOException {
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_DISABLE_STD_LOGGER, Validator.TRUE);
        Path validationConfig = parameters.getValidationConfig();
        if (validationConfig != null) settings.setValue(Validator.SETTING_CONFIGFILE, validationConfig.toString());
        if (parameters.getCustomModelDir() != null) settings.setValue(Validator.SETTING_ILIDIRS, parameters.getCustomModelDir());
        return settings;
    }

    private List<Path> discoverTransferFiles(Path cacheRoot) throws IOException {
        if (!Files.isDirectory(cacheRoot)) {
            throw new IllegalArgumentException("cache root must be a directory: " + cacheRoot);
        }
        List<Path> transferFiles = new ArrayList<>();
        Files.walkFileTree(cacheRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                String name = file.getFileName().toString().toLowerCase();
                if (attributes.isRegularFile() && (name.endsWith(".xtf") || name.endsWith(".itf"))) {
                    transferFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        transferFiles.sort(Comparator.naturalOrder());
        return transferFiles;
    }

    private void prepareMetaDirectory(Path metaDir) throws IOException {
        if (Files.exists(metaDir)) {
            Files.walkFileTree(metaDir, new SimpleFileVisitor<Path>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file); return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
                    Files.delete(dir); return FileVisitResult.CONTINUE;
                }
            });
        }
        Files.createDirectories(metaDir);
    }
}
