package ch.so.agi.gretl.steps.publisher.stage.derivedformats;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleDirectoryParameters;

public final class DerivatorParameters extends AbstractSingleDirectoryParameters {
    private final List<DerivedFormat> requestedFormats;

    private DerivatorParameters(Path cacheDir, List<DerivedFormat> requestedFormats) {
        super(cacheDir);
        Objects.requireNonNull(requestedFormats, "requestedFormats must not be null");
        if (requestedFormats.isEmpty()) {
            throw new IllegalArgumentException("requestedFormats must not be empty");
        }
        this.requestedFormats = Collections.unmodifiableList(new ArrayList<DerivedFormat>(requestedFormats));
    }

    public static DerivatorParameters of(Path cacheDir, List<DerivedFormat> requestedFormats) {
        return new DerivatorParameters(cacheDir, requestedFormats);
    }

    public Path getCacheDir() {
        return getDirectory();
    }

    public List<DerivedFormat> getRequestedFormats() {
        return requestedFormats;
    }
}
