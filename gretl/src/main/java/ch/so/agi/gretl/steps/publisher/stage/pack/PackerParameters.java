package ch.so.agi.gretl.steps.publisher.stage.pack;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleDirectoryParameters;

public final class PackerParameters extends AbstractSingleDirectoryParameters {
    private final String dataIdent;
    private final List<OutputFormat> outputFormats;

    private PackerParameters(Path cacheDir, String dataIdent, List<OutputFormat> outputFormats) {
        super(cacheDir);
        this.dataIdent = requireText(dataIdent, "dataIdent");
        this.outputFormats = normalizeFormats(outputFormats);
    }

    public static PackerParameters of(Path cacheDir, String dataIdent, List<OutputFormat> outputFormats) {
        return new PackerParameters(cacheDir, dataIdent, outputFormats);
    }

    public Path getCacheDir() {
        return getDirectory();
    }

    public String getDataIdent() {
        return dataIdent;
    }

    public List<OutputFormat> getOutputFormats() { return outputFormats; }

    private static List<OutputFormat> normalizeFormats(List<OutputFormat> values) {
        Objects.requireNonNull(values, "outputFormats must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("outFormats must not be empty");
        }
        List<OutputFormat> result = new ArrayList<OutputFormat>();
        for (OutputFormat value : values) {
            OutputFormat format = Objects.requireNonNull(value, "outFormats must not contain null values");
            if (!result.contains(format)) {
                result.add(format);
            }
        }
        return Collections.unmodifiableList(result);
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
