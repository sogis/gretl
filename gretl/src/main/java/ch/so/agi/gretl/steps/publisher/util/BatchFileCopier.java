package ch.so.agi.gretl.steps.publisher.util;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Reusable batch file copy helper for publisher subpackages.
 *
 * <p>Creates the destination directory tree and copies multiple already-resolved
 * source files into one target directory while preserving the original file
 * names.</p>
 */
public final class BatchFileCopier {
    public List<Path> copyFiles(List<Path> sourceFiles, Path targetDirectory) throws IOException {
        Objects.requireNonNull(sourceFiles, "sourceFiles must not be null");
        Objects.requireNonNull(targetDirectory, "targetDirectory must not be null");
        if (sourceFiles.isEmpty()) {
            throw new IllegalArgumentException("sourceFiles must not be empty");
        }

        Files.createDirectories(targetDirectory);

        List<Path> targetFiles = new ArrayList<Path>(sourceFiles.size());
        Set<Path> seenTargets = new LinkedHashSet<Path>();
        for (Path sourceFile : sourceFiles) {
            Path validatedSource = validateSourceFile(sourceFile);
            Path targetFile = targetDirectory.resolve(validatedSource.getFileName().toString());
            if (!seenTargets.add(targetFile)) {
                throw new IllegalArgumentException("duplicate target file <" + targetFile + ">");
            }
            assertTargetAvailable(targetFile);
            targetFiles.add(targetFile);
        }

        for (int index = 0; index < sourceFiles.size(); index++) {
            Path sourceFile = validateSourceFile(sourceFiles.get(index));
            Path targetFile = targetFiles.get(index);
            Files.copy(sourceFile, targetFile);
        }

        return targetFiles;
    }

    public Path copyFile(Path sourceFile, Path targetDirectory) throws IOException {
        List<Path> targetFiles = copyFiles(List.of(sourceFile), targetDirectory);
        return targetFiles.get(0);
    }

    private static Path validateSourceFile(Path sourceFile) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFiles must not contain null values");
        if (!Files.exists(sourceFile)) {
            throw new java.nio.file.NoSuchFileException(sourceFile.toString());
        }
        if (!Files.isRegularFile(sourceFile)) {
            throw new IllegalArgumentException("source file <" + sourceFile + "> must be a regular file");
        }
        return sourceFile;
    }

    private static void assertTargetAvailable(Path targetFile) throws IOException {
        if (Files.exists(targetFile)) {
            throw new FileAlreadyExistsException(targetFile.toString());
        }
    }
}
