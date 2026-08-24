package ch.so.agi.gretl.steps.publisher;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.Ehi2GretlAdapter;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;
import ch.so.agi.gretl.steps.publisher.operation.Operation;

public class PublisherStep {
    private final GretlLogger log;
    private final String taskName;
    private final OpSequenceBuilder opSequenceBuilder;
    private final OpSequenceRunner opSequenceRunner;
    private final Function<DatabaseConfig, Connector> connectorFactory;
    private final PublisherLogFormatter logFormatter;

    public PublisherStep() {
        this(null);
    }

    public PublisherStep(String taskName) {
        this(taskName, LogEnvironment.getLogger(PublisherStep.class), new OpSequenceBuilder(), new OpSequenceRunner(),
                DatabaseConfig::createConnector);
    }

    PublisherStep(String taskName, GretlLogger log, OpSequenceBuilder opSequenceBuilder, OpSequenceRunner opSequenceRunner,
            Function<DatabaseConfig, Connector> connectorFactory) {
        this.taskName = taskName == null ? PublisherStep.class.getSimpleName() : taskName;
        this.log = Objects.requireNonNull(log, "log must not be null");
        this.opSequenceBuilder = Objects.requireNonNull(opSequenceBuilder, "opSequenceBuilder must not be null");
        this.opSequenceRunner = Objects.requireNonNull(opSequenceRunner, "opSequenceRunner must not be null");
        this.connectorFactory = Objects.requireNonNull(connectorFactory, "connectorFactory must not be null");
        this.logFormatter = new PublisherLogFormatter();
    }

    /** Runs a complete publication, including its database connection lifecycle. */
    public void publish(RawPublisherArgs rawPublisherArgs, PublisherEnv publisherEnv, Path cacheRoot) throws Exception {
        Objects.requireNonNull(rawPublisherArgs, "rawPublisherArgs must not be null");
        Objects.requireNonNull(publisherEnv, "publisherEnv must not be null");
        Objects.requireNonNull(cacheRoot, "cacheRoot must not be null");

        ResolvedPublisherArgs resolvedArgs = new ResolvedPublisherArgs(rawPublisherArgs, publisherEnv, new Date());
        long startedAt = System.nanoTime();
        log.lifecycle(logFormatter.start(taskName, resolvedArgs));
        logOverrideMessages(rawPublisherArgs, publisherEnv);

        Connector sourceConnector = null;
        Connector publicationConnector = null;
        Connection sourceConnection = null;
        Connection publicationConnection = null;
        try {
            if (rawPublisherArgs.getXtfFile_FolderPath() == null) {
                sourceConnector = connectorFactory.apply(resolvedArgs.getDbDatabase());
                sourceConnection = sourceConnector.connect();
            }
            if (resolvedArgs.getOutWriteMetadata()) {
                publicationConnector = connectorFactory.apply(resolvedArgs.getPublicationDatabase());
                publicationConnection = publicationConnector.connect();
            }

            List<Operation> steps = opSequenceBuilder.buildSequence(resolvedArgs, sourceConnection,
                    publicationConnection, cacheRoot);
            // A preceding non-Publisher ili2db task may have restored the EHI
            // console listener. Set up the bridge once for this publication;
            // PublisherIli2dbRunner prevents ili2db from adding its own logger.
            Ehi2GretlAdapter.init();
            try (AutoCloseable ignored = Ehi2GretlAdapter.beginPublisherLogging()) {
                opSequenceRunner.run(steps);
            }
            if (publicationConnection != null) {
                publicationConnection.commit();
            }
        } catch (Exception e) {
            rollbackQuietly(publicationConnection);
            throw e;
        } finally {
            rollbackQuietly(sourceConnection);
            closeQuietly(sourceConnector);
            closeQuietly(publicationConnector);
        }

        log.lifecycle(logFormatter.success(taskName, resolvedArgs, Duration.ofNanos(System.nanoTime() - startedAt)));
    }

    private void logOverrideMessages(RawPublisherArgs rawPublisherArgs, PublisherEnv publisherEnv) {
        java.util.List<String> overrides = new java.util.ArrayList<>();
        if (rawPublisherArgs.getOutFolderPath() != null && publisherEnv.getPubFolderEnv() != null
                && publisherEnv.getPubFolderEnv().getPath() != null) {
            overrides.add("output target");
        }
        if (rawPublisherArgs.getOutGroomingConfigFilePath() != null && publisherEnv.getGroomingConfigFilePath() != null) {
            overrides.add("grooming configuration");
        }
        if (rawPublisherArgs.getCustomModelDir() != null && publisherEnv.getModeldir() != null) {
            overrides.add("model directory");
        }
        if (!overrides.isEmpty()) {
            log.info("Publisher overrides: " + String.join(", ", overrides));
        }
    }

    private static void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try { connection.rollback(); } catch (SQLException ignored) { }
        }
    }

    private static void closeQuietly(Connector connector) {
        if (connector != null) {
            try { connector.close(); } catch (SQLException ignored) { }
        }
    }

}
