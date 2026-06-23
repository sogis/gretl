package ch.so.agi.gretl.steps.publisher.in.xtf.regex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import ch.so.agi.gretl.steps.publisher.util.copy.BatchFileCopier;

/**
 * Selects direct-child transfer files from a source directory by regular
 * expression and copies the matches to a target directory.
 */
public final class XtfByRegex {
    private final BatchFileCopier batchFileCopier;

    public XtfByRegex() {
        this(new BatchFileCopier());
    }

    XtfByRegex(BatchFileCopier batchFileCopier) {
        this.batchFileCopier = Objects.requireNonNull(batchFileCopier, "batchFileCopier must not be null");
    }

    public List<Path> execute(Path sourceDir, Path targetDir, XtfByRegexParams params) throws IOException {
        Objects.requireNonNull(sourceDir, "sourceDir must not be null");
        Objects.requireNonNull(targetDir, "targetDir must not be null");
        Objects.requireNonNull(params, "params must not be null");

        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("sourceDir <" + sourceDir + "> must be an existing directory");
        }

        List<Path> matchingFiles = findMatchingFiles(sourceDir, params);
        if (matchingFiles.isEmpty()) {
            throw new IllegalArgumentException("regex <" + params.getFileNameRegex() + "> did not match any files");
        }

        return batchFileCopier.copyFiles(matchingFiles, targetDir);
    }

    private List<Path> findMatchingFiles(Path sourceDir, XtfByRegexParams params) throws IOException {
        List<Path> matchingFiles = new ArrayList<Path>();
        try (Stream<Path> stream = Files.list(sourceDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> params.getFileNamePattern().matcher(path.getFileName().toString()).matches())
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(matchingFiles::add);
        }
        return matchingFiles;
    }
}
