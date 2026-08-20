package ch.so.agi.gretl.steps.publisher;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;

public class PublisherStep {
    private final GretlLogger log;
    private final String taskName;
    private final OpSequenceBuilder opSequenceBuilder;
    private final OpSequenceRunner opSequenceRunner;

    public PublisherStep() {
        this(null);
    }

    public PublisherStep(String taskName) {
        this(taskName, LogEnvironment.getLogger(PublisherStep.class), new OpSequenceBuilder(), new OpSequenceRunner());
    }

    PublisherStep(String taskName, GretlLogger log, OpSequenceBuilder opSequenceBuilder, OpSequenceRunner opSequenceRunner) {
        this.taskName = taskName == null ? PublisherStep.class.getSimpleName() : taskName;
        this.log = Objects.requireNonNull(log, "log must not be null");
        this.opSequenceBuilder = Objects.requireNonNull(opSequenceBuilder, "opSequenceBuilder must not be null");
        this.opSequenceRunner = Objects.requireNonNull(opSequenceRunner, "opSequenceRunner must not be null");
    }

    public void publish(Date date, RawPublisherArgs rawPublisherArgs, PublisherEnv publisherEnv,
            Connection publicationDbConnection, Path cacheRoot) throws Exception {
        publish(date, rawPublisherArgs, publisherEnv, publicationDbConnection, publicationDbConnection, cacheRoot);
    }

    /** Runs a publication with distinct source and publication-metadata connections. */
    public void publish(Date date, RawPublisherArgs rawPublisherArgs, PublisherEnv publisherEnv,
            Connection sourceDbConnection, Connection publicationDbConnection, Path cacheRoot) throws Exception {
        publish(date, rawPublisherArgs, publisherEnv, sourceDbConnection, publicationDbConnection, "public", true,
                cacheRoot);
    }

    /** Runs a publication with optional post-promotion metadata persistence. */
    public void publish(Date date, RawPublisherArgs rawPublisherArgs, PublisherEnv publisherEnv,
            Connection sourceDbConnection, Connection publicationDbConnection, String metadataSchema, boolean writeMetadata,
            Path cacheRoot) throws Exception {
        Objects.requireNonNull(rawPublisherArgs, "rawPublisherArgs must not be null");
        Objects.requireNonNull(publisherEnv, "publisherEnv must not be null");
        if (writeMetadata) {
            Objects.requireNonNull(publicationDbConnection, "publicationDbConnection must not be null when writing metadata");
        }
        if (rawPublisherArgs.getXtfFile_FolderPath() == null) {
            Objects.requireNonNull(sourceDbConnection, "sourceDbConnection must not be null in db mode");
        }
        Objects.requireNonNull(cacheRoot, "cacheRoot must not be null");

        Date effectiveDate = date != null ? new Date(date.getTime()) : new Date();
        log.lifecycle(taskName + ": Start PublisherStep");

        RawPublisherArgs effectiveArgs = buildEffectiveArgs(rawPublisherArgs, publisherEnv, effectiveDate);
        logOverrideMessages(rawPublisherArgs, publisherEnv);
        logModeDetails(effectiveArgs);

        List<OpSequenceStep> steps = opSequenceBuilder.buildSequence(effectiveArgs, sourceDbConnection,
                publicationDbConnection, metadataSchema, writeMetadata, cacheRoot);
        opSequenceRunner.run(steps);

        log.lifecycle(taskName + ": End PublisherStep (successful)");
    }

    private RawPublisherArgs buildEffectiveArgs(RawPublisherArgs rawPublisherArgs, PublisherEnv publisherEnv, Date effectiveDate) {
        Endpoint effectiveOutput = resolveOutputEndpoint(rawPublisherArgs, publisherEnv);
        String effectiveGrooming = rawPublisherArgs.getOutCustomGroomingConfFilePath();
        if (effectiveGrooming == null && publisherEnv.getGroomingConfigFilePath() != null) {
            effectiveGrooming = publisherEnv.getGroomingConfigFilePath().toString();
        }
        String effectiveModelDir = rawPublisherArgs.getCustomModelDir();
        if (effectiveModelDir == null) {
            effectiveModelDir = publisherEnv.getModeldir();
        }

        return new RawPublisherArgs(
                rawPublisherArgs.getDbDatabase(),
                rawPublisherArgs.getDbSchema(),
                rawPublisherArgs.getDbIliIdent_Type(),
                toArrayList(rawPublisherArgs.getDbIliIdent_Values()),
                rawPublisherArgs.getDbIliIdent_RegEx(),
                rawPublisherArgs.getDbMergeToSingleXtf(),
                rawPublisherArgs.getXtfFile_FolderPath(),
                rawPublisherArgs.getXtfFilename_Regex(),
                toArrayList(rawPublisherArgs.getXtfFilename_List()),
                effectiveOutput,
                rawPublisherArgs.getOutDataIdent(),
                rawPublisherArgs.getOutIsolatedMode(),
                rawPublisherArgs.getOutWriteToThisLocalFolderOnly(),
                effectiveGrooming,
                rawPublisherArgs.getOutValidationConfigFilePath(),
                effectiveModelDir,
                rawPublisherArgs.getOutDerivedFormats(),
                effectiveDate);
    }

    private Endpoint resolveOutputEndpoint(RawPublisherArgs rawPublisherArgs, PublisherEnv publisherEnv) {
        if (rawPublisherArgs.getOutWriteToThisLocalFolderOnly() != null) {
            return new Endpoint(rawPublisherArgs.getOutWriteToThisLocalFolderOnly());
        }
        if (publisherEnv.getPubFolderEnv() == null) {
            return rawPublisherArgs.getOutBasePath();
        }
        return new Endpoint(
                publisherEnv.getPubFolderEnv().getPath(),
                publisherEnv.getPubFolderEnv().getUser(),
                publisherEnv.getPubFolderEnv().getPassword());
    }

    private void logOverrideMessages(RawPublisherArgs rawPublisherArgs, PublisherEnv publisherEnv) {
        if (rawPublisherArgs.getOutWriteToThisLocalFolderOnly() != null && publisherEnv.getPubFolderEnv() != null
                && publisherEnv.getPubFolderEnv().getPath() != null) {
            log.info("Global publisher setting pubFolder.path overridden by RawPublisherArgs.outWriteToThisLocalFolderOnly: "
                    + rawPublisherArgs.getOutWriteToThisLocalFolderOnly());
        }
        if (rawPublisherArgs.getOutCustomGroomingConfFilePath() != null && publisherEnv.getGroomingConfigFilePath() != null) {
            log.info("Global publisher setting groomingConfigFilePath overridden by RawPublisherArgs.outCustomGroomingConfFilePath: "
                    + rawPublisherArgs.getOutCustomGroomingConfFilePath());
        }
        if (rawPublisherArgs.getCustomModelDir() != null && publisherEnv.getModeldir() != null) {
            log.info("Global publisher setting modeldir overridden by RawPublisherArgs.customModelDir: "
                    + rawPublisherArgs.getCustomModelDir());
        }
    }

    private void logModeDetails(RawPublisherArgs effectiveArgs) {
        log.debug("Publisher mode: " + effectiveArgs.getPublishMode());
        switch (effectiveArgs.getPublishMode()) {
        case dbIdentvaluesList:
            log.debug("Publisher mode args: dbSchema=" + effectiveArgs.getDbSchema()
                    + ", dbIliIdent_Type=" + effectiveArgs.getDbIliIdent_Type()
                    + ", dbIliIdent_Values=" + effectiveArgs.getDbIliIdent_Values()
                    + ", dbMergeToSingleXtf=" + effectiveArgs.getDbMergeToSingleXtf()
                    + ", outDataIdent=" + effectiveArgs.getOutDataIdent()
                    + ", outBasePath=" + effectiveArgs.getOutBasePath().getUrl()
                    + ", modeldir=" + effectiveArgs.getCustomModelDir()
                    + ", groomingConfig=" + effectiveArgs.getOutCustomGroomingConfFilePath());
            break;
        case dbIdentvaluesRegex:
            log.debug("Publisher mode args: dbSchema=" + effectiveArgs.getDbSchema()
                    + ", dbIliIdent_Type=" + effectiveArgs.getDbIliIdent_Type()
                    + ", dbIliIdent_RegEx=" + effectiveArgs.getDbIliIdent_RegEx()
                    + ", dbMergeToSingleXtf=" + effectiveArgs.getDbMergeToSingleXtf()
                    + ", outDataIdent=" + effectiveArgs.getOutDataIdent()
                    + ", outBasePath=" + effectiveArgs.getOutBasePath().getUrl()
                    + ", modeldir=" + effectiveArgs.getCustomModelDir()
                    + ", groomingConfig=" + effectiveArgs.getOutCustomGroomingConfFilePath());
            break;
        case xtfFilesList:
            log.debug("Publisher mode args: xtfFile_FolderPath=" + effectiveArgs.getXtfFile_FolderPath()
                    + ", xtfFilename_List=" + effectiveArgs.getXtfFilename_List()
                    + ", outDataIdent=" + effectiveArgs.getOutDataIdent()
                    + ", outBasePath=" + effectiveArgs.getOutBasePath().getUrl()
                    + ", modeldir=" + effectiveArgs.getCustomModelDir()
                    + ", groomingConfig=" + effectiveArgs.getOutCustomGroomingConfFilePath());
            break;
        case xtfFilesRegex:
            log.debug("Publisher mode args: xtfFile_FolderPath=" + effectiveArgs.getXtfFile_FolderPath()
                    + ", xtfFilename_Regex=" + effectiveArgs.getXtfFilename_Regex()
                    + ", outDataIdent=" + effectiveArgs.getOutDataIdent()
                    + ", outBasePath=" + effectiveArgs.getOutBasePath().getUrl()
                    + ", modeldir=" + effectiveArgs.getCustomModelDir()
                    + ", groomingConfig=" + effectiveArgs.getOutCustomGroomingConfFilePath());
            break;
        default:
            throw new IllegalArgumentException("unsupported publishMode <" + effectiveArgs.getPublishMode() + ">");
        }
    }

    private static ArrayList<String> toArrayList(List<String> values) {
        return values == null ? null : new ArrayList<>(values);
    }
}
