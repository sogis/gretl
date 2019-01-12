package ch.so.agi.gretl.steps;

import org.gradle.api.GradleException;

public class GeometryTransformGeoJson extends GeometryTransform {

    private int epsgCode;

    GeometryTransformGeoJson(String[] definitionParts) {
        super(definitionParts[0]);

        if (definitionParts.length != 3)
            throw new GradleException(
                    String.format("Configuration error. Expecting format string %s for wkb", this.formatInfo()));

        this.epsgCode = parseEpsgCode(definitionParts[2]);
    }

    @Override
    public String wrapWithGeoTransformFunction(String valuePlaceHolder) {
        return String.format("ST_SetSRID(ST_GeomFromGeoJSON(%s), %s)", valuePlaceHolder, epsgCode);
    }

    @Override
    public String formatInfo() {
        return "[ColumnName]:GEOJSON:[EPSG_Code]. All case insensitive";
    }
}
