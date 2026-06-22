package ch.so.agi.gretl.steps.publisher.stage.derivedformats;

public enum DerivedFormat {
    GPKG("gpkg"),
    SHP("shp"),
    DXF("dxf"),
    GEOBAU_DXF("geobau_dxf");

    private final String directoryName;

    DerivedFormat(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public boolean requiresGpkg() {
        return this == SHP || this == DXF;
    }
}
