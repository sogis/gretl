package ch.so.agi.gretl.steps.publisher.out.updateremote.seed;

import ch.so.agi.gretl.steps.publisher.out.updateremote.RemotePublicationPaths;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class StageSeeder {
    public void seed(Path dataRoot, Path tempStageRoot) throws IOException {
        Path currentRoot = RemotePublicationPaths.currentRoot(Objects.requireNonNull(dataRoot, "dataRoot"));
        Path checkedTempStageRoot = Objects.requireNonNull(tempStageRoot, "tempStageRoot");

        if (Files.notExists(currentRoot)) {
            return;
        }
        if (!Files.isDirectory(currentRoot)) {
            throw new IllegalArgumentException("currentRoot must be a directory: " + currentRoot);
        }

        Files.createDirectories(checkedTempStageRoot);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentRoot)) {
            for (Path source : stream) {
                if (Files.isRegularFile(source) && isZipFile(source)) {
                    Files.copy(source, checkedTempStageRoot.resolve(source.getFileName()));
                }
            }
        }
    }

    private static boolean isZipFile(Path path) {
        return path.getFileName().toString().endsWith(".zip");
    }
}
