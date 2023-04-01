package ch.so.agi.gretl.steps.metapublisher.geocat.model;

import ch.interlis.iom.IomObject;

public class OfficeGC {
    private IomObject inner;

    public OfficeGC(IomObject inner) {
        this.inner = inner;
    }

    public String getName() {
        String res = inner.getattrvalue("agencyName");

        if (inner.getattrvalue("division") != null && inner.getattrvalue("division").length() > 0)
            res = res + ", " + inner.getattrvalue("divions");

        return res;
    }
    
    public String getPhone() { 
        return inner.getattrvalue("phone");
    }

    public String getEmail() {
        if (inner.getattrvalue("email") == null)
            return null;

        String mailAdress = inner.getattrvalue("email").substring(7); // TODO robuster
        return mailAdress;
    }

    public String getAbbreviation() {
        return inner.getattrvalue("abbreviation");
    }

    public String getUrl() {
        if (inner.getattrvalue("officeAtWeb") == null)
            return null;

        return inner.getattrvalue("officeAtWeb");
    }

}
