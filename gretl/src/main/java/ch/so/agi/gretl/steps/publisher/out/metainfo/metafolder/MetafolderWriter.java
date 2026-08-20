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
import java.util.Iterator;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.PredefinedModel;
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.stage.validation.CacheValidator;
import org.interlis2.validator.Validator;

/** Recreates the legacy publication {@code meta/} folder from staged transfer files. */
public final class MetafolderWriter implements Operation<MetafolderWriterParameters> {
    static final String META_DIR = "meta";
    static final String METAINFO_FILE = "metainfo.json";

    @Override
    public void execute(MetafolderWriterParameters parameters) throws Exception {
        List<Path> modelFiles = resolveModelFiles(parameters);
        Path metaDir = parameters.getCacheRoot().resolve(META_DIR);
        prepareMetaDirectory(metaDir);
        for (Path modelFile : modelFiles) {
            Files.copy(modelFile, metaDir.resolve(modelFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        if (parameters.shouldWriteJson()) {
            new JsonSectionWriter().execute(JsonSectionWriterParameters.of(parameters.getJsonmetaAddress(),
                    parameters.getJsonmetaBucket(), parameters.getJsonmetaFileName(), parameters.getIdent(),
                    metaDir.resolve(METAINFO_FILE)));
        }
    }

    private List<Path> resolveModelFiles(MetafolderWriterParameters parameters) throws IOException {
        List<Path> transferFiles = discoverTransferFiles(parameters.getCacheRoot());
        Set<Path> modelFiles = new LinkedHashSet<>();
        for (Path transferFile : transferFiles) {
            Validator validator = new Validator();
            if (!validator.validate(new String[] { transferFile.toAbsolutePath().toString() }, settings(parameters))) {
                throw new IllegalStateException("validation failed while resolving models for " + transferFile);
            }
            for (Iterator<Model> iterator = validator.getModel().iterator(); iterator.hasNext();) {
                Model model = iterator.next();
                if (!(model instanceof PredefinedModel) && model.getFileName() != null) {
                    modelFiles.add(Path.of(model.getFileName()).toAbsolutePath().normalize());
                }
            }
        }
        return new ArrayList<>(modelFiles);
    }

    private Settings settings(MetafolderWriterParameters parameters) throws IOException {
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_DISABLE_STD_LOGGER, Validator.TRUE);
        Path validationConfig = parameters.getValidationConfig();
        if (validationConfig != null) {
            if (!Files.isRegularFile(validationConfig)) {
                throw new IllegalArgumentException("validation config must be a file: " + validationConfig);
            }
            settings.setValue(Validator.SETTING_CONFIGFILE, validationConfig.toString());
        }
        if (parameters.getCustomModelDir() != null) {
            settings.setValue(Validator.SETTING_ILIDIRS, parameters.getCustomModelDir());
        }
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
