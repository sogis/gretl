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

/**
 * Responsibility: orchestrate packaging of one cache publication part by
 * discovering archive sources once and then delegating to the renamer and the
 * zipper in sequence.
 */
public final class Packer {
    private static final String STAGING_DIR = ".pack";
    private static final String META_DIR = "meta";
    private static final String VALIDATION_LOG = "validation.log";
    private static final String VALIDATION_INI = "validation.ini";

    private final Path cacheDir;
    private final String dataIdent;
    private final Renamer renamer;
    private final Zipper zipper;

    public Packer(Path cacheDir, String dataIdent) {
        this.cacheDir = Objects.requireNonNull(cacheDir, "cacheDir");
        this.dataIdent = requireText(dataIdent, "dataIdent");
        this.renamer = new Renamer(cacheDir, dataIdent);
        this.zipper = new Zipper(cacheDir, dataIdent);
    }

    public void pack() {
        try {
            Files.createDirectories(cacheDir);
            Path stagingDir = cacheDir.resolve(STAGING_DIR).resolve(dataIdent);
            Files.createDirectories(stagingDir);

            for (Path source : discoverSources()) {
                Path stagingZip = zipper.zip(source, stagingDir.resolve(source.getFileName().toString() + ".zip"));
                renamer.moveArchive(stagingZip, source);
            }

            deleteTreeIfExists(cacheDir.resolve(STAGING_DIR));
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to pack cache directory " + cacheDir, ex);
        }
    }

    private List<Path> discoverSources() throws IOException {
        List<Path> sources = new ArrayList<>();
        try (Stream<Path> stream = Files.list(cacheDir)) {
            stream.filter(this::isPackableSource)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(sources::add);
        }
        return sources;
    }

    private boolean isPackableSource(Path path) {
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
        return !fileName.endsWith(".zip");
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

    private static String requireText(String value, String name) {
        if (value == null) {
            throw new NullPointerException(name);
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
