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
import org.gradle.api.tasks.*;

public class Ili2gpkgImport extends Ili2gpkgAbstractTask {
    private Object dataFile;
    private Boolean coalesceJson = false;
    private Boolean nameByTopic = false;
    private String defaultSrsCode;
    private Boolean createEnumTabs = false;
    private Boolean createMetaInfo = false;
    private Boolean createGeomIdx = false;

    @InputFiles
    public Object getDataFile(){
        return dataFile;
    }

    @Input
    @Optional
    public Boolean isCoalesceJson() {
        return coalesceJson;
    }

    @Input
    @Optional
    public Boolean isNameByTopic() {
        return nameByTopic;
    }

    @Input
    @Optional
    public String getDefaultSrsCode() {
        return defaultSrsCode;
    }

    @Input
    @Optional
    public Boolean isCreateEnumTabs() {
        return createEnumTabs;
    }

    @Input
    @Optional
    public Boolean isCreateMetaInfo() {
        return createMetaInfo;
    }

    @Input
    @Optional
    public Boolean isCreateGeomIdx() {
        return createGeomIdx;
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }

    public void setCoalesceJson(Boolean coalesceJson) {
        this.coalesceJson = coalesceJson;
    }

    public void setNameByTopic(Boolean nameByTopic) {
        this.nameByTopic = nameByTopic;
    }

    public void setDefaultSrsCode(String defaultSrsCode) {
        this.defaultSrsCode = defaultSrsCode;
    }

    public void setCreateEnumTabs(Boolean createEnumTabs) {
        this.createEnumTabs = createEnumTabs;
    }

    public void setCreateMetaInfo(Boolean createMetaInfo) {
        this.createMetaInfo = createMetaInfo;
    }

    public void setCreateGeomIdx(Boolean createGeomIdx) {
        this.createGeomIdx = createGeomIdx;
    }

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
        if (getDataset() != null) {
            if(getDataset() instanceof String) {
                datasetNames=new ArrayList<String>();
                datasetNames.add((String)getDataset());
            }else {
                datasetNames=(java.util.List)getDataset();
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
        if(getLogFile()!=null){
            // setup logger here, so that multiple file imports result in one logfile
            java.io.File logFilepath=this.getProject().file(getLogFile());
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
