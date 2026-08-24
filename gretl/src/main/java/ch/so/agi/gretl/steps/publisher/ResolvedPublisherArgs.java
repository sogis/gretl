package ch.so.agi.gretl.steps.publisher;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;
import ch.so.agi.gretl.steps.publisher.util.env.PubFolderEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PupDateEnv;

/** Immutable publisher arguments after global defaults and the execution timestamp are resolved. */
public final class ResolvedPublisherArgs {
    private final RawPublisherArgs rawArgs;
    private final Endpoint outFolderPath;
    private final String groomingConfigFilePath;
    private final String modelDir;
    private final Date publicationTimestamp;
    private final DatabaseConfig publicationDatabase;
    private final String metadataSchema;
    private final String jsonmetaAddress;
    private final String jsonmetaBucket;
    private final String jsonmetaFileName;

    public ResolvedPublisherArgs(RawPublisherArgs rawArgs, PublisherEnv publisherEnv, Date publicationTimestamp) {
        this.rawArgs = Objects.requireNonNull(rawArgs, "rawArgs must not be null");
        Objects.requireNonNull(publisherEnv, "publisherEnv must not be null");
        this.publicationTimestamp = new Date(Objects.requireNonNull(publicationTimestamp,
                "publicationTimestamp must not be null").getTime());

        this.outFolderPath = resolveOutput(rawArgs, publisherEnv.getPubFolderEnv());
        this.groomingConfigFilePath = rawArgs.getOutGroomingConfigFilePath() != null
                ? rawArgs.getOutGroomingConfigFilePath() : pathString(publisherEnv.getGroomingConfigFilePath());
        this.modelDir = rawArgs.getCustomModelDir() != null ? rawArgs.getCustomModelDir() : publisherEnv.getModeldir();
        if (rawArgs.getOutWriteMetadata()) {
            PupDateEnv metadataEnvironment = Objects.requireNonNull(publisherEnv.getPupDateEnv(),
                    "Publisher metadata settings must be configured when writing metadata");
            this.jsonmetaAddress = publisherEnv.getJsonmetaAddress();
            this.jsonmetaBucket = publisherEnv.getJsonmetaBucket();
            this.jsonmetaFileName = publisherEnv.getJsonmetaFileName();
            this.publicationDatabase = new DatabaseConfig(metadataEnvironment.getConnectionUrl(),
                    metadataEnvironment.getUser(), metadataEnvironment.getPassword());
            this.metadataSchema = metadataEnvironment.getDbSchema();
            if (publicationDatabase.getUrl() == null || metadataSchema == null || metadataSchema.trim().isEmpty()) {
                throw new IllegalArgumentException("Publisher metadata database and schema must be configured when writing metadata");
            }
        } else {
            this.jsonmetaAddress = null;
            this.jsonmetaBucket = null;
            this.jsonmetaFileName = null;
            this.publicationDatabase = null;
            this.metadataSchema = null;
        }
    }

    private static Endpoint resolveOutput(RawPublisherArgs rawArgs, PubFolderEnv pubFolderEnv) {
        if (rawArgs.getOutFolderPath() != null) {
            return rawArgs.getOutFolderPath();
        }
        if (pubFolderEnv == null) {
            throw new IllegalArgumentException("outFolderPath must be set when Publisher global settings are unavailable");
        }
        return new Endpoint(pubFolderEnv.getPath(), pubFolderEnv.getUser(), pubFolderEnv.getPassword());
    }

    private static String pathString(Path path) {
        return path == null ? null : path.toString();
    }

    public DatabaseConfig getDbDatabase() { return rawArgs.getDbDatabase(); }
    public String getDbSchema() { return rawArgs.getDbSchema(); }
    public String getDbIliIdent_Type() { return rawArgs.getDbIliIdent_Type(); }
    public List<String> getDbIliIdent_Values() { return rawArgs.getDbIliIdent_Values(); }
    public String getDbIliIdent_RegEx() { return rawArgs.getDbIliIdent_RegEx(); }
    public Boolean getDbMergeToSingleXtf() { return rawArgs.getDbMergeToSingleXtf(); }
    public RawPublisherArgs.IliIdentType getDbIliIdentType() { return rawArgs.getDbIliIdentType(); }
    public String getXtfFile_FolderPath() { return rawArgs.getXtfFile_FolderPath(); }
    public String getXtfFilename_Regex() { return rawArgs.getXtfFilename_Regex(); }
    public List<String> getXtfFilename_List() { return rawArgs.getXtfFilename_List(); }
    public Endpoint getOutFolderPath() { return outFolderPath; }
    public String getOutDataIdent() { return rawArgs.getOutDataIdent(); }
    public Boolean getOutWriteMetadata() { return rawArgs.getOutWriteMetadata(); }
    public String getOutGroomingConfigFilePath() { return groomingConfigFilePath; }
    public String getOutValidationConfigFilePath() { return rawArgs.getOutValidationConfigFilePath(); }
    public String getCustomModelDir() { return modelDir; }
    public List<OutputFormat> getOutFormats() { return rawArgs.getOutFormats(); }
    public Date getPublicationTimestamp() { return new Date(publicationTimestamp.getTime()); }
    public RawPublisherArgs.PublishMode getPublishMode() { return rawArgs.getPublishMode(); }
    public DatabaseConfig getPublicationDatabase() { return publicationDatabase; }
    public String getMetadataSchema() { return metadataSchema; }
    public String getJsonmetaAddress() { return jsonmetaAddress; }
    public String getJsonmetaBucket() { return jsonmetaBucket; }
    public String getJsonmetaFileName() { return jsonmetaFileName; }
}
