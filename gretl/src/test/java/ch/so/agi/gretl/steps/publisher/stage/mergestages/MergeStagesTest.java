package ch.so.agi.gretl.steps.publisher.stage.mergestages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MergeStagesTest {
    @TempDir
    Path tempDir;

    @Test
    void mergesMultipleTreesIntoOneOutputTree() throws IOException {
        Path sourceOne = createSourceTree("source-one",
                file("alpha.txt", "alpha"),
                file("nested/a.txt", "nested-a"));
        Path sourceTwo = createSourceTree("source-two",
                file("beta.txt", "beta"),
                file("deep/inside/b.txt", "deep-b"));
        Path outputDir = tempDir.resolve("output");

        new MergeStages().execute(MergeStagesParameters.of(List.of(sourceOne, sourceTwo), outputDir));

        assertTrue(Files.isRegularFile(outputDir.resolve("alpha.txt")));
        assertTrue(Files.isRegularFile(outputDir.resolve("nested").resolve("a.txt")));
        assertTrue(Files.isRegularFile(outputDir.resolve("beta.txt")));
        assertTrue(Files.isRegularFile(outputDir.resolve("deep").resolve("inside").resolve("b.txt")));
        assertEquals("alpha", Files.readString(outputDir.resolve("alpha.txt")));
        assertEquals("deep-b", Files.readString(outputDir.resolve("deep").resolve("inside").resolve("b.txt")));
    }

    @Test
    void keepsExistingTargetDirectories() throws IOException {
        Path sourceOne = createSourceTree("source-one", file("nested/a.txt", "one-a"));
        Path sourceTwo = createSourceTree("source-two", file("nested/b.txt", "two-b"));
        Path outputDir = Files.createDirectories(tempDir.resolve("output").resolve("nested"));

        new MergeStages().execute(MergeStagesParameters.of(List.of(sourceOne, sourceTwo), outputDir.getParent()));

        assertTrue(Files.isDirectory(outputDir));
        assertTrue(Files.isRegularFile(outputDir.resolve("a.txt")));
        assertTrue(Files.isRegularFile(outputDir.resolve("b.txt")));
    }

    @Test
    void rejectsTargetFileCollisionsFromSameRelativePath() throws IOException {
        Path sourceOne = createSourceTree("source-one", file("shared.txt", "alpha"));
        Path sourceTwo = createSourceTree("source-two", file("shared.txt", "beta"));
        Path outputDir = tempDir.resolve("output");

        assertThrows(FileAlreadyExistsException.class,
                () -> new MergeStages().execute(MergeStagesParameters.of(List.of(sourceOne, sourceTwo), outputDir)));
        assertTrue(Files.isRegularFile(outputDir.resolve("shared.txt")));
        assertEquals("alpha", Files.readString(outputDir.resolve("shared.txt")));
    }

    @Test
    void rejectsExistingTargetFileBeforeOverwrite() throws IOException {
        Path sourceOne = createSourceTree("source-one", file("nested/a.txt", "one-a"));
        Path outputDir = Files.createDirectories(tempDir.resolve("output"));
        Files.createDirectories(outputDir.resolve("nested"));
        Files.writeString(outputDir.resolve("nested").resolve("a.txt"), "stale", StandardCharsets.UTF_8);

        assertThrows(FileAlreadyExistsException.class,
                () -> new MergeStages().execute(MergeStagesParameters.of(List.of(sourceOne), outputDir)));
        assertEquals("stale", Files.readString(outputDir.resolve("nested").resolve("a.txt")));
    }

    @Test
    void rejectsMissingInputDirectory() {
        Path outputDir = tempDir.resolve("output");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MergeStages().execute(MergeStagesParameters.of(List.of(tempDir.resolve("missing")), outputDir)));

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    void rejectsInputFileInsteadOfDirectory() throws IOException {
        Path inputFile = Files.writeString(tempDir.resolve("input.txt"), "content", StandardCharsets.UTF_8);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MergeStages().execute(MergeStagesParameters.of(List.of(inputFile), tempDir.resolve("output"))));

        assertTrue(exception.getMessage().contains("must be a directory"));
    }

    private Path createSourceTree(String sourceName, FixtureFile... files) throws IOException {
        Path sourceRoot = Files.createDirectories(tempDir.resolve(sourceName));
        for (FixtureFile fixtureFile : files) {
            Path file = sourceRoot.resolve(fixtureFile.relativePath);
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, fixtureFile.content, StandardCharsets.UTF_8);
        }
        return sourceRoot;
    }

    private static FixtureFile file(String relativePath, String content) {
        return new FixtureFile(relativePath, content);
    }

    private static final class FixtureFile {
        private final String relativePath;
        private final String content;

        private FixtureFile(String relativePath, String content) {
            this.relativePath = relativePath;
            this.content = content;
        }
    }
}
