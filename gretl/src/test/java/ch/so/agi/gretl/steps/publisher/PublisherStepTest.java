package ch.so.agi.gretl.steps.publisher;

import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.publisherArgs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.steps.publisher.util.env.PubFolderEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PupDateEnv;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;

class PublisherStepTest {
    @TempDir
    Path tempDir;

    @Test
    void defaultsNullDateAndUsesEnvValuesWhenNoOverridesAreSet() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        RecordingBuilder builder = new RecordingBuilder();
        RecordingRunner runner = new RecordingRunner();
        PublisherStep step = new PublisherStep("publishDemo", logger, builder, runner);
        RawPublisherArgs rawArgs = RawPublisherArgs.builder().output(null, "ch.so.agi.demo")
                .xtfRegexSource(tempDir.resolve("incoming").toString(), ".*\\.xtf$")
                .outFormats(List.of(OutputFormat.SHP))
                .build();
        PublisherEnv publisherEnv = publisherEnv();
        Connection sourceConnection = fakeConnection();
        Connection publicationConnection = fakeConnection();
        Path cacheRoot = tempDir.resolve("cache");

        long before = System.currentTimeMillis();
        step.publish(rawArgs, publisherEnv, sourceConnection, publicationConnection, cacheRoot);
        long after = System.currentTimeMillis();

        assertNotNull(builder.capturedArgs);
        assertTrue(builder.capturedArgs.getPublicationTimestamp().getTime() >= before);
        assertTrue(builder.capturedArgs.getPublicationTimestamp().getTime() <= after);
        assertEquals("sftp://host/data", builder.capturedArgs.getOutFolderPath().getUrl());
        assertEquals("/env/grooming.json", builder.capturedArgs.getOutGroomingConfigFilePath());
        assertEquals("/env/models", builder.capturedArgs.getCustomModelDir());
        assertEquals(List.of(OutputFormat.SHP), builder.capturedArgs.getOutFormats());
        assertSame(sourceConnection, builder.capturedSourceConnection);
        assertSame(publicationConnection, builder.capturedPublicationConnection);
        assertEquals(cacheRoot, builder.capturedCacheRoot);
        assertSame(builder.stepsToReturn, runner.capturedSteps);
        assertEquals(List.of("publishDemo: Start PublisherStep", "publishDemo: End PublisherStep (successful)"),
                logger.lifecycleMessages);
        assertTrue(logger.infoMessages.isEmpty());
        assertTrue(logger.debugMessages.stream().anyMatch(message -> message.contains("Publisher mode: xtfFilesRegex")));
        assertTrue(logger.debugMessages.stream().anyMatch(message -> message.contains("xtfFilename_Regex=.*\\.xtf$")));
        assertTrue(logger.debugMessages.stream().noneMatch(message -> message.contains("publisher_pass")));
        assertTrue(logger.debugMessages.stream().noneMatch(message -> message.contains("pub_user")));
    }

    @Test
    void usesAutomaticTimestampAndLogsEachOverride() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        RecordingBuilder builder = new RecordingBuilder();
        RecordingRunner runner = new RecordingRunner();
        PublisherStep step = new PublisherStep(null, logger, builder, runner);
        RawPublisherArgs rawArgs = publisherArgs()
                .dbValuesSource("edit-db", "live", RawPublisherArgs.IliIdentType.dataset, true, List.of("2401"))
                .output(new Endpoint(tempDir.resolve("local-out").toString()), "ch.so.agi.demo")
                .groomingConfig(tempDir.resolve("custom-grooming.json").toString())
                .customModelDir("/custom/models")
                .build();

        long before = System.currentTimeMillis();
        step.publish(rawArgs, publisherEnv(), fakeConnection(), fakeConnection(), tempDir.resolve("cache"));
        long after = System.currentTimeMillis();

        assertEquals(tempDir.resolve("local-out").toString(), builder.capturedArgs.getOutFolderPath().getUrl());
        assertEquals(tempDir.resolve("custom-grooming.json").toString(),
                builder.capturedArgs.getOutGroomingConfigFilePath());
        assertEquals("/custom/models", builder.capturedArgs.getCustomModelDir());
        assertTrue(builder.capturedArgs.getPublicationTimestamp().getTime() >= before);
        assertTrue(builder.capturedArgs.getPublicationTimestamp().getTime() <= after);
        assertTrue(logger.infoMessages.stream().anyMatch(message -> message.contains("pubFolder.path overridden")));
        assertTrue(logger.infoMessages.stream().anyMatch(message -> message.contains("groomingConfigFilePath overridden")));
        assertTrue(logger.infoMessages.stream().anyMatch(message -> message.contains("modeldir overridden")));
        assertTrue(logger.debugMessages.stream().anyMatch(message -> message.contains("Publisher mode: dbIdentvaluesList")));
        assertTrue(logger.debugMessages.stream().anyMatch(message -> message.contains("dbIliIdent_Values=[2401]")));
    }

    private static PublisherEnv publisherEnv() {
        return new PublisherEnv(
                new PupDateEnv("jdbc:postgresql://db/publisher", "pub_meta", "pub_user", "publisher_pass"),
                new PubFolderEnv("sftp://host/data", "pub_user", "publisher_pass"),
                "https://geo.so.ch/json", "publication", "metadata.json",
                "/env/models",
                Path.of("/env/grooming.json"));
    }

    private static Connection fakeConnection() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class },
                (proxy, method, args) -> {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) {
                        return Boolean.FALSE;
                    }
                    if (returnType == byte.class || returnType == short.class || returnType == int.class
                            || returnType == long.class || returnType == float.class || returnType == double.class) {
                        return Integer.valueOf(0);
                    }
                    return null;
                });
    }

    private static final class RecordingBuilder extends OpSequenceBuilder {
        private RawPublisherArgs capturedArgs;
        private Connection capturedSourceConnection;
        private Connection capturedPublicationConnection;
        private Path capturedCacheRoot;
        private final List<OpSequenceStep> stepsToReturn = new ArrayList<>();

        @Override
        public List<OpSequenceStep> buildSequence(RawPublisherArgs rawPublisherArgs, Connection sourceDbConnection,
                Connection publicationDbConnection, String metadataSchema, boolean writeMetadata, String jsonmetaAddress,
                String jsonmetaBucket, String jsonmetaFileName, Path cacheRoot) {
            this.capturedArgs = rawPublisherArgs;
            this.capturedSourceConnection = sourceDbConnection;
            this.capturedPublicationConnection = publicationDbConnection;
            this.capturedCacheRoot = cacheRoot;
            return stepsToReturn;
        }
    }

    private static final class RecordingRunner extends OpSequenceRunner {
        private List<OpSequenceStep> capturedSteps;

        @Override
        public void run(List<OpSequenceStep> steps) {
            this.capturedSteps = steps;
        }
    }

    private static final class RecordingLogger implements GretlLogger {
        private final List<String> infoMessages = new ArrayList<>();
        private final List<String> debugMessages = new ArrayList<>();
        private final List<String> lifecycleMessages = new ArrayList<>();

        @Override
        public void info(String msg) {
            infoMessages.add(msg);
        }

        @Override
        public void debug(String msg) {
            debugMessages.add(msg);
        }

        @Override
        public void error(String msg, Throwable thrown) {
        }

        @Override
        public void lifecycle(String msg) {
            lifecycleMessages.add(msg);
        }
    }
}
