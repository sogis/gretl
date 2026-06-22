package ch.so.agi.gretl.steps.publisher.in.db.tostage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.ehi.ili2db.gui.Config;
import ch.interlis.ilirepository.IliFiles;

class DbDataSelectorTest {
    @Test
    void validatesRegionSelection() {
        DataSelector selector = new DataSelector();

        assertEquals(DbDataSelectionOld.SelectionMode.REGIONS,
                selector.validateSelectionMode(null, null, "[0-9]+", null));
        assertEquals(DbDataSelectionOld.SelectionMode.REGIONS,
                selector.validateSelectionMode(null, null, null, Collections.singletonList("2501")));
    }

    @Test
    void validatesDatasetAndModelSelection() {
        DataSelector selector = new DataSelector();

        assertEquals(DbDataSelectionOld.SelectionMode.DATASET, selector.validateSelectionMode("av", null, null, null));
        assertEquals(DbDataSelectionOld.SelectionMode.MODELS,
                selector.validateSelectionMode(null, "DM01AVCH24LV95D", null, null));
    }

    @Test
    void rejectsMissingSelection() {
        DataSelector selector = new DataSelector();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> selector.validateSelectionMode(null, null, null, null));

        assertEquals("regionRegEx==null && regionsToPublish==null && datasetName==null && modelsToPublish==null",
                exception.getMessage());
    }

    @Test
    void filtersRegionsWithoutChangingInputList() {
        DataSelector selector = new DataSelector();
        List<String> regions = new ArrayList<String>(Arrays.asList("2501", "2502"));

        List<String> filteredRegions = selector.filterRegions(regions, Collections.singletonList("2501"));

        assertEquals(Collections.singletonList("2501"), filteredRegions);
        assertEquals(Arrays.asList("2501", "2502"), regions);
    }

    @Test
    void selectsRegionsFromDbDatasets() throws Exception {
        TestSelector selector = new TestSelector();

        DbDataSelectionOld selection = selector.select(null, new Config(), null, null, "25.*",
                Collections.singletonList("2501"));

        assertTrue(selection.isRegionSelection());
        assertEquals(Collections.singletonList("2501"), selection.getNames());
        assertEquals("2501", selector.checkedDatasetName);
    }

    @Test
    void selectsDatasetNames() throws Exception {
        TestSelector selector = new TestSelector();

        DbDataSelectionOld selection = selector.select(null, new Config(), "av;simple", null, null, null);

        assertTrue(selection.isDatasetSelection());
        assertEquals(Arrays.asList("av", "simple"), selection.getNames());
        assertEquals("av;simple", selection.getDatasetName());
        assertEquals("av;simple", selection.getFileName());
        assertEquals(Arrays.asList("av", "simple"), selector.checkedDatasetNames);
    }

    @Test
    void selectsModelNames() throws Exception {
        TestSelector selector = new TestSelector();

        DbDataSelectionOld selection = selector.select(null, new Config(), null, "ModelA;ModelB", null, null);

        assertTrue(selection.isModelsSelection());
        assertEquals(Arrays.asList("ModelA", "ModelB"), selection.getNames());
        assertEquals("ModelA;ModelB", selection.getModelsToPublish());
        assertEquals("ModelA", selection.getFileName());
        assertEquals(Arrays.asList("ModelA", "ModelB"), selector.checkedModelNames);
    }

    @Test
    void detectsModelNamesInIliFiles() {
        DataSelector selector = new DataSelector();
        IliFiles iliFiles = new IliFiles();
        ch.interlis.ili2c.modelscan.IliFile iliFile = new ch.interlis.ili2c.modelscan.IliFile();
        ch.interlis.ili2c.modelscan.IliModel iliModel = new ch.interlis.ili2c.modelscan.IliModel();
        iliModel.setName("ModelA");
        iliFile.addModel(iliModel);
        iliFiles.addFile(iliFile);

        assertTrue(selector.modelExists("ModelA", iliFiles));
        assertFalse(selector.modelExists("ModelB", iliFiles));
    }

    @Test
    void rejectsModelsToPublishForBasketAwareSchemas() {
        DataSelector selector = new DataSelector();
        Config config = new Config();
        config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> selector.assertModelsToPublishUsesSimpleModels(config, "ModelA"));

        assertEquals("modelsToPublish <ModelA> can only be used with simple models", exception.getMessage());
    }

    private static final class TestSelector extends DataSelector {
        private String checkedDatasetName;
        private List<String> checkedDatasetNames;
        private List<String> checkedModelNames;

        @Override
        public List<String> getRegionsFromDb(Connection conn, Config config, String regionRegEx) {
            return Arrays.asList("2501", "2502", "foo");
        }

        @Override
        public void assertDatasetExists(Connection conn, Config config, String datasetName) {
            checkedDatasetName = datasetName;
        }

        @Override
        public void assertDatasetsExist(Connection conn, Config config, List<String> datasetNames) {
            checkedDatasetNames = new ArrayList<String>(datasetNames);
        }

        @Override
        public void assertModelsExist(Connection conn, Config config, List<String> modelNames) {
            checkedModelNames = new ArrayList<String>(modelNames);
        }
    }
}
