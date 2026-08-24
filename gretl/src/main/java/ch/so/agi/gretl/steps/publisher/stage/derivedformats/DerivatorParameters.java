package ch.so.agi.gretl.steps.publisher.stage.derivedformats;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleDirectoryParameters;

public final class DerivatorParameters extends AbstractSingleDirectoryParameters {
    private final List<DerivedFormat> requestedFormats;
    private final String customModelDir;

    private DerivatorParameters(Path cacheDir, List<DerivedFormat> requestedFormats, String customModelDir) {
        super(cacheDir);
        Objects.requireNonNull(requestedFormats, "requestedFormats must not be null");
        if (requestedFormats.isEmpty()) {
            throw new IllegalArgumentException("requestedFormats must not be empty");
        }
        this.requestedFormats = Collections.unmodifiableList(new ArrayList<DerivedFormat>(requestedFormats));
        this.customModelDir = customModelDir;
    }

    public static DerivatorParameters of(Path cacheDir, List<DerivedFormat> requestedFormats) {
        return of(cacheDir, requestedFormats, null);
    }

    public static DerivatorParameters of(Path cacheDir, List<DerivedFormat> requestedFormats, String customModelDir) {
        return new DerivatorParameters(cacheDir, requestedFormats, customModelDir);
    }

    public Path getCacheDir() {
        return getDirectory();
    }

    public List<DerivedFormat> getRequestedFormats() {
        return requestedFormats;
    }

    public String getCustomModelDir() {
        return customModelDir;
    }
}
