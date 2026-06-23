package ch.so.agi.gretl.steps.publisher.out.updateremote.rotate;

import ch.so.agi.gretl.steps.publisher.out.updateremote.RemotePublicationPaths;
import ch.so.agi.gretl.util.Grooming;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Objects;

public final class LatestRotator {
    public void rotate(Path dataRoot, Path tempStageRoot, Date publishDate) throws IOException {
        Path checkedDataRoot = Objects.requireNonNull(dataRoot, "dataRoot");
        Path checkedTempStageRoot = Objects.requireNonNull(tempStageRoot, "tempStageRoot");
        Date checkedPublishDate = Objects.requireNonNull(publishDate, "publishDate");

        Path currentRoot = RemotePublicationPaths.currentRoot(checkedDataRoot);
        Path historyRoot = RemotePublicationPaths.historyRoot(checkedDataRoot);
        Path historyTarget = historyRoot.resolve(dateTag(checkedPublishDate));

        if (Files.exists(currentRoot)) {
            Files.createDirectories(historyRoot);
            if (Files.exists(historyTarget)) {
                deleteTree(currentRoot);
            } else {
                Files.move(currentRoot, historyTarget);
                removeHistoricalUserFormatArchives(historyTarget);
            }
        }

        Files.move(checkedTempStageRoot, currentRoot);
    }

    private static String dateTag(Date date) {
        return Grooming.getDateFormat().format(date);
    }

    private static void removeHistoricalUserFormatArchives(Path historyTarget) throws IOException {
        try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(historyTarget)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file) && isUserFormatArchive(file)) {
                    Files.deleteIfExists(file);
                }
            }
        }
    }

    private static boolean isUserFormatArchive(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.endsWith(".shp.zip") || fileName.endsWith(".gpkg.zip") || fileName.endsWith(".dxf.zip");
    }

    private static void deleteTree(Path path) throws IOException {
        if (Files.notExists(path)) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
