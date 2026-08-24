package ch.so.agi.gretl.steps.publisher.util.env;

import java.nio.file.Path;

public final class PublisherEnv {
    private final PupDateEnv pupDateEnv;
    private final PubFolderEnv pubFolderEnv;
    private final String jsonmetaAddress;
    private final String jsonmetaBucket;
    private final String jsonmetaFileName;
    private final String modeldir;
    private final Path groomingConfigFilePath;

    public PublisherEnv(PupDateEnv pupDateEnv, PubFolderEnv pubFolderEnv, String jsonmetaAddress,
            String jsonmetaBucket, String jsonmetaFileName, String modeldir, Path groomingConfigFilePath) {
        this.pupDateEnv = pupDateEnv;
        this.pubFolderEnv = pubFolderEnv;
        this.jsonmetaAddress = jsonmetaAddress;
        this.jsonmetaBucket = jsonmetaBucket;
        this.jsonmetaFileName = jsonmetaFileName;
        this.modeldir = modeldir;
        this.groomingConfigFilePath = groomingConfigFilePath;
    }

    public static PublisherEnv empty() {
        return new PublisherEnv(null, null, null, null, null, null, null);
    }

    public PupDateEnv getPupDateEnv() {
        return pupDateEnv;
    }

    public PubFolderEnv getPubFolderEnv() {
        return pubFolderEnv;
    }

    public String getJsonmetaAddress() { return jsonmetaAddress; }
    public String getJsonmetaBucket() { return jsonmetaBucket; }
    public String getJsonmetaFileName() { return jsonmetaFileName; }

    public String getModeldir() {
        return modeldir;
    }

    public Path getGroomingConfigFilePath() {
        return groomingConfigFilePath;
    }
}
