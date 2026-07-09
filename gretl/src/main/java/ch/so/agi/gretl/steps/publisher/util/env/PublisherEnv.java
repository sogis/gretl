package ch.so.agi.gretl.steps.publisher.util.env;

import java.net.URI;
import java.nio.file.Path;

public final class PublisherEnv {
    private final PupDateEnv pupDateEnv;
    private final PubFolderEnv pubFolderEnv;
    private final URI datasheetUri;
    private final String modeldir;
    private final Path groomingConfigFilePath;

    public PublisherEnv(PupDateEnv pupDateEnv, PubFolderEnv pubFolderEnv, URI datasheetUri, String modeldir,
            Path groomingConfigFilePath) {
        this.pupDateEnv = pupDateEnv;
        this.pubFolderEnv = pubFolderEnv;
        this.datasheetUri = datasheetUri;
        this.modeldir = modeldir;
        this.groomingConfigFilePath = groomingConfigFilePath;
    }

    public PupDateEnv getPupDateEnv() {
        return pupDateEnv;
    }

    public PubFolderEnv getPubFolderEnv() {
        return pubFolderEnv;
    }

    public URI getDatasheetUri() {
        return datasheetUri;
    }

    public String getModeldir() {
        return modeldir;
    }

    public Path getGroomingConfigFilePath() {
        return groomingConfigFilePath;
    }
}
