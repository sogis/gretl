package ch.so.agi.gretl.tasks;

import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import ch.so.agi.oereb.LegendEntry;

public class OerebIconizer extends DefaultTask {
    protected GretlLogger log;

    @Input
    public String vendor = null;
    
    @Input
    public String stylesUrl = null;
    
    @Input 
    public String legendGraphicUrl = null;
    
    @Input
    public Connector database = null;
    
    @Input
    public String dbSchema = null;
    
    @Input
    public String dbTable = null;
    
    @Input
    public String typeCodeAttrName = null;
    
    @Input
    public String typeCodeListAttrName = null;

    @Input
    public String typeCodeListValue = null;

    @Input 
    public String symbolAttrName = null;
        
    @Input
    @Optional
    public boolean substringMode = false;

    @TaskAction
    public void createAndSaveSymbols() {
        log = LogEnvironment.getLogger(OerebIconizer.class);

        if (vendor == null) {
            throw new IllegalArgumentException("vendor must not be null");
        }
        if (stylesUrl == null) {
            throw new IllegalArgumentException("stylesUrl must not be null");
        }
        if (legendGraphicUrl == null) {
            throw new IllegalArgumentException("legendGraphicUrl must not be null");
        }
        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        if (dbSchema == null) {
            throw new IllegalArgumentException("dbSchema must not be null");
        }
        if (dbTable == null) {
            throw new IllegalArgumentException("dbTable must not be null");
        }
        if (typeCodeAttrName == null) {
            throw new IllegalArgumentException("typeCodeAttrName must not be null");
        }
        if (typeCodeListAttrName == null) {
            throw new IllegalArgumentException("typeCodeListAttrName must not be null");
        }
        if (typeCodeListValue == null) {
            throw new IllegalArgumentException("typeCodeListValue must not be null");
        }
        if (symbolAttrName == null) {
            throw new IllegalArgumentException("symbolAttrName must not be null");
        }
                            
        try {            
            ch.so.agi.oereb.OerebIconizer iconizer = new ch.so.agi.oereb.OerebIconizer();
            List<LegendEntry> legendEntries = iconizer.getSymbols(vendor, stylesUrl, legendGraphicUrl);
            int count = iconizer.updateSymbols(legendEntries, database.getDbUri(), database.getDbUser(), database.getDbPassword(), 
                    dbSchema, dbTable, typeCodeAttrName, typeCodeListAttrName, typeCodeListValue, symbolAttrName, substringMode);
            log.info("Updated " + String.valueOf(count) + " record(s).");
        } catch (Exception e) {
            log.error("Exception in OerebIconizer task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
