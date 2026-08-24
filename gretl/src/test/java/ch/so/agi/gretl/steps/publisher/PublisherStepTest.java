package ch.so.agi.gretl.steps.publisher;

import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.publisherArgs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.steps.publisher.util.env.PubFolderEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PupDateEnv;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;
import ch.so.agi.gretl.steps.publisher.operation.Operation;

class PublisherStepTest {
    @TempDir
    Path tempDir;

    @Test
    void resolvesDefaultsAndOwnsMetadataConnectionLifecycle() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        RecordingBuilder builder = new RecordingBuilder();
        RecordingRunner runner = new RecordingRunner();
        RecordingConnector publication = new RecordingConnector("jdbc:postgresql://db/publisher");
        PublisherStep step = step("publishDemo", logger, builder, runner, null, publication);
        RawPublisherArgs rawArgs = RawPublisherArgs.builder().output(null, "ch.so.agi.demo")
                .xtfRegexSource(tempDir.resolve("incoming").toString(), ".*\\.xtf$")
                .outFormats(List.of(OutputFormat.SHP))
                .build();

        long before = System.currentTimeMillis();
        step.publish(rawArgs, publisherEnv(), tempDir.resolve("cache"));
        long after = System.currentTimeMillis();

        assertTrue(builder.capturedArgs.getPublicationTimestamp().getTime() >= before);
        assertTrue(builder.capturedArgs.getPublicationTimestamp().getTime() <= after);
        assertEquals("sftp://host/data", builder.capturedArgs.getOutFolderPath().getUrl());
        assertEquals("/env/grooming.json", builder.capturedArgs.getOutGroomingConfigFilePath());
        assertEquals("/env/models", builder.capturedArgs.getCustomModelDir());
        assertSame(publication.connection, builder.capturedPublicationConnection);
        assertEquals(1, publication.commitCount);
        assertTrue(publication.closed);
        assertEquals(2, logger.lifecycleMessages.size());
        assertEquals("publishDemo: Publishing ch.so.agi.demo from XTF files (formats: shp)",
                logger.lifecycleMessages.get(0));
        assertTrue(logger.lifecycleMessages.get(1).matches(
                "publishDemo: Published ch\\.so\\.agi\\.demo \\(formats: shp, duration: [0-9.]+ s\\)"));
        assertTrue(logger.lifecycleMessages.stream().noneMatch(message -> message.contains("sftp://host/data")
                || message.contains("aktuell") || message.contains("artifact")));
        assertTrue(logger.lifecycleMessages.stream().noneMatch(message -> message.contains("publisher_pass")
                || message.contains("pub_user")));
        assertTrue(logger.infoMessages.stream().noneMatch(message -> message.startsWith("Publisher configuration:")));
        assertTrue(logger.infoMessages.stream().noneMatch(message -> message.contains("publisher_pass")
                || message.contains("pub_user")));
    }

    @Test
    void opensSourceConnectionForDbPublication() throws Exception {
        RecordingBuilder builder = new RecordingBuilder();
        RecordingConnector source = new RecordingConnector("edit-db");
        RecordingConnector publication = new RecordingConnector("jdbc:postgresql://db/publisher");
        PublisherStep step = step(null, new RecordingLogger(), builder, new RecordingRunner(), source, publication);
        RawPublisherArgs rawArgs = publisherArgs()
                .dbValuesSource(new DatabaseConfig("edit-db", "source_user", "source_pass"), "live",
                        RawPublisherArgs.IliIdentType.dataset, true, List.of("2401"))
                .output(new Endpoint(tempDir.resolve("local-out").toString()), "ch.so.agi.demo")
                .build();

        step.publish(rawArgs, publisherEnv(), tempDir.resolve("cache"));

        assertSame(source.connection, builder.capturedSourceConnection);
        assertSame(publication.connection, builder.capturedPublicationConnection);
        assertTrue(source.closed);
        assertTrue(publication.closed);
    }

    @Test
    void skipsMetadataConnectionForLocalOnlyPublication() throws Exception {
        RecordingBuilder builder = new RecordingBuilder();
        PublisherStep step = step("publishDemo", new RecordingLogger(), builder, new RecordingRunner(), null, null);
        RawPublisherArgs rawArgs = publisherArgs()
                .xtfListSource(tempDir.resolve("incoming").toString(), List.of("north.xtf"))
                .output(new Endpoint(tempDir.resolve("local-out").toString()), "ch.so.agi.demo")
                .writeMetadata(false)
                .build();

        step.publish(rawArgs, PublisherEnv.empty(), tempDir.resolve("cache"));

        assertNull(builder.capturedPublicationConnection);
    }

    @Test
    void rollsBackMetadataWhenExecutionFails() {
        RecordingConnector publication = new RecordingConnector("jdbc:postgresql://db/publisher");
        RecordingLogger logger = new RecordingLogger();
        PublisherStep step = step("publishDemo", logger, new RecordingBuilder(),
                new FailingRunner(), null, publication);
        RawPublisherArgs rawArgs = RawPublisherArgs.builder().output(null, "ch.so.agi.demo")
                .xtfRegexSource(tempDir.resolve("incoming").toString(), ".*\\.xtf$")
                .outFormats(List.of(OutputFormat.XTF))
                .build();

        assertThrows(IllegalStateException.class,
                () -> step.publish(rawArgs, publisherEnv(), tempDir.resolve("cache")));

        assertEquals(0, publication.commitCount);
        assertEquals(1, publication.rollbackCount);
        assertTrue(publication.closed);
        assertEquals(1, logger.lifecycleMessages.size());
        assertTrue(logger.lifecycleMessages.get(0).contains("Publishing ch.so.agi.demo"));
    }

    private static PublisherStep step(String taskName, GretlLogger logger, RecordingBuilder builder,
            OpSequenceRunner runner, RecordingConnector source, RecordingConnector publication) {
        return new PublisherStep(taskName, logger, builder, runner, config ->
                config.getUrl().contains("publisher") ? publication : source);
    }

    private static PublisherEnv publisherEnv() {
        return new PublisherEnv(
                new PupDateEnv("jdbc:postgresql://db/publisher", "pub_meta", "pub_user", "publisher_pass"),
                new PubFolderEnv("sftp://host/data", "pub_user", "publisher_pass"),
                "https://geo.so.ch/json", "publication", "metadata.json", "/env/models",
                Path.of("/env/grooming.json"));
    }

    private static final class RecordingBuilder extends OpSequenceBuilder {
        private ResolvedPublisherArgs capturedArgs;
        private Connection capturedSourceConnection;
        private Connection capturedPublicationConnection;
        private final List<Operation> stepsToReturn = new ArrayList<>();

        @Override
        public List<Operation> buildSequence(ResolvedPublisherArgs publisherArgs, Connection sourceConnection,
                Connection publicationConnection, Path cacheRoot) {
            capturedArgs = publisherArgs;
            capturedSourceConnection = sourceConnection;
            capturedPublicationConnection = publicationConnection;
            return stepsToReturn;
        }
    }

    private static final class RecordingRunner extends OpSequenceRunner {
        @Override
        public void run(List<? extends Operation> steps) {
        }
    }

    private static final class FailingRunner extends OpSequenceRunner {
        @Override
        public void run(List<? extends Operation> steps) {
            throw new IllegalStateException("publication failed");
        }
    }

    private static final class RecordingConnector extends Connector {
        private final Connection connection;
        private int commitCount;
        private int rollbackCount;
        private boolean closed;

        private RecordingConnector(String url) {
            super(url);
            connection = (Connection) java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                        if ("commit".equals(method.getName())) commitCount++;
                        if ("rollback".equals(method.getName())) rollbackCount++;
                        Class<?> type = method.getReturnType();
                        if (type == boolean.class) return Boolean.FALSE;
                        if (type == byte.class || type == short.class || type == int.class || type == long.class
                                || type == float.class || type == double.class) return Integer.valueOf(0);
                        return null;
                    });
        }

        @Override public Connection connect() { return connection; }
        @Override public void close() throws SQLException { closed = true; }
    }

    private static final class RecordingLogger implements GretlLogger {
        private final List<String> lifecycleMessages = new ArrayList<>();
        private final List<String> infoMessages = new ArrayList<>();
        @Override public void info(String msg) { infoMessages.add(msg); }
        @Override public void debug(String msg) { }
        @Override public void error(String msg, Throwable thrown) { }
        @Override public void lifecycle(String msg) { lifecycleMessages.add(msg); }
    }
}
