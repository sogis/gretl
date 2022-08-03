package ch.so.agi.gretl.tasks;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.interlis.iox_j.logging.FileLogger;
import ch.so.agi.gretl.tasks.impl.Ili2gpkgAbstractTask;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

public class Ili2gpkgImport extends Ili2gpkgAbstractTask {
    @InputFile
    public Object dataFile = null;

    @Input
    @Optional
    public boolean coalesceJson = false;    

    @Input
    @Optional
    public boolean nameByTopic = false;

    @Input
    @Optional
    public String defaultSrsCode = null;

    @Input
    @Optional
    public boolean createEnumTabs = false;
    
    @Input
    @Optional
    public boolean createMetaInfo = false;
    
    @Input
    @Optional
    public boolean createGeomIdx = false;

    @TaskAction
    public void importData() {
        Config settings = createConfig();
        // Probably the most wanted use case with GeoPackage?
        settings.setDoImplicitSchemaImport(true);

        if (dataFile == null) {
            return;
        }
        FileCollection dataFilesCollection=null;
        if(dataFile instanceof FileCollection) {
            dataFilesCollection=(FileCollection)dataFile;
        }else {
            dataFilesCollection=getProject().files(dataFile);
        }
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<String> files = new ArrayList<String>();
        for (java.io.File fileObj : dataFilesCollection) {
            String fileName = fileObj.getPath();
            files.add(fileName);
        }
        java.util.List<String> datasetNames=null;
        if (dataset != null) {
            if(dataset instanceof String) {
                datasetNames=new ArrayList<String>();
                datasetNames.add((String)dataset);
            }else {
                datasetNames=(java.util.List)dataset;
            }
            if(files.size()!=datasetNames.size()) {
                throw new GradleException("number of dataset names ("+datasetNames.size()+") doesn't match number of files ("+files.size()+")");
            }
            settings.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
        }

        
        if (coalesceJson) {
            settings.setJsonTrafo(Config.JSON_TRAFO_COALESCE);
        }

        if (nameByTopic) {
            settings.setNameOptimization(Config.NAME_OPTIMIZATION_TOPIC);
        }

        if (defaultSrsCode != null) {
            settings.setDefaultSrsCode(defaultSrsCode);
        }

        if (createEnumTabs) {
            settings.setCreateEnumDefs(Config.CREATE_ENUM_DEFS_MULTI);
        }
        
        if (createMetaInfo) {
            settings.setCreateMetaInfo(true);
        }
        
        if (createGeomIdx) {
            settings.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
        }

        int function = Config.FC_IMPORT;
        ch.ehi.basics.logging.FileListener fileLogger=null;
        if(logFile!=null){
            // setup logger here, so that multiple file imports result in one logfile
            java.io.File logFilepath=this.getProject().file(logFile);
            fileLogger=new FileLogger(logFilepath);
            EhiLogger.getInstance().addListener(fileLogger);
        }
        try {
            int i=0;
            for(String xtfFilename:files) {
                if (Ili2db.isItfFilename(xtfFilename)) {
                    settings.setItfTransferfile(true);
                }else {
                    settings.setItfTransferfile(false);
                }
                settings.setXtffile(xtfFilename);
                if(datasetNames!=null) {
                    settings.setDatasetName(datasetNames.get(i));
                }
                run(function, settings);            
                i++;
            }
        }finally{
            if(fileLogger!=null){
                EhiLogger.getInstance().removeListener(fileLogger);
                fileLogger.close();
                fileLogger=null;
            }
        }
    }
}
