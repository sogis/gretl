package ch.so.agi.gretl.steps.publisher.stage.derivedformats;

import ch.ehi.basics.settings.Settings;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.steps.Gpkg2DxfStep;
import ch.so.agi.gretl.steps.Gpkg2ShpStep;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class SingleTransferDerivator {
    private final Path transferFile;
    private final EnumSet<DerivedFormat> requestedFormats;

    public SingleTransferDerivator(Path transferFile, List<DerivedFormat> requestedFormats) {
        this.transferFile = Objects.requireNonNull(transferFile, "transferFile must not be null").toAbsolutePath().normalize();
        Objects.requireNonNull(requestedFormats, "requestedFormats must not be null");
        this.requestedFormats = EnumSet.noneOf(DerivedFormat.class);
        for (DerivedFormat requestedFormat : requestedFormats) {
            this.requestedFormats.add(Objects.requireNonNull(requestedFormat, "requestedFormats must not contain null"));
        }
        if (this.requestedFormats.isEmpty()) {
            throw new IllegalArgumentException("requestedFormats must not be empty");
        }
    }

    public void derive() {
        Path transferRoot = transferFile.getParent();
        if (transferRoot == null) {
            throw new IllegalArgumentException("transferFile must have a parent directory");
        }

        Path intermediatesDir = transferRoot.resolve("derivation_intermediates");
        try {
            prepareCleanDirectory(intermediatesDir);
            deriveRequestedFormats(transferRoot, intermediatesDir);
        } finally {
            deleteRecursively(intermediatesDir);
        }
    }

    private void deriveRequestedFormats(Path transferRoot, Path intermediatesDir) {
        try {
            String baseName = stripExtension(transferFile.getFileName().toString());

            boolean gpkgRequested = requestedFormats.contains(DerivedFormat.GPKG);
            boolean needsGpkg = gpkgRequested
                    || requestedFormats.contains(DerivedFormat.SHP)
                    || requestedFormats.contains(DerivedFormat.DXF);

            Path gpkgFile = null;
            if (needsGpkg) {
                Path gpkgRoot = gpkgRequested ? transferRoot.resolve(DerivedFormat.GPKG.getDirectoryName())
                        : intermediatesDir.resolve(DerivedFormat.GPKG.getDirectoryName());
                gpkgFile = gpkgRoot.resolve(baseName + ".gpkg");
            }

            if (gpkgRequested) {
                prepareCleanDirectory(gpkgFile.getParent());
            }
            if (requestedFormats.contains(DerivedFormat.SHP)) {
                prepareCleanDirectory(transferRoot.resolve(DerivedFormat.SHP.getDirectoryName()));
            }
            if (requestedFormats.contains(DerivedFormat.DXF)) {
                prepareCleanDirectory(transferRoot.resolve(DerivedFormat.DXF.getDirectoryName()));
            }
            if (requestedFormats.contains(DerivedFormat.GEOBAU_DXF)) {
                prepareCleanDirectory(transferRoot.resolve(DerivedFormat.GEOBAU_DXF.getDirectoryName()));
            }

            if (gpkgFile != null) {
                deriveGpkg(gpkgFile);
            }
            if (requestedFormats.contains(DerivedFormat.SHP)) {
                deriveShp(gpkgFile, transferRoot.resolve(DerivedFormat.SHP.getDirectoryName()));
            }
            if (requestedFormats.contains(DerivedFormat.DXF)) {
                deriveDxf(gpkgFile, transferRoot.resolve(DerivedFormat.DXF.getDirectoryName()));
            }
            if (requestedFormats.contains(DerivedFormat.GEOBAU_DXF)) {
                deriveGeobauDxf(transferRoot.resolve(DerivedFormat.GEOBAU_DXF.getDirectoryName()));
            }
        } catch (Exception e) {
            throw new IllegalStateException("failed to derive formats for " + transferFile, e);
        }
    }

    private void deriveGpkg(Path gpkgFile) throws Exception {
        Files.createDirectories(gpkgFile.getParent());
        Files.deleteIfExists(gpkgFile);

        Config config = new Config();
        new ch.ehi.ili2gpkg.GpkgMain().initConfig(config);
        config.setXtffile(transferFile.toString());
        config.setDbfile(gpkgFile.toString());
        config.setDburl("jdbc:sqlite:" + config.getDbfile());
        config.setModeldir(buildModeldir());
        config.setFunction(Config.FC_IMPORT);
        config.setDoImplicitSchemaImport(true);
        config.setValidation(false);
        config.setStrokeArcs(config.STROKE_ARCS_ENABLE);
        config.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
        config.setCreateMetaInfo(true);
        config.setItfTransferfile(transferFile.getFileName().toString().toLowerCase().endsWith(".itf"));
        Ili2db.run(config, null);
    }

    private void deriveShp(Path gpkgFile, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        new Gpkg2ShpStep().execute(gpkgFile.toString(), outputDir.toString());
    }

    private void deriveDxf(Path gpkgFile, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        new Gpkg2DxfStep().execute(gpkgFile.toString(), outputDir.toString());
    }

    private void deriveGeobauDxf(Path outputDir) throws Exception {
        Files.createDirectories(outputDir);

        Settings settings = new Settings();
        settings.setValue(org.interlis2.av2geobau.Av2geobau.SETTING_ILIDIRS, buildGeobauModeldir());

        File sourceFile = transferFile.toFile();
        File dxfFile = outputDir.resolve(stripExtension(transferFile.getFileName().toString()) + ".dxf").toFile();
        boolean ok = org.interlis2.av2geobau.Av2geobau.convert(sourceFile, dxfFile, settings);
        if (!ok) {
            throw new IllegalStateException("Geobau DXF conversion failed for " + transferFile);
        }
    }

    private String buildModeldir() {
        Path parent = transferFile.getParent();
        if (parent == null) {
            return ch.interlis.ili2c.gui.UserSettings.DEFAULT_ILIDIRS;
        }
        return parent.toAbsolutePath().normalize() + ";" + ch.interlis.ili2c.gui.UserSettings.DEFAULT_ILIDIRS;
    }

    private String buildGeobauModeldir() {
        Path parent = transferFile.getParent();
        if (parent == null) {
            return org.interlis2.av2geobau.Av2geobau.SETTING_DEFAULT_ILIDIRS;
        }
        return parent.toAbsolutePath().normalize() + ";" + org.interlis2.av2geobau.Av2geobau.SETTING_DEFAULT_ILIDIRS;
    }

    private void prepareCleanDirectory(Path directory) {
        deleteRecursively(directory);
        try {
            Files.createDirectories(directory);
        } catch (Exception e) {
            throw new IllegalStateException("failed to create directory " + directory, e);
        }
    }

    private void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(path)) {
            stream
                    .sorted(Comparator.reverseOrder())
                    .forEach(current -> {
                        try {
                            Files.deleteIfExists(current);
                        } catch (Exception e) {
                            throw new IllegalStateException("failed to delete " + current, e);
                        }
                    });
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }
            throw new IllegalStateException("failed to delete " + path, e);
        }
    }

    private String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return fileName;
        }
        return fileName.substring(0, dot);
    }
}
