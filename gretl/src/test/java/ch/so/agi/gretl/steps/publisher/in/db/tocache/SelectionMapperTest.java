package ch.so.agi.gretl.steps.publisher.in.db.tocache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class SelectionMapperTest {
    @Test
    void mapsDatasetRegexToConcreteValuesWithoutRegex() throws Exception {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);
        selection.setKeyRegEx("25.*");

        DataSelection mappedSelection = new SelectionMapper(selection, new TestMetadataReader()).map();

        assertEquals(DataSelection.KeyType.dataset, mappedSelection.getKeyType());
        assertEquals(Arrays.asList("2501", "2502"), mappedSelection.getKeyValues());
        assertFalse(mappedSelection.hasKeyRegEx());
    }

    @Test
    void mapsExplicitDatasetValues() throws Exception {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);
        selection.setKeyValues(Collections.singletonList("av"));

        DataSelection mappedSelection = new SelectionMapper(selection, new TestMetadataReader()).map();

        assertEquals(Collections.singletonList("av"), mappedSelection.getKeyValues());
    }

    @Test
    void rejectsMissingExplicitValues() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);
        selection.setKeyValues(Arrays.asList("av", "missing"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new SelectionMapper(selection, new TestMetadataReader()).map());

        assertEquals("selection for keyType <dataset> references missing keys [missing]", exception.getMessage());
    }

    @Test
    void mapsIntersectionOfRegexAndExplicitValues() throws Exception {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);
        selection.setKeyRegEx("25.*");
        selection.setKeyValues(Arrays.asList("2501", "2502"));

        DataSelection mappedSelection = new SelectionMapper(selection, new TestMetadataReader()).map();

        assertEquals(Arrays.asList("2501", "2502"), mappedSelection.getKeyValues());
    }

    @Test
    void rejectsEmptyIntersectionOfRegexAndExplicitValues() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);
        selection.setKeyRegEx("25.*");
        selection.setKeyValues(Collections.singletonList("av"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new SelectionMapper(selection, new TestMetadataReader()).map());

        assertEquals("selection for keyType <dataset> did not match any keys", exception.getMessage());
    }

    @Test
    void rejectsEmptyMappedSelection() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);
        selection.setKeyRegEx("99.*");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new SelectionMapper(selection, new TestMetadataReader()).map());

        assertEquals("selection for keyType <dataset> did not match any keys", exception.getMessage());
    }

    @Test
    void mapsModelValues() throws Exception {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.model);
        selection.setKeyValues(Collections.singletonList("ModelB"));

        DataSelection mappedSelection = new SelectionMapper(selection, new TestMetadataReader()).map();

        assertEquals(Collections.singletonList("ModelB"), mappedSelection.getKeyValues());
    }

    @Test
    void mapsTopicRegex() throws Exception {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.topic);
        selection.setKeyRegEx("ModelA\\..*");

        DataSelection mappedSelection = new SelectionMapper(selection, new TestMetadataReader()).map();

        assertEquals(Arrays.asList("ModelA.TopicA", "ModelA.TopicB"), mappedSelection.getKeyValues());
    }

    @Test
    void mapsBasketValues() throws Exception {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.basket);
        selection.setKeyValues(Collections.singletonList("basket-b"));

        DataSelection mappedSelection = new SelectionMapper(selection, new TestMetadataReader()).map();

        assertEquals(Collections.singletonList("basket-b"), mappedSelection.getKeyValues());
    }

    @Test
    void usesMetadataMethodMatchingKeyType() throws Exception {
        TestMetadataReader metadataReader = new TestMetadataReader();
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.topic);
        selection.setKeyValues(Collections.singletonList("ModelA.TopicA"));

        new SelectionMapper(selection, metadataReader).map();

        assertEquals(0, metadataReader.datasetCalls);
        assertEquals(0, metadataReader.modelCalls);
        assertEquals(1, metadataReader.topicCalls);
        assertEquals(0, metadataReader.basketCalls);
    }

    private static final class TestMetadataReader implements SelectionMetadataReader {
        private int datasetCalls;
        private int modelCalls;
        private int topicCalls;
        private int basketCalls;

        @Override
        public List<String> getDatasets() {
            datasetCalls++;
            return Arrays.asList("2501", "2502", "av");
        }

        @Override
        public List<String> getModels() {
            modelCalls++;
            return Arrays.asList("ModelA", "ModelB");
        }

        @Override
        public List<String> getTopics() {
            topicCalls++;
            return Arrays.asList("ModelA.TopicA", "ModelA.TopicB", "ModelB.TopicA");
        }

        @Override
        public List<String> getBaskets() {
            basketCalls++;
            return Arrays.asList("basket-a", "basket-b");
        }
    }
}
