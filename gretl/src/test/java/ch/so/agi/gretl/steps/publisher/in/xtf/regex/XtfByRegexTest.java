package ch.so.agi.gretl.steps.publisher.in.xtf.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XtfByRegexTest {
    @TempDir
    Path tempDir;

    @Test
    void copiesMatchingXtfAndItfFilesInDeterministicOrder() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("b.xtf"), "b", StandardCharsets.UTF_8);
        Files.writeString(sourceDir.resolve("a.itf"), "a", StandardCharsets.UTF_8);
        Files.writeString(sourceDir.resolve("ignore.txt"), "ignore", StandardCharsets.UTF_8);
        Files.createDirectory(sourceDir.resolve("nested"));

        List<Path> copiedFiles = new XtfByRegex().execute(sourceDir, tempDir.resolve("target"),
                XtfByRegexParams.of(".*\\.(xtf|itf)$"));

        assertEquals(List.of(tempDir.resolve("target").resolve("a.itf"), tempDir.resolve("target").resolve("b.xtf")),
                copiedFiles);
        assertTrue(Files.exists(tempDir.resolve("target").resolve("a.itf")));
        assertTrue(Files.exists(tempDir.resolve("target").resolve("b.xtf")));
        assertEquals("a", Files.readString(tempDir.resolve("target").resolve("a.itf")));
        assertEquals("b", Files.readString(tempDir.resolve("target").resolve("b.xtf")));
    }

    @Test
    void createsTargetDirectoryTree() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("only.xtf"), "content", StandardCharsets.UTF_8);

        new XtfByRegex().execute(sourceDir, tempDir.resolve("nested").resolve("target"),
                XtfByRegexParams.of(".*\\.xtf$"));

        assertTrue(Files.exists(tempDir.resolve("nested").resolve("target").resolve("only.xtf")));
    }

    @Test
    void rejectsNonDirectorySourcePath() throws Exception {
        Path sourceFile = Files.writeString(tempDir.resolve("source.xtf"), "content", StandardCharsets.UTF_8);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new XtfByRegex().execute(sourceFile, tempDir.resolve("target"), XtfByRegexParams.of(".*")));

        assertTrue(exception.getMessage().contains("must be an existing directory"));
    }

    @Test
    void rejectsEmptyMatchSet() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("a.xtf"), "content", StandardCharsets.UTF_8);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new XtfByRegex().execute(sourceDir, tempDir.resolve("target"),
                        XtfByRegexParams.of(".*\\.itf$")));

        assertTrue(exception.getMessage().contains("did not match any files"));
    }
}
