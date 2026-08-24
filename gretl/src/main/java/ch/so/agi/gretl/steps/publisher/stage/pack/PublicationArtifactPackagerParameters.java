package ch.so.agi.gretl.steps.publisher.stage.pack;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

/** Inputs for packaging a part-first raw workspace into a publication directory. */
public final class PublicationArtifactPackagerParameters implements OperationParameters {
    private final Path rawRoot;
    private final Path publicationRoot;
    private final List<OutputFormat> outputFormats;

    private PublicationArtifactPackagerParameters(Path rawRoot, Path publicationRoot, List<OutputFormat> outputFormats) {
        this.rawRoot = Objects.requireNonNull(rawRoot, "rawRoot");
        this.publicationRoot = Objects.requireNonNull(publicationRoot, "publicationRoot");
        Objects.requireNonNull(outputFormats, "outputFormats");
        this.outputFormats = Collections.unmodifiableList(new ArrayList<OutputFormat>(outputFormats));
    }

    public static PublicationArtifactPackagerParameters of(Path rawRoot, Path publicationRoot,
            List<OutputFormat> outputFormats) {
        return new PublicationArtifactPackagerParameters(rawRoot, publicationRoot, outputFormats);
    }
    public Path getRawRoot() { return rawRoot; }
    public Path getPublicationRoot() { return publicationRoot; }
    public List<OutputFormat> getOutputFormats() { return outputFormats; }
}
