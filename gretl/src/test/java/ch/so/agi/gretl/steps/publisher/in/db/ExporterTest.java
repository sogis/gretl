package ch.so.agi.gretl.steps.publisher.in.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.ehi.ili2db.gui.Config;

class ExporterTest {
    @TempDir
    private Path tempDir;

    @Test
    void rejectsUnresolvedSelection() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);
        selection.setKeyValues(Collections.singletonList("2501"));
        selection.setKeyRegEx("25.*");
        ExporterParameters params = ExporterParameters.of(selection, connection(), "schema", true, tempDir);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new TestExporter().export(params));

        assertEquals("resolved keyRegEx must be null", exception.getMessage());
    }

    @Test
    void rejectsNonDirectoryExportPath() throws Exception {
        Path exportFile = Files.createFile(tempDir.resolve("export.xtf"));
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.dataset, "2501"), connection(),
                "schema", true, exportFile);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new TestExporter().export(params));

        assertEquals("exportDirectory <" + exportFile + "> must be an existing directory", exception.getMessage());
    }

    @Test
    void createsMissingExportDirectory() throws Exception {
        Path exportDirectory = tempDir.resolve("raw").resolve("xtf");
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.dataset, "2501"), connection(),
                "schema", true, exportDirectory);

        new TestExporter().export(params);

        assertTrue(Files.isDirectory(exportDirectory));
    }

    @Test
    void exportsMergedSelectionToGenericXtfFile() throws Exception {
        TestExporter exporter = new TestExporter();
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.dataset, "2501", "2502"),
                connection(), "schema", true, tempDir);

        int objectCount = exporter.export(params);

        assertEquals(6, objectCount);
        assertEquals(Collections.singletonList(tempDir.resolve("export.xtf")), exporter.exportFiles);
        assertEquals("2501;2502", exporter.configs.get(0).getDatasetName());
    }

    @Test
    void exportsMergedSelectionToGenericItfFile() throws Exception {
        TestExporter exporter = new TestExporter();
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.dataset, "2501"), connection(),
                "schema", true, tempDir);
        exporter.itfTransferFile = true;

        exporter.execute(params);

        assertEquals(Collections.singletonList(tempDir.resolve("export.itf")), exporter.exportFiles);
    }

    @Test
    void exportsSplitSelectionToOneFilePerKeyValue() throws Exception {
        TestExporter exporter = new TestExporter();
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.dataset, "2501", "2502"),
                connection(), "schema", false, tempDir);

        int objectCount = exporter.export(params);

        assertEquals(12, objectCount);
        assertEquals(Arrays.asList(tempDir.resolve("2501.xtf"), tempDir.resolve("2502.xtf")), exporter.exportFiles);
        assertEquals("2501", exporter.configs.get(0).getDatasetName());
        assertEquals("2502", exporter.configs.get(1).getDatasetName());
    }

    @Test
    void sanitizesSplitExportFileNames() throws Exception {
        TestExporter exporter = new TestExporter();
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.topic, "ModelA.Topic A"),
                connection(), "schema", false, tempDir);

        exporter.execute(params);

        assertEquals(Collections.singletonList(tempDir.resolve("ModelA.Topic_A.xtf")), exporter.exportFiles);
    }

    @Test
    void appliesModelSelectionToModelsConfig() throws Exception {
        TestExporter exporter = new TestExporter();
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.model, "ModelA", "ModelB"),
                connection(), "schema", true, tempDir);

        exporter.execute(params);

        assertEquals("ModelA;ModelB", exporter.configs.get(0).getModels());
    }

    @Test
    void appliesTopicSelectionToTopicsConfig() throws Exception {
        TestExporter exporter = new TestExporter();
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.topic, "ModelA.TopicA"),
                connection(), "schema", true, tempDir);

        exporter.execute(params);

        assertEquals("ModelA.TopicA", exporter.configs.get(0).getTopics());
    }

    @Test
    void appliesBasketSelectionToBasketsConfig() throws Exception {
        TestExporter exporter = new TestExporter();
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.basket, "basket-a"),
                connection(), "schema", true, tempDir);

        exporter.execute(params);

        assertEquals("basket-a", exporter.configs.get(0).getBaskets());
    }

    @Test
    void rejectsModelSelectionForBasketAwareSchema() {
        TestExporter exporter = new TestExporter();
        ExporterParameters params = ExporterParameters.of(selection(DataSelection.KeyType.model, "ModelA"), connection(),
                "schema", true, tempDir);
        exporter.basketHandling = Config.BASKET_HANDLING_READWRITE;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> exporter.export(params));

        assertEquals("models can only be used with simple models", exception.getMessage());
        assertEquals(0, exporter.runCount);
    }

    private DataSelection selection(DataSelection.KeyType keyType, String... keyValues) {
        DataSelection selection = new DataSelection();
        selection.setKeyType(keyType);
        selection.setKeyValues(Arrays.asList(keyValues));
        return selection;
    }

    private Connection connection() {
        try {
            return java.sql.DriverManager.getConnection("jdbc:derby:memory:publisher-exporter-test;create=true");
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class TestExporter extends Exporter {
        private final List<Config> configs = new ArrayList<Config>();
        private final List<Path> exportFiles = new ArrayList<Path>();
        private boolean itfTransferFile;
        private String basketHandling;
        private int runCount;

        @Override
        Config createConfig(ExporterParameters operationParameters) {
            Config config = new Config();
            config.setDbschema("schema");
            configs.add(config);
            return config;
        }

        @Override
        boolean isItfTransferFile(ExporterParameters operationParameters) {
            return itfTransferFile;
        }

        @Override
        void readSettingsFromDb(ExporterParameters operationParameters, Config config) {
            config.setBasketHandling(basketHandling);
        }

        @Override
        void runIli2db(Config config, Path workDirectory) {
            runCount++;
        }

        @Override
        int countExportedObjects(Path exportFile, boolean itfTransferFile) {
            exportFiles.add(exportFile);
            return itfTransferFile ? 4 : 6;
        }
    }
}
