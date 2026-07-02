package ch.so.agi.gretl.steps.publisher.stage.mergestages;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.Operation;

/**
 * Merges multiple stage directories into one output directory.
 *
 * <p>The merge is recursive, preserves relative paths, creates missing target
 * directories, and refuses to overwrite existing target files.</p>
 */
public final class MergeStages implements Operation<MergeStagesParameters> {
    @Override
    public void execute(MergeStagesParameters operationParameters) throws IOException {
        merge(operationParameters);
    }

    void merge(MergeStagesParameters operationParameters) throws IOException {
        Objects.requireNonNull(operationParameters, "operationParameters must not be null");
        Path outputDir = operationParameters.getOutputDir();

        validateOutputDir(outputDir);
        Files.createDirectories(outputDir);

        for (Path inputDir : operationParameters.getInputDirs()) {
            mergeOne(inputDir, outputDir);
        }
    }

    private void mergeOne(Path inputDir, Path outputDir) throws IOException {
        Objects.requireNonNull(inputDir, "inputDirs must not contain null values");
        validateInputDir(inputDir);

        Files.walkFileTree(inputDir, new SimpleFileVisitor<Path>() {
            @Override
            public java.nio.file.FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = outputDir.resolve(inputDir.relativize(dir).toString());
                if (Files.exists(targetDir)) {
                    if (!Files.isDirectory(targetDir)) {
                        throw new FileAlreadyExistsException(targetDir.toString());
                    }
                } else {
                    Files.createDirectories(targetDir);
                }
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @Override
            public java.nio.file.FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isRegularFile()) {
                    return java.nio.file.FileVisitResult.CONTINUE;
                }

                Path targetFile = outputDir.resolve(inputDir.relativize(file).toString());
                if (Files.exists(targetFile)) {
                    throw new FileAlreadyExistsException(targetFile.toString());
                }
                Path targetParent = targetFile.getParent();
                if (targetParent != null && Files.notExists(targetParent)) {
                    Files.createDirectories(targetParent);
                }
                Files.copy(file, targetFile, StandardCopyOption.COPY_ATTRIBUTES);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }

    private void validateInputDir(Path inputDir) {
        if (!Files.exists(inputDir)) {
            throw new IllegalArgumentException("inputDir does not exist: " + inputDir);
        }
        if (!Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("inputDir must be a directory: " + inputDir);
        }
    }

    private void validateOutputDir(Path outputDir) {
        if (Files.exists(outputDir) && !Files.isDirectory(outputDir)) {
            throw new IllegalArgumentException("outputDir must be a directory: " + outputDir);
        }
    }
}
