package ch.so.agi.gretl.steps.publisher.stage.pack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Responsibility: compute publication archive names and move the staged zips to
 * their final cache locations.
 */
public final class Renamer {
    private final Path cacheDir;
    private final String dataIdent;

    public Renamer(Path cacheDir, String dataIdent) {
        this.cacheDir = Objects.requireNonNull(cacheDir, "cacheDir");
        this.dataIdent = requireText(dataIdent, "dataIdent");
    }

    public Path archivePath(Path source) {
        Objects.requireNonNull(source, "source");

        String sourceName = source.getFileName().toString();
        if (sourceName.endsWith(".zip")) {
            return cacheDir.resolve(sourceName);
        }
        return cacheDir.resolve(sourceName + ".zip");
    }

    public Path moveArchive(Path stagedArchive, Path source) throws IOException {
        Objects.requireNonNull(stagedArchive, "stagedArchive");
        Objects.requireNonNull(source, "source");

        Path target = archivePath(source);
        Files.createDirectories(target.getParent());
        return Files.move(stagedArchive, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public String getDataIdent() {
        return dataIdent;
    }

    private static String requireText(String value, String name) {
        if (value == null) {
            throw new NullPointerException(name);
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
