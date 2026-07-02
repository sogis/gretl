package ch.so.agi.gretl.steps.publisher.stage.pack;

import java.nio.file.Path;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleDirectoryParameters;

public final class PackerParameters extends AbstractSingleDirectoryParameters {
    private final String dataIdent;

    private PackerParameters(Path cacheDir, String dataIdent) {
        super(cacheDir);
        this.dataIdent = requireText(dataIdent, "dataIdent");
    }

    public static PackerParameters of(Path cacheDir, String dataIdent) {
        return new PackerParameters(cacheDir, dataIdent);
    }

    public Path getCacheDir() {
        return getDirectory();
    }

    public String getDataIdent() {
        return dataIdent;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
