package ch.so.agi.gretl.steps.publisher.cache.pack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Responsibility: create the staged publication zip archives from one source
 * file or folder plus the validation artifacts stored next to it.
 */
public final class Zipper {
    private static final String VALIDATION_LOG = "validation.log";
    private static final String VALIDATION_INI = "validation.ini";

    private final Path cacheDir;
    private final String dataIdent;

    public Zipper(Path cacheDir, String dataIdent) {
        this.cacheDir = Objects.requireNonNull(cacheDir, "cacheDir");
        this.dataIdent = requireText(dataIdent, "dataIdent");
    }

    public Path zip(Path source, Path targetZip) throws IOException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(targetZip, "targetZip");

        Files.createDirectories(targetZip.getParent());
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(targetZip))) {
            if (Files.isDirectory(source)) {
                copyFilesFromFolderToZip(out, source);
            } else {
                copyFileToZip(out, source.getFileName().toString(), source);
            }

            Path validationLog = findSibling(source, VALIDATION_LOG);
            if (validationLog != null) {
                copyFileToZip(out, VALIDATION_LOG, validationLog);
            }

            Path validationIni = findSibling(source, VALIDATION_INI);
            if (validationIni != null) {
                copyFileToZip(out, VALIDATION_INI, validationIni);
            }
        }
        return targetZip;
    }

    public Path getCacheDir() {
        return cacheDir;
    }

    public String getDataIdent() {
        return dataIdent;
    }

    private Path findSibling(Path source, String filename) {
        Path parent = source.getParent();
        if (parent == null) {
            parent = cacheDir;
        }
        Path candidate = parent.resolve(filename);
        if (Files.exists(candidate)) {
            return candidate;
        }
        return null;
    }

    private void copyFilesFromFolderToZip(ZipOutputStream out, Path sourceFolder) throws IOException {
        Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isRegularFile(file)) {
                    copyFileToZip(out, file.getFileName().toString(), file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void copyFileToZip(ZipOutputStream out, String filename, Path sourcePath) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(sourcePath))) {
            out.putNextEntry(new ZipEntry(filename));
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
        }
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
