package ch.so.agi.gretl.steps;

import org.gradle.api.GradleException;

public class GeometryTransformWkt extends GeometryTransform {

    private int epsgCode;

    GeometryTransformWkt(String[] definitionParts) {
        super(definitionParts[0]);

        if (definitionParts.length != 3)
            throw new GradleException(
                    String.format("Configuration error. Expecting format string %s for wkt", this.formatInfo()));

        this.epsgCode = parseEpsgCode(definitionParts[2]);
    }

    @Override
    public String wrapWithGeoTransformFunction(String valuePlaceHolder) {
        String res = String.format("ST_GeomFromText(%s, %s)", valuePlaceHolder, epsgCode);

        return res;
    }

    @Override
    public String formatInfo() {
        return "[colname]:WKT:[epsg_code]. All case insensitive.";
    }
}
