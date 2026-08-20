package ch.so.agi.gretl.steps.publisher.stage.pack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import ch.so.agi.gretl.steps.publisher.operation.Operation;

/**
 * Responsibility: orchestrate packaging of one cache publication part by
 * discovering archive sources once and then delegating to the renamer and the
 * zipper in sequence.
 */
public final class Packer implements Operation<PackerParameters> {
    private static final String STAGING_DIR = ".pack";
    private static final String META_DIR = "meta";
    private static final String VALIDATION_LOG = "validation.log";
    private static final String VALIDATION_INI = "validation.ini";

    @Override
    public void execute(PackerParameters operationParameters) {
        pack(operationParameters);
    }

    void pack(PackerParameters operationParameters) {
        Objects.requireNonNull(operationParameters, "operationParameters must not be null");
        Path cacheDir = operationParameters.getCacheDir();
        String dataIdent = operationParameters.getDataIdent();
        Renamer renamer = new Renamer(cacheDir, dataIdent);
        Zipper zipper = new Zipper(cacheDir, dataIdent);

        try {
            Files.createDirectories(cacheDir);
            Path stagingDir = cacheDir.resolve(STAGING_DIR).resolve(dataIdent);
            Files.createDirectories(stagingDir);

            List<Path> sources = discoverSources(cacheDir, operationParameters.getOutputFormats());
            assertRequestedTransferFormatExists(cacheDir, sources, operationParameters.getOutputFormats());
            for (Path source : sources) {
                Path stagingZip = zipper.zip(source, stagingDir.resolve(source.getFileName().toString() + ".zip"));
                renamer.moveArchive(stagingZip, source);
            }

            deleteTreeIfExists(cacheDir.resolve(STAGING_DIR));
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to pack cache directory " + cacheDir, ex);
        }
    }

    private List<Path> discoverSources(Path cacheDir, List<OutputFormat> outputFormats) throws IOException {
        List<Path> sources = new ArrayList<>();
        try (Stream<Path> stream = Files.list(cacheDir)) {
            stream.filter(path -> isPackableSource(path, outputFormats))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(sources::add);
        }
        return sources;
    }

    private boolean isPackableSource(Path path, List<OutputFormat> outputFormats) {
        if (!Files.isRegularFile(path) && !Files.isDirectory(path)) {
            return false;
        }

        String fileName = path.getFileName().toString();
        if (fileName.startsWith(".")) {
            return false;
        }
        if (META_DIR.equals(fileName)) {
            return false;
        }
        if (VALIDATION_LOG.equals(fileName) || VALIDATION_INI.equals(fileName)) {
            return false;
        }
        if (fileName.endsWith(".zip")) {
            return false;
        }
        return outputFormats.stream().anyMatch(format -> matches(path, format));
    }

    private boolean matches(Path path, OutputFormat format) {
        String name = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        if (format.isTransferFormat()) {
            return Files.isRegularFile(path) && name.endsWith("." + format.getIdentifier());
        }
        return Files.isDirectory(path) && format.getDerivedFormat().getDirectoryName().equals(name);
    }

    private void assertRequestedTransferFormatExists(Path cacheDir, List<Path> sources,
            List<OutputFormat> outputFormats) {
        for (OutputFormat format : outputFormats) {
            if (format.isTransferFormat() && sources.stream().noneMatch(path -> matches(path, format))) {
                throw new IllegalArgumentException("Requested transfer format " + format.getIdentifier()
                        + " is not available in " + cacheDir);
            }
        }
    }

    private static void deleteTreeIfExists(Path path) throws IOException {
        if (Files.notExists(path)) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public java.nio.file.FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @Override
            public java.nio.file.FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }

}
