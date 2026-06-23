package ch.so.agi.gretl.steps.publisher.out.updateremote.push;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalToRemotePusherTest {
    @TempDir
    Path tempDir;

    @Test
    void copiesLocalStageTreeAndReplacesExistingFiles() throws Exception {
        Path localStageRoot = Files.createDirectories(tempDir.resolve("local"));
        Files.writeString(localStageRoot.resolve("data.xtf.zip"), "new", StandardCharsets.UTF_8);
        Path meta = Files.createDirectories(localStageRoot.resolve("meta"));
        Files.writeString(meta.resolve("publishdate.json"), "{}", StandardCharsets.UTF_8);

        Path remoteTempStageRoot = Files.createDirectories(tempDir.resolve("remote"));
        Files.writeString(remoteTempStageRoot.resolve("data.xtf.zip"), "old", StandardCharsets.UTF_8);

        new LocalToRemotePusher().push(localStageRoot, remoteTempStageRoot);

        assertEquals("new", Files.readString(remoteTempStageRoot.resolve("data.xtf.zip")));
        assertTrue(Files.isRegularFile(remoteTempStageRoot.resolve("meta").resolve("publishdate.json")));
    }
}
