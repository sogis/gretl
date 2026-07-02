package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XtfCopyTest {
    @TempDir
    Path tempDir;

    @Test
    void copiesRequestedXtfFiles() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path targetDir = tempDir.resolve("target");
        Files.writeString(sourceDir.resolve("alpha.xtf"), "alpha", StandardCharsets.UTF_8);
        Files.writeString(sourceDir.resolve("beta.xtf"), "beta", StandardCharsets.UTF_8);
        XtfCopyParams params = XtfCopyParams.of(sourceDir, targetDir, XtfCopyParams.TransferFileType.XTF,
                TransferFileList.of("alpha", "beta"));

        List<Path> copiedFiles = new XtfCopy().copyFiles(params);

        assertEquals(List.of(targetDir.resolve("alpha.xtf"), targetDir.resolve("beta.xtf")),
                copiedFiles);
        assertTrue(Files.exists(targetDir.resolve("alpha.xtf")));
        assertTrue(Files.exists(targetDir.resolve("beta.xtf")));
        assertEquals("alpha", Files.readString(targetDir.resolve("alpha.xtf")));
        assertEquals("beta", Files.readString(targetDir.resolve("beta.xtf")));
    }

    @Test
    void copiesRequestedItfFiles() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path targetDir = tempDir.resolve("target");
        Files.writeString(sourceDir.resolve("alpha.itf"), "alpha", StandardCharsets.UTF_8);
        XtfCopyParams params = XtfCopyParams.of(sourceDir, targetDir, XtfCopyParams.TransferFileType.ITF,
                TransferFileList.of("alpha"));

        List<Path> copiedFiles = new XtfCopy().copyFiles(params);

        assertEquals(List.of(targetDir.resolve("alpha.itf")), copiedFiles);
    }

    @Test
    void createsTargetDirectoryTree() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path targetDir = tempDir.resolve("nested").resolve("target");
        Files.writeString(sourceDir.resolve("alpha.xtf"), "alpha", StandardCharsets.UTF_8);
        XtfCopyParams params = XtfCopyParams.of(sourceDir, targetDir, XtfCopyParams.TransferFileType.XTF,
                TransferFileList.of("alpha"));

        new XtfCopy().execute(params);

        assertTrue(Files.exists(targetDir.resolve("alpha.xtf")));
    }

    @Test
    void rejectsMissingRequestedSourceFileWithoutCrossExtensionFallback() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("alpha.itf"), "alpha", StandardCharsets.UTF_8);
        XtfCopyParams params = XtfCopyParams.of(sourceDir, tempDir.resolve("target"),
                XtfCopyParams.TransferFileType.XTF, TransferFileList.of("alpha"));

        assertThrows(NoSuchFileException.class,
                () -> new XtfCopy().execute(params));
    }

    @Test
    void rejectsNonDirectorySourcePath() throws Exception {
        Path sourceFile = Files.writeString(tempDir.resolve("source.xtf"), "alpha", StandardCharsets.UTF_8);
        XtfCopyParams params = XtfCopyParams.of(sourceFile, tempDir.resolve("target"),
                XtfCopyParams.TransferFileType.XTF, TransferFileList.of("alpha"));

        assertThrows(IllegalArgumentException.class,
                () -> new XtfCopy().execute(params));
    }
}
