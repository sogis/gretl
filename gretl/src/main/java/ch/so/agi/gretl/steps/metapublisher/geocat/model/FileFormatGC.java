package ch.so.agi.gretl.steps.metapublisher.geocat.model;

import java.util.HashMap;
import java.util.UUID;

import ch.interlis.iom.IomObject;

public class FileFormatGC {
    
    private static final String XTF_ABBREVIATION = "xtf.zip";
    private static final String ITF_ABBREVIATION = "itf.zip";
    private static final String GPKG_ABBREVIATION = "gpkg.zip";
    private static final String SHP_ABBREVIATION = "shp.zip";
    private static final String DXF_ABBREVIATION = "dxf.zip";
    private static final String GEOTIFF_ABBREVIATION = "tif";
    private static final String LAZ_ABBREVIATION = "laz";

    private IomObject tpObj;
    private IomObject ffObj;
    private static HashMap<String, String> protocols;
    private UUID id;

    static {
        protocols = new HashMap<>();
        protocols.put(GPKG_ABBREVIATION, "WWW:DOWNLOAD:Geopackage (ogc)");
        protocols.put(XTF_ABBREVIATION, "WWW:DOWNLOAD:INTERLIS");
        protocols.put(ITF_ABBREVIATION, "WWW:DOWNLOAD:INTERLIS");
        protocols.put(SHP_ABBREVIATION, "WWW:DOWNLOAD:SHP");
        protocols.put(DXF_ABBREVIATION, "WWW:DOWNLOAD:DXF");
        protocols.put(GEOTIFF_ABBREVIATION, "WWW:DOWNLOAD:GeoTIFF");
        protocols.put(LAZ_ABBREVIATION, "WWW:DOWNLOAD:LAZ");
    }

    public FileFormatGC(IomObject tpObj, IomObject ffObj) {
        this.tpObj = tpObj;
        this.ffObj = ffObj;
        this.id = UUID.randomUUID();
    }
    
    public String getUrl() {
        String[] nameParts = new String[] {
                tpObj.getattrvalue("identifier"),
                ffObj.getattrvalue("extension")
        };

        String name = String.join(".", nameParts);

        String url = tpObj.getattrvalue("downloadHostUrl") + "/" + tpObj.getattrvalue("identifier") + "/aktuell/" + name;
        return url;
    }

    private boolean isVectorTheme(){
        boolean isVec = tpObj.getattrvalue("model") != null && tpObj.getattrvalue("model").length() > 0;
        return isVec;
    }

    public String getName(){
        return ffObj.getattrvalue("name");
    }
    
    public String getNameWithTpTitle() {
        String name = ffObj.getattrvalue("name");

        if(isVectorTheme())
            name += " (in Zip)";

        name += ": " + tpObj.getattrvalue("title");

        return name;
    }

    public String getVersion() {
        if(ffObj.getattrvalue("extension") == null)
            return null;

        String res = "-";

        if(ffObj.getattrvalue("extension").equals(XTF_ABBREVIATION))
            res = "2";
        else if(ffObj.getattrvalue("extension").equals(ITF_ABBREVIATION))
            res = "1";

        return res;
    }

    public String getIdString(){
        return id.toString();
    }

    public String getProtocol(){
        return protocols.getOrDefault(ffObj.getattrvalue("extension"), "WWW:DOWNLOAD-URL");
    }

}
