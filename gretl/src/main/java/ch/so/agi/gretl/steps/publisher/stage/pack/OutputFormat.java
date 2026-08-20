package ch.so.agi.gretl.steps.publisher.stage.pack;

import java.util.Locale;

import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivedFormat;

/** Formats that may be included in a published output. */
public enum OutputFormat {
    XTF("xtf", null),
    ITF("itf", null),
    GPKG("gpkg", DerivedFormat.GPKG),
    SHP("shp", DerivedFormat.SHP),
    DXF("dxf", DerivedFormat.DXF),
    DXF_GEOBAU("dxf_geobau", DerivedFormat.GEOBAU_DXF);

    private final String identifier;
    private final DerivedFormat derivedFormat;

    OutputFormat(String identifier, DerivedFormat derivedFormat) {
        this.identifier = identifier;
        this.derivedFormat = derivedFormat;
    }

    public String getIdentifier() { return identifier; }

    public boolean isTransferFormat() { return derivedFormat == null; }

    public DerivedFormat getDerivedFormat() { return derivedFormat; }

    public static OutputFormat parse(Object value) {
        if (value instanceof OutputFormat) {
            return (OutputFormat) value;
        }
        String normalized = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        for (OutputFormat format : values()) {
            if (format.identifier.equals(normalized)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unsupported outFormats value: " + value);
    }
}
