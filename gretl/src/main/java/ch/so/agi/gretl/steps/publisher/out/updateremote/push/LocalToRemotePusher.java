package ch.so.agi.gretl.steps.publisher.out.updateremote.push;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public final class LocalToRemotePusher {
    public void push(Path localStageRoot, Path remoteTempStageRoot) throws IOException {
        Path checkedLocalStageRoot = Objects.requireNonNull(localStageRoot, "localStageRoot");
        Path checkedRemoteTempStageRoot = Objects.requireNonNull(remoteTempStageRoot, "remoteTempStageRoot");

        if (!Files.isDirectory(checkedLocalStageRoot)) {
            throw new IllegalArgumentException("localStageRoot must be a directory: " + checkedLocalStageRoot);
        }

        Files.createDirectories(checkedRemoteTempStageRoot);
        Files.walkFileTree(checkedLocalStageRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(resolveTarget(dir));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, resolveTarget(file), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            private Path resolveTarget(Path source) {
                return checkedRemoteTempStageRoot.resolve(checkedLocalStageRoot.relativize(source));
            }
        });
    }
}
