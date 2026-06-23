package ch.so.agi.gretl.steps.publisher.in.copy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import ch.so.agi.gretl.steps.publisher.util.copy.BatchFileCopier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BatchFileCopierTest {
    @TempDir
    Path tempDir;

    @Test
    void copiesOneFileAndCreatesTargetDirectory() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path sourceFile = sourceDir.resolve("alpha.xtf");
        Files.write(sourceFile, "alpha".getBytes(StandardCharsets.UTF_8));
        Path targetDir = tempDir.resolve("nested").resolve("target");

        List<Path> copiedFiles = new BatchFileCopier().copyFiles(List.of(sourceFile), targetDir);

        assertEquals(List.of(targetDir.resolve("alpha.xtf")), copiedFiles);
        assertTrue(Files.exists(targetDir.resolve("alpha.xtf")));
        assertEquals("alpha", Files.readString(targetDir.resolve("alpha.xtf")));
    }

    @Test
    void copiesMultipleFilesPreservingFileNames() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path alpha = sourceDir.resolve("alpha.xtf");
        Path beta = sourceDir.resolve("beta.itf");
        Files.writeString(alpha, "alpha", StandardCharsets.UTF_8);
        Files.writeString(beta, "beta", StandardCharsets.UTF_8);

        List<Path> copiedFiles = new BatchFileCopier().copyFiles(List.of(alpha, beta), tempDir.resolve("target"));

        assertEquals(List.of(tempDir.resolve("target").resolve("alpha.xtf"), tempDir.resolve("target").resolve("beta.itf")),
                copiedFiles);
        assertEquals("alpha", Files.readString(tempDir.resolve("target").resolve("alpha.xtf")));
        assertEquals("beta", Files.readString(tempDir.resolve("target").resolve("beta.itf")));
    }

    @Test
    void rejectsMissingSourceFile() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path missing = sourceDir.resolve("missing.xtf");

        assertThrows(NoSuchFileException.class,
                () -> new BatchFileCopier().copyFiles(List.of(missing), tempDir.resolve("target")));
    }

    @Test
    void rejectsExistingTargetFile() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path sourceFile = sourceDir.resolve("alpha.xtf");
        Files.writeString(sourceFile, "alpha", StandardCharsets.UTF_8);
        Path targetDir = Files.createDirectories(tempDir.resolve("target"));
        Files.writeString(targetDir.resolve("alpha.xtf"), "stale", StandardCharsets.UTF_8);

        assertThrows(FileAlreadyExistsException.class,
                () -> new BatchFileCopier().copyFiles(List.of(sourceFile), targetDir));
    }

    @Test
    void rejectsDuplicateTargetFileNamesInBatch() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path first = sourceDir.resolve("alpha.xtf");
        Path secondDir = Files.createDirectory(sourceDir.resolve("other"));
        Path second = secondDir.resolve("alpha.xtf");
        Files.writeString(first, "alpha-1", StandardCharsets.UTF_8);
        Files.writeString(second, "alpha-2", StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class,
                () -> new BatchFileCopier().copyFiles(List.of(first, second), tempDir.resolve("target")));
    }
}
