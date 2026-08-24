package ch.so.agi.gretl.steps.publisher.stage.pack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.so.agi.gretl.steps.publisher.PartWorkspace;
import ch.so.agi.gretl.steps.publisher.operation.Operation;

/** Packages each raw part/format artifact into a flattened publication archive. */
public final class PublicationArtifactPackager implements Operation {
    private final PublicationArtifactPackagerParameters parameters;
    private int artifactCount;

    public PublicationArtifactPackager(PublicationArtifactPackagerParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
    }

    @Override public void execute() throws IOException { artifactCount = packageArtifacts(); }
    @Override public String getHumanReadableName() { return "Packer for publication zip's"; }
    @Override public String getSuccessLogDetail() { return "created " + artifactCount + " publication zip(s)"; }

    private int packageArtifacts() throws IOException {
        Path rawRoot = parameters.getRawRoot();
        if (!Files.isDirectory(rawRoot)) {
            throw new IllegalArgumentException("rawRoot must be a directory: " + rawRoot);
        }
        Files.createDirectories(parameters.getPublicationRoot());
        int count = 0;
        for (Path partRoot : partRoots(rawRoot)) {
            String partKey = partRoot.getFileName().toString();
            PartWorkspace.decode(partKey);
            List<FormatSource> sources = new ArrayList<>();
            for (OutputFormat format : parameters.getOutputFormats()) {
                Path source = sourceFor(partRoot, format);
                if (source == null) {
                    throw new IllegalArgumentException("Requested " + (format.isTransferFormat() ? "transfer" : "derived")
                            + " format " + format.getIdentifier() + " is not available for part " + partKey);
                }
                sources.add(new FormatSource(format, source));
            }
            for (FormatSource formatSource : sources) {
                OutputFormat format = formatSource.format();
                Path archive = parameters.getPublicationRoot().resolve(partKey + "." + format.getIdentifier() + ".zip");
                new Zipper(parameters.getPublicationRoot(), partKey).zip(formatSource.source(), archive);
                count++;
            }
        }
        return count;
    }

    private List<Path> partRoots(Path rawRoot) throws IOException {
        try (Stream<Path> stream = Files.list(rawRoot)) {
            return stream.filter(Files::isDirectory).sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .collect(Collectors.toList());
        }
    }

    private Path sourceFor(Path partRoot, OutputFormat format) throws IOException {
        if (!format.isTransferFormat()) {
            Path directory = partRoot.resolve(format.getDerivedFormat().getDirectoryName());
            return Files.isDirectory(directory) ? directory : null;
        }
        Path transferRoot = partRoot.resolve(PartWorkspace.TRANSFER);
        if (!Files.isDirectory(transferRoot)) return null;
        try (Stream<Path> stream = Files.list(transferRoot)) {
            List<Path> matches = stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(java.util.Locale.ROOT)
                            .endsWith("." + format.getIdentifier()))
                    .sorted().collect(Collectors.toList());
            if (matches.size() > 1) throw new IllegalArgumentException("part " + partRoot + " has multiple "
                    + format.getIdentifier() + " transfer files");
            return matches.isEmpty() ? null : matches.get(0);
        }
    }

    private static final class FormatSource {
        private final OutputFormat format;
        private final Path source;

        private FormatSource(OutputFormat format, Path source) {
            this.format = format;
            this.source = source;
        }

        private OutputFormat format() { return format; }
        private Path source() { return source; }
    }
}
