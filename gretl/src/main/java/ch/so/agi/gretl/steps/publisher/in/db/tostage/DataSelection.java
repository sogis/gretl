package ch.so.agi.gretl.steps.publisher.in.db.tostage;

import java.util.List;

/**
 * Describes, which part of the data in the db will be exported as transfer file.
 */
public class DataSelection {
    public enum KeyType {
        model,
        topic,
        basket,
        dataset
    }

    /**
     * List of keys describing, which data parts shouldd be exported.
     */
    private List<String> keyValues;
    private KeyType keyType;
    private String keyRegEx;

    public void validateRequest() {
        validateKeyType();
        if (!hasKeyValues() && !hasKeyRegEx()) {
            throw new IllegalArgumentException("keyValues or keyRegEx must be set");
        }
    }

    public void validateResolved() {
        validateKeyType();
        if (!hasKeyValues()) {
            throw new IllegalArgumentException("resolved keyValues must be set");
        }
        if (hasKeyRegEx()) {
            throw new IllegalArgumentException("resolved keyRegEx must be null");
        }
    }

    public boolean hasKeyValues() {
        return keyValues != null && !keyValues.isEmpty();
    }

    public boolean hasKeyRegEx() {
        return keyRegEx != null && !keyRegEx.isEmpty();
    }

    public String getKeyRegEx() {
        return keyRegEx;
    }

    public void setKeyRegEx(String keyRegEx) {
        this.keyRegEx = keyRegEx;
    }

    public List<String> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(List<String> keyValues) {
        this.keyValues = keyValues;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    private void validateKeyType() {
        if (keyType == null) {
            throw new IllegalArgumentException("keyType must be set");
        }
    }
}
