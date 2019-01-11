package ch.so.agi.gretl.steps;


import ch.so.agi.gretl.util.GretlException;

public abstract class GeometryTransform {

    protected static final String WKB = "WKB";
    protected static final String WKT = "WKT";
    protected static final String GEOJSON = "GEOJSON";

    protected String colNameUpperCase;

    protected GeometryTransform(String colName){
        this.colNameUpperCase = colName.toUpperCase();
    }

    public static GeometryTransform createFromString(String columnDefinition) {
        GeometryTransform res = null;

        if(columnDefinition == null)
            throw new GretlException("Column definition must not be null");

        String[] parts = columnDefinition.split(":");

        if(parts == null || parts.length < 2)
            throw new GretlException("Malformed geometry column definition. Expecting [colname]:[geomtype](:[options]).");

        String colname = parts[0].toUpperCase();
        String geomtype = parts[1].toUpperCase();

        boolean isDefinedType = geomtype.equals(WKB) || geomtype.equals(WKT) || geomtype.equals(GEOJSON);
        if(!isDefinedType)
            throw new GretlException("Unknown geometry Standard. Expecting WKB or WKT or GeoJson");

        if(geomtype.equals(WKB)){
            res = new GeometryTransformWkb(parts);
        }
        else if(geomtype.equals(WKT)){
            res = new GeometryTransformWkt(parts);
        }
        else if(geomtype.equals(GEOJSON)){
            res = new GeometryTransformGeoJson(parts);
        }

        return res;
    }

    public String getColNameUpperCase(){
        return colNameUpperCase;
    }

    public abstract String wrapWithGeoTransformFunction(String valuePlaceholer);

    public abstract String formatInfo();

    protected static int parseEpsgCode(String epsgCodeString){
        int res = -1;

        try{
            res = Integer.parseInt(epsgCodeString);
        }
        catch(NumberFormatException ne){
            throw new GretlException(String.format("Given epsg code [%s] in column configuration is not a number", epsgCodeString), ne);
        }

        return res;
    }
}
