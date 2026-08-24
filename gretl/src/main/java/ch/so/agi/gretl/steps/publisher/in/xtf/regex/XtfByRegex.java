package ch.so.agi.gretl.steps.publisher.in.xtf.regex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.util.BatchFileCopier;

/**
 * Selects direct-child transfer files from a source directory by regular
 * expression and copies the matches to a target directory.
 */
public final class XtfByRegex implements Operation {
    private XtfByRegexParams parameters;
    private final BatchFileCopier batchFileCopier;
    private List<Path> copiedFiles = List.of();

    public XtfByRegex(XtfByRegexParams parameters) {
        this(parameters, new BatchFileCopier());
    }

    @Deprecated public XtfByRegex() { this.batchFileCopier = new BatchFileCopier(); }
    @Deprecated public void execute(XtfByRegexParams parameters) throws IOException { this.parameters = parameters; execute(); }

    XtfByRegex(XtfByRegexParams parameters, BatchFileCopier batchFileCopier) {
        this.parameters = Objects.requireNonNull(parameters, "parameters must not be null");
        this.batchFileCopier = Objects.requireNonNull(batchFileCopier, "batchFileCopier must not be null");
    }

    @Override
    public void execute() throws IOException {
        copiedFiles = copyFiles(parameters);
    }

    @Override
    public String getHumanReadableName() { return "Source xtf/itf regex copy"; }

    @Override
    public String getSuccessLogDetail() {
        return "copied " + copiedFiles.size() + " source file(s) matching " + parameters.getFileNameRegex();
    }

    List<Path> copyFiles(XtfByRegexParams operationParameters) throws IOException {
        Objects.requireNonNull(operationParameters, "operationParameters must not be null");
        Path sourceDir = operationParameters.getSourceDir();
        Path targetDir = operationParameters.getTargetDir();

        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("sourceDir <" + sourceDir + "> must be an existing directory");
        }

        List<Path> matchingFiles = findMatchingFiles(sourceDir, operationParameters);
        if (matchingFiles.isEmpty()) {
            throw new IllegalArgumentException(
                    "regex <" + operationParameters.getFileNameRegex() + "> did not match any files");
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
