package ch.so.agi.gretl.steps.publisher;

import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.list;
import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.publisherArgs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.steps.publisher.in.db.DataSelection;
import ch.so.agi.gretl.steps.publisher.in.db.ExporterParameters;
import ch.so.agi.gretl.steps.publisher.in.xtf.list.XtfCopyParams;
import ch.so.agi.gretl.steps.publisher.in.xtf.regex.XtfByRegex;
import ch.so.agi.gretl.steps.publisher.out.metainfo.table.WriterParameters;
import ch.so.agi.gretl.steps.publisher.out.metainfo.metafolder.MetafolderWriterParameters;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivedFormat;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivatorParameters;
import ch.so.agi.gretl.steps.publisher.stage.mergestages.MergeStages;
import ch.so.agi.gretl.steps.publisher.stage.mergestages.MergeStagesParameters;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;
import ch.so.agi.gretl.steps.publisher.stage.pack.PackerParameters;

class OpSequenceBuilderTest {
    @TempDir
    Path tempDir;

    @Test
    void buildsMergedDbSequenceWithValidationAndDerivedFormats() throws Exception {
        Connection sourceConnection = fakeConnection();
        Connection publicationConnection = fakeConnection();
        RecordingResolver resolver = new RecordingResolver(resolvedSelection(DataSelection.KeyType.dataset, "2401", "2402"));
        OpSequenceBuilder builder = new OpSequenceBuilder(resolver);
        Path cacheRoot = tempDir.resolve("cache");
        Path validationConfig = Files.writeString(tempDir.resolve("validation.ini"), "models=ModelA",
                StandardCharsets.UTF_8);

        List<OpSequenceStep> steps = builder.buildSequence(publisherArgs()
                .dbRegexSource("edit", "live", RawPublisherArgs.IliIdentType.dataset, false, "24.*")
                .validationConfig(validationConfig.toString())
                .outFormats(List.of(OutputFormat.XTF, OutputFormat.GPKG))
                .build(), sourceConnection, publicationConnection, cacheRoot);

        assertEquals(List.of(
                "Exporter",
                "Exporter",
                "MergeStages",
                "ValidationConfigSeeder",
                "CacheValidator",
                "Derivator",
                "MetafolderWriter",
                "Packer",
                "RemoteUpdater",
                "Writer"), operationNames(steps));
        assertSame(sourceConnection, resolver.connection);
        assertEquals("live", resolver.dbSchema);
        assertEquals("24.*", resolver.requestedSelection.getKeyRegEx());

        ExporterParameters firstExporter = assertInstanceOf(ExporterParameters.class, steps.get(0).resolveParameters());
        assertEquals(List.of("2401"), firstExporter.getSelectionToExport().getKeyValues());
        assertTrue(firstExporter.getExportDirectory().toString().contains(".source-stages"));
        assertSame(sourceConnection, firstExporter.getConnection());

        DerivatorParameters derivatorParameters = assertInstanceOf(DerivatorParameters.class,
                steps.get(5).resolveParameters());
        assertEquals(List.of(DerivedFormat.GPKG), derivatorParameters.getRequestedFormats());
        PackerParameters packerParameters = assertInstanceOf(PackerParameters.class,
                steps.get(7).resolveParameters());
        assertEquals(List.of(OutputFormat.XTF, OutputFormat.GPKG), packerParameters.getOutputFormats());

        MergeStagesParameters mergeParameters = assertInstanceOf(MergeStagesParameters.class, steps.get(2).resolveParameters());
        assertEquals(2, mergeParameters.getInputDirs().size());
        assertEquals(cacheRoot.toAbsolutePath().normalize(), mergeParameters.getOutputDir());

        OpSequenceStep writerStep = steps.stream()
                .filter(step -> "Writer".equals(step.getOperation().getHumanReadableName()))
                .findFirst().orElseThrow();
        Files.createDirectories(cacheRoot);
        Files.writeString(cacheRoot.resolve("2401.xtf.zip"), "zip", StandardCharsets.UTF_8);
        WriterParameters writerParameters = assertInstanceOf(WriterParameters.class, writerStep.resolveParameters());
        assertSame(publicationConnection, writerParameters.getConnection());
    }

    @Test
    void buildsSingleStageXtfRegexSequenceWithoutMerge() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        Path incoming = Files.createDirectories(tempDir.resolve("incoming"));
        Files.writeString(incoming.resolve("north.xtf"), "transfer", StandardCharsets.UTF_8);
        List<OpSequenceStep> steps = new OpSequenceBuilder().buildSequence(publisherArgs()
                .xtfRegexSource(incoming.toString(), ".*\\.xtf$")
                .build(), fakeConnection(), cacheRoot);

        assertEquals(List.of("XtfByRegex", "MetafolderWriter", "Packer", "RemoteUpdater", "Writer"), operationNames(steps));
        assertTrue(steps.get(0).getOperation() instanceof XtfByRegex);
        assertTrue(operationNames(steps).stream().noneMatch("MergeStages"::equals));
    }

    @Test
    void buildsMergedXtfListSequenceAndNormalizesSuffixes() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        List<OpSequenceStep> steps = new OpSequenceBuilder().buildSequence(publisherArgs()
                .xtfListSource(tempDir.resolve("incoming").toString(), list("north.xtf", "south.xtf"))
                .build(), fakeConnection(), cacheRoot);

        assertEquals(List.of("XtfCopy", "XtfCopy", "MergeStages", "MetafolderWriter", "Packer", "RemoteUpdater", "Writer"),
                operationNames(steps));

        XtfCopyParams firstCopy = assertInstanceOf(XtfCopyParams.class, steps.get(0).resolveParameters());
        assertEquals(List.of("north"), firstCopy.getTransferFileList().asList());
        assertEquals(XtfCopyParams.TransferFileType.XTF, firstCopy.getTransferFileType());
        assertTrue(steps.get(2).getOperation() instanceof MergeStages);
    }

    @Test
    void omitsWriterForLocalOnlyPublication() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        List<OpSequenceStep> steps = new OpSequenceBuilder().buildSequence(publisherArgs()
                .xtfListSource(tempDir.resolve("incoming").toString(), list("north.xtf"))
                .output(new ch.so.agi.gretl.api.Endpoint(tempDir.resolve("local-publication").toString()),
                        "ch.so.agi.demo")
                .writeMetadata(false)
                .build(), null, null, null, false, cacheRoot);

        assertEquals(List.of("XtfCopy", "MetafolderWriter", "Packer", "RemoteUpdater"), operationNames(steps));
        MetafolderWriterParameters metafolderParameters = assertInstanceOf(MetafolderWriterParameters.class,
                steps.get(1).resolveParameters());
        assertTrue(!metafolderParameters.shouldWriteJson());
    }

    @Test
    void rejectsMixedTransferFileSuffixesInExplicitList() {
        RawPublisherArgs rawPublisherArgs = publisherArgs()
                .xtfListSource(tempDir.resolve("incoming").toString(), list("north.xtf", "south.itf"))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new OpSequenceBuilder().buildSequence(rawPublisherArgs, fakeConnection(), tempDir.resolve("cache")));

        assertTrue(exception.getMessage().contains("must not mix .xtf and .itf"));
    }

    @Test
    void resolvesWriterParametersFromPublishedArtifactsAndResolvedParts() throws Exception {
        Path cacheRoot = Files.createDirectories(tempDir.resolve("cache"));
        Path incoming = Files.createDirectories(tempDir.resolve("incoming"));
        Files.writeString(incoming.resolve("north.xtf"), "transfer", StandardCharsets.UTF_8);
        Files.writeString(incoming.resolve("south.xtf"), "transfer", StandardCharsets.UTF_8);
        OpSequenceStep writerStep = new OpSequenceBuilder().buildSequence(publisherArgs()
                .xtfRegexSource(incoming.toString(), ".*\\.xtf$")
                .build(), fakeConnection(), cacheRoot).stream()
                .filter(step -> "Writer".equals(step.getOperation().getHumanReadableName()))
                .findFirst()
                .orElseThrow();

        Files.writeString(cacheRoot.resolve("north.ch.so.agi.demo.xtf.zip"), "zip", StandardCharsets.UTF_8);
        Files.writeString(cacheRoot.resolve("south.ch.so.agi.demo.xtf.zip"), "zip", StandardCharsets.UTF_8);

        WriterParameters writerParameters = assertInstanceOf(WriterParameters.class, writerStep.resolveParameters());
        assertEquals(List.of("north", "south"), writerParameters.getPartIdentifiers());
        assertEquals(List.of("xtf"), writerParameters.getExportedFormats());
    }

    private static List<String> operationNames(List<OpSequenceStep> steps) {
        List<String> names = new ArrayList<>();
        for (OpSequenceStep step : steps) {
            names.add(step.getOperation().getHumanReadableName());
        }
        return names;
    }

    private static DataSelection resolvedSelection(DataSelection.KeyType keyType, String... keyValues) {
        DataSelection selection = new DataSelection();
        selection.setKeyType(keyType);
        selection.setKeyValues(List.of(keyValues));
        return selection;
    }

    private static Connection fakeConnection() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class},
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

    private static final class RecordingResolver implements OpSequenceBuilder.DbSelectionResolver {
        private final DataSelection resolvedSelection;
        private Connection connection;
        private String dbSchema;
        private DataSelection requestedSelection;

        private RecordingResolver(DataSelection resolvedSelection) {
            this.resolvedSelection = resolvedSelection;
        }

        @Override
        public DataSelection resolve(Connection publicationDbConnection, String dbSchema, DataSelection requestedSelection) {
            this.connection = publicationDbConnection;
            this.dbSchema = dbSchema;
            this.requestedSelection = requestedSelection;
            return resolvedSelection;
        }
    }
}
