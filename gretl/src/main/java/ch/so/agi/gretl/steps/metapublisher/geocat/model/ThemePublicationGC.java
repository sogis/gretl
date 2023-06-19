package ch.so.agi.gretl.steps.metapublisher.geocat.model;

import java.util.List;
import java.util.UUID;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import ch.interlis.iom.IomObject;

public class ThemePublicationGC {

    private IomObject tpObj;
    private List<FileFormatGC> fileFormats;
    private OfficeGC ownerOrg;
    private OfficeGC serviceOrg;

    public ThemePublicationGC(IomObject tpObj) {
        this.tpObj = tpObj;
        
        fileFormats = new ArrayList<>();
        
        for (int i=0;i<tpObj.getattrvaluecount("fileFormats");i++) {
            IomObject ffObj = tpObj.getattrobj("fileFormats", i);
            fileFormats.add(new FileFormatGC(tpObj, ffObj));            
        }
        
        this.ownerOrg = new OfficeGC(tpObj.getattrobj("owner", 0));
        this.serviceOrg = new OfficeGC(tpObj.getattrobj("servicer", 0));
    }
    
    public String getTitle() { 
        return tpObj.getattrvalue("title");
    }

    public String getDescription() {
        return tpObj.getattrvalue("shortDescription");
    }

    public String getLastPublished() {
        return tpObj.getattrvalue("lastPublishingDate");
    }
    
    public List<FileFormatGC> getFileFormats() {
        return fileFormats;
    }

    public String getDataAppPageUrl() {
        String full = tpObj.getattrvalue("appHostUrl") + "?filter=" + tpObj.getattrvalue("identifier");
        return full;
    }
    
    public OfficeGC getOwnerOrg() {
        return ownerOrg;
    }

    public OfficeGC getServiceOrg() {
        return serviceOrg;
    }

    public List<String> getKeysAndSynos(){
        boolean noKeys = tpObj.getattrvalue("keywords") == null || tpObj.getattrvalue("keywords").length() == 0;
        boolean noSynos = tpObj.getattrvalue("synonyms") == null || tpObj.getattrvalue("synonyms").length() == 0;

        if (noKeys && noSynos)
            return null;

        ArrayList<String> res = new ArrayList<>();

        if (!noKeys) {
            res.addAll(Arrays.asList(tpObj.getattrvalue("keywords").split(",")));
            
        }

        if (!noSynos) {
            res.addAll(Arrays.asList(tpObj.getattrvalue("synonyms").split(",")));            
        }

        return res;
    }

    public boolean getHasDirectPortalDownloads() {
        return tpObj.getattrvaluecount("items") == 1;
    }

    // TODO: geht heute noch nicht.
    // Oder gute Idee? Hardcodieren in meta.toml?
    public String getPreviewUrl(){
        return null;
    }

    public String getRandomUid(){
        return UUID.randomUUID().toString();
    }
}
