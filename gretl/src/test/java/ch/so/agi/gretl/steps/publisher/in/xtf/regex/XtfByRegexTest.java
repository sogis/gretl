package ch.so.agi.gretl.steps.publisher.in.xtf.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XtfByRegexTest {
    @TempDir
    Path tempDir;

    @Test
    void copiesMatchingXtfAndItfFilesInDeterministicOrder() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path targetDir = tempDir.resolve("target");
        Files.writeString(sourceDir.resolve("b.xtf"), "b", StandardCharsets.UTF_8);
        Files.writeString(sourceDir.resolve("a.itf"), "a", StandardCharsets.UTF_8);
        Files.writeString(sourceDir.resolve("ignore.txt"), "ignore", StandardCharsets.UTF_8);
        Files.createDirectory(sourceDir.resolve("nested"));

        List<Path> copiedFiles = new XtfByRegex().copyFiles(
                XtfByRegexParams.of(sourceDir, targetDir, ".*\\.(xtf|itf)$"));

        assertEquals(List.of(targetDir.resolve("a.itf"), targetDir.resolve("b.xtf")),
                copiedFiles);
        assertTrue(Files.exists(targetDir.resolve("a.itf")));
        assertTrue(Files.exists(targetDir.resolve("b.xtf")));
        assertEquals("a", Files.readString(targetDir.resolve("a.itf")));
        assertEquals("b", Files.readString(targetDir.resolve("b.xtf")));
    }

    @Test
    void createsTargetDirectoryTree() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path targetDir = tempDir.resolve("nested").resolve("target");
        Files.writeString(sourceDir.resolve("only.xtf"), "content", StandardCharsets.UTF_8);

        new XtfByRegex().execute(XtfByRegexParams.of(sourceDir, targetDir, ".*\\.xtf$"));

        assertTrue(Files.exists(targetDir.resolve("only.xtf")));
    }

    @Test
    void copiesOnlyTheExactEscapedFilename() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path targetDir = tempDir.resolve("target");
        String filename = "a+b.XTF";
        Files.writeString(sourceDir.resolve(filename), "selected", StandardCharsets.UTF_8);
        Files.writeString(sourceDir.resolve("ab.XTF"), "not selected", StandardCharsets.UTF_8);

        List<Path> copiedFiles = new XtfByRegex().copyFiles(
                XtfByRegexParams.of(sourceDir, targetDir, Pattern.quote(filename)));

        assertEquals(List.of(targetDir.resolve(filename)), copiedFiles);
        assertEquals("selected", Files.readString(targetDir.resolve(filename)));
        assertTrue(Files.notExists(targetDir.resolve("ab.XTF")));
    }

    @Test
    void rejectsNonDirectorySourcePath() throws Exception {
        Path sourceFile = Files.writeString(tempDir.resolve("source.xtf"), "content", StandardCharsets.UTF_8);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new XtfByRegex().execute(
                        XtfByRegexParams.of(sourceFile, tempDir.resolve("target"), ".*")));

        assertTrue(exception.getMessage().contains("must be an existing directory"));
    }

    @Test
    void rejectsEmptyMatchSet() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("a.xtf"), "content", StandardCharsets.UTF_8);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new XtfByRegex().execute(
                        XtfByRegexParams.of(sourceDir, tempDir.resolve("target"), ".*\\.itf$")));

        assertTrue(exception.getMessage().contains("did not match any files"));
    }
}
