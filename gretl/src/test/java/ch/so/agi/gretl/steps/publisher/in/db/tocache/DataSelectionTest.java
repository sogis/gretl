package ch.so.agi.gretl.steps.publisher.in.db.tocache;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class DataSelectionTest {
    @Test
    void requestValidationRejectsMissingKeyType() {
        DataSelection selection = new DataSelection();
        selection.setKeyRegEx(".*");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> selection.validateRequest());

        assertEquals("keyType must be set", exception.getMessage());
    }

    @Test
    void requestValidationRequiresKeyValuesOrRegex() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> selection.validateRequest());

        assertEquals("keyValues or keyRegEx must be set", exception.getMessage());
    }

    @Test
    void requestValidationAcceptsRegex() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.dataset);
        selection.setKeyRegEx("25.*");

        assertDoesNotThrow(() -> selection.validateRequest());
    }

    @Test
    void requestValidationAcceptsKeyValues() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.model);
        selection.setKeyValues(Collections.singletonList("ModelA"));

        assertDoesNotThrow(() -> selection.validateRequest());
    }

    @Test
    void resolvedValidationRejectsRegex() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.basket);
        selection.setKeyValues(Collections.singletonList("basket-a"));
        selection.setKeyRegEx("basket.*");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> selection.validateResolved());

        assertEquals("resolved keyRegEx must be null", exception.getMessage());
    }

    @Test
    void resolvedValidationRequiresKeyValues() {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.topic);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> selection.validateResolved());

        assertEquals("resolved keyValues must be set", exception.getMessage());
    }
}
