package ch.so.agi.gretl.steps.publisher.in.db.tostage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Maps the given dataselection to the data state of the database and
 * returns a resolved dataselection with "state-matching" keys.
 */
public class SelectionMapper {
    private final DataSelection selectionToBeMapped;
    private final SelectionMetadataReader metadataReader;

    public SelectionMapper(DataSelection selectionToBeMapped, Connection conn, String dbSchema) {
        this(selectionToBeMapped, new Ili2dbSelectionMetadataReader(conn, dbSchema));
    }

    SelectionMapper(DataSelection selectionToBeMapped, SelectionMetadataReader metadataReader) {
        this.selectionToBeMapped = selectionToBeMapped;
        this.metadataReader = metadataReader;
    }

    public DataSelection map() throws Exception {
        if (selectionToBeMapped == null) {
            throw new IllegalArgumentException("selectionToBeMapped must be set");
        }
        if (metadataReader == null) {
            throw new IllegalArgumentException("metadataReader must be set");
        }

        selectionToBeMapped.validateRequest();

        List<String> availableKeys = getAvailableKeys(selectionToBeMapped.getKeyType());
        validateExplicitValues(availableKeys, selectionToBeMapped.getKeyValues());
        List<String> mappedKeys = filterByRegex(availableKeys, selectionToBeMapped.getKeyRegEx());
        mappedKeys = filterByExplicitValues(mappedKeys, selectionToBeMapped.getKeyValues());

        if (mappedKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "selection for keyType <" + selectionToBeMapped.getKeyType() + "> did not match any keys");
        }

        DataSelection mappedSelection = new DataSelection();
        mappedSelection.setKeyType(selectionToBeMapped.getKeyType());
        mappedSelection.setKeyValues(mappedKeys);
        mappedSelection.validateResolved();
        return mappedSelection;
    }

    List<String> filterByRegex(List<String> keys, String keyRegEx) {
        if (keyRegEx == null || keyRegEx.isEmpty()) {
            return new ArrayList<String>(keys);
        }

        List<String> filteredKeys = new ArrayList<String>();
        for (String key : keys) {
            if (key.matches(keyRegEx)) {
                filteredKeys.add(key);
            }
        }
        return filteredKeys;
    }

    List<String> filterByExplicitValues(List<String> keys, List<String> keyValues) {
        if (keyValues == null || keyValues.isEmpty()) {
            return new ArrayList<String>(keys);
        }

        Set<String> requestedKeys = new HashSet<String>(keyValues);
        List<String> filteredKeys = new ArrayList<String>();
        for (String key : keys) {
            if (requestedKeys.contains(key)) {
                filteredKeys.add(key);
            }
        }
        return filteredKeys;
    }

    private void validateExplicitValues(List<String> keys, List<String> keyValues) {
        if (keyValues == null || keyValues.isEmpty()) {
            return;
        }

        Set<String> availableKeys = new HashSet<String>(keys);
        List<String> missingKeys = new ArrayList<String>();
        for (String keyValue : keyValues) {
            if (!availableKeys.contains(keyValue)) {
                missingKeys.add(keyValue);
            }
        }
        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "selection for keyType <" + selectionToBeMapped.getKeyType() + "> references missing keys "
                            + missingKeys);
        }
    }

    private List<String> getAvailableKeys(DataSelection.KeyType keyType) throws Exception {
        switch (keyType) {
        case dataset:
            return metadataReader.getDatasets();
        case model:
            return metadataReader.getModels();
        case topic:
            return metadataReader.getTopics();
        case basket:
            return metadataReader.getBaskets();
        default:
            throw new IllegalArgumentException("unsupported keyType <" + keyType + ">");
        }
    }

}
