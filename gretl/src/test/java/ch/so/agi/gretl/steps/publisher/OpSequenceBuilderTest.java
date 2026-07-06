package ch.so.agi.gretl.steps.publisher;

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
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivedFormat;
import ch.so.agi.gretl.steps.publisher.stage.mergestages.MergeStages;
import ch.so.agi.gretl.steps.publisher.stage.mergestages.MergeStagesParameters;

class OpSequenceBuilderTest {
    @TempDir
    Path tempDir;

    @Test
    void buildsMergedDbSequenceWithValidationAndDerivedFormats() throws Exception {
        Connection connection = fakeConnection();
        RecordingResolver resolver = new RecordingResolver(resolvedSelection(DataSelection.KeyType.dataset, "2401", "2402"));
        OpSequenceBuilder builder = new OpSequenceBuilder(resolver);
        Path cacheRoot = tempDir.resolve("cache");
        Path validationConfig = Files.writeString(tempDir.resolve("validation.ini"), "models=ModelA",
                StandardCharsets.UTF_8);

        List<OpSequenceStep> steps = builder.buildSequence(new RawPublisherArgs(
                "edit",
                "live",
                "dataset",
                null,
                "24.*",
                false,
                null,
                null,
                null,
                target(),
                "ch.so.agi.demo",
                validationConfig,
                List.of(DerivedFormat.GPKG),
                null), connection, cacheRoot);

        assertEquals(List.of(
                "Exporter",
                "Exporter",
                "MergeStages",
                "ValidationConfigSeeder",
                "CacheValidator",
                "Derivator",
                "Packer",
                "Writer",
                "RemoteUpdater"), operationNames(steps));
        assertSame(connection, resolver.connection);
        assertEquals("live", resolver.dbSchema);
        assertEquals("24.*", resolver.requestedSelection.getKeyRegEx());

        ExporterParameters firstExporter = assertInstanceOf(ExporterParameters.class, steps.get(0).resolveParameters());
        assertEquals(List.of("2401"), firstExporter.getSelectionToExport().getKeyValues());
        assertTrue(firstExporter.getExportDirectory().toString().contains(".source-stages"));

        MergeStagesParameters mergeParameters = assertInstanceOf(MergeStagesParameters.class, steps.get(2).resolveParameters());
        assertEquals(2, mergeParameters.getInputDirs().size());
        assertEquals(cacheRoot.toAbsolutePath().normalize(), mergeParameters.getOutputDir());
    }

    @Test
    void buildsSingleStageXtfRegexSequenceWithoutMerge() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        List<OpSequenceStep> steps = new OpSequenceBuilder().buildSequence(new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                tempDir.resolve("incoming").toString(),
                ".*\\.xtf$",
                null,
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null), fakeConnection(), cacheRoot);

        assertEquals(List.of("XtfByRegex", "Packer", "Writer", "RemoteUpdater"), operationNames(steps));
        assertTrue(steps.get(0).getOperation() instanceof XtfByRegex);
        assertTrue(operationNames(steps).stream().noneMatch("MergeStages"::equals));
    }

    @Test
    void buildsMergedXtfListSequenceAndNormalizesSuffixes() throws Exception {
        Path cacheRoot = tempDir.resolve("cache");
        List<OpSequenceStep> steps = new OpSequenceBuilder().buildSequence(new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                tempDir.resolve("incoming").toString(),
                null,
                list("north.xtf", "south.xtf"),
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null), fakeConnection(), cacheRoot);

        assertEquals(List.of("XtfCopy", "XtfCopy", "MergeStages", "Packer", "Writer", "RemoteUpdater"),
                operationNames(steps));

        XtfCopyParams firstCopy = assertInstanceOf(XtfCopyParams.class, steps.get(0).resolveParameters());
        assertEquals(List.of("north"), firstCopy.getTransferFileList().asList());
        assertEquals(XtfCopyParams.TransferFileType.XTF, firstCopy.getTransferFileType());
        assertTrue(steps.get(2).getOperation() instanceof MergeStages);
    }

    @Test
    void rejectsMixedTransferFileSuffixesInExplicitList() {
        RawPublisherArgs rawPublisherArgs = new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                tempDir.resolve("incoming").toString(),
                null,
                list("north.xtf", "south.itf"),
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new OpSequenceBuilder().buildSequence(rawPublisherArgs, fakeConnection(), tempDir.resolve("cache")));

        assertTrue(exception.getMessage().contains("must not mix .xtf and .itf"));
    }

    @Test
    void resolvesWriterParametersLazilyFromPackedArtifacts() throws Exception {
        Path cacheRoot = Files.createDirectories(tempDir.resolve("cache"));
        OpSequenceStep writerStep = new OpSequenceBuilder().buildSequence(new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                tempDir.resolve("incoming").toString(),
                ".*\\.xtf$",
                null,
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null), fakeConnection(), cacheRoot).stream()
                .filter(step -> "Writer".equals(step.getOperation().getHumanReadableName()))
                .findFirst()
                .orElseThrow();

        Files.writeString(cacheRoot.resolve("north.ch.so.agi.demo.xtf.zip"), "zip", StandardCharsets.UTF_8);
        Files.writeString(cacheRoot.resolve("south.ch.so.agi.demo.xtf.zip"), "zip", StandardCharsets.UTF_8);

        WriterParameters writerParameters = assertInstanceOf(WriterParameters.class, writerStep.resolveParameters());
        assertEquals(List.of(
                cacheRoot.resolve("north.ch.so.agi.demo.xtf.zip"),
                cacheRoot.resolve("south.ch.so.agi.demo.xtf.zip")), writerParameters.getCachedTransferFiles());
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

    private static ArrayList<String> list(String... values) {
        return new ArrayList<>(List.of(values));
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

    private static Endpoint target() {
        return new Endpoint("/tmp/publisher-target");
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
