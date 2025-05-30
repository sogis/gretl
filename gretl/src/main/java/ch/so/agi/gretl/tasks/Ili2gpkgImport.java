package ch.so.agi.gretl.tasks;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.interlis.iox_j.logging.FileLogger;
import ch.so.agi.gretl.tasks.impl.Ili2gpkgAbstractTask;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.io.File;
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

    /**
     * Name der XTF-/ITF-Datei, die gelesen werden soll. Es können auch mehrere Dateien sein. `FileCollection` oder `List`
     */
    @InputFiles
    public Object getDataFile(){
        return dataFile;
    }

    /**
     * Entspricht der ili2gpkg-Option `--coalesceJson`
     */
    @Input
    @Optional
    public Boolean isCoalesceJson() {
        return coalesceJson;
    }

    /**
     * Entspricht der ili2gpkg-Option `--nameByTopic`
     */
    @Input
    @Optional
    public Boolean isNameByTopic() {
        return nameByTopic;
    }

    /**
     * Entspricht der ili2gpkg-Option `--defaultSrsCode`
     */
    @Input
    @Optional
    public String getDefaultSrsCode() {
        return defaultSrsCode;
    }

    /**
     * Entspricht der ili2gpkg-Option `--createEnumTabs`
     */
    @Input
    @Optional
    public Boolean isCreateEnumTabs() {
        return createEnumTabs;
    }

    /**
     * Entspricht der ili2gpkg-Option `--createMetaInfo`
     */
    @Input
    @Optional
    public Boolean isCreateMetaInfo() {
        return createMetaInfo;
    }

    /**
     * Entspricht der ili2gpkg-Option `--createGeomIdx`
     */
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
        List<String> files = new ArrayList<String>();
        if (dataFile instanceof FileCollection) {
            FileCollection dataFilesCollection = (FileCollection)dataFile;
            for (File fileObj : dataFilesCollection) {
                String fileName = fileObj.getPath();
                files.add(fileName);
            }
        } else if (dataFile instanceof List) {
            List<String> dataFiles = (List) dataFile;
            for (String file : dataFiles) {
                if (!file.startsWith("ilidata")) {
                    throw new GradleException("dataFile: must start with ilidata");
                }
                files.add(file);
            }
        } else {            
            throw new GradleException("dataFile: illegal data type <"+dataFile.getClass()+">");
        }
        
        if (files == null || files.isEmpty()) {
            return;
        }

        List<String> datasetNames = null;
        if (getDataset() != null) {
            if(getDataset() instanceof String) {
                datasetNames=new ArrayList<String>();
                datasetNames.add((String)getDataset());
            } else {
                datasetNames=(List)getDataset();
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
        if (getLogFile() != null) {
            // setup logger here, so that multiple file imports result in one logfile
            java.io.File logFilepath=this.getProject().file(getLogFile());
            fileLogger=new FileLogger(logFilepath);
            EhiLogger.getInstance().addListener(fileLogger);
        }
        try {
            int i=0;
            for (String xtfFilename:files) {
                if (Ili2db.isItfFilename(xtfFilename)) {
                    settings.setItfTransferfile(true);
                }else {
                    settings.setItfTransferfile(false);
                }
                settings.setXtffile(xtfFilename);
                if (datasetNames != null ) {
                    settings.setDatasetName(datasetNames.get(i));
                }
                run(function, settings);            
                i++;
            }
        } finally {
            if (fileLogger != null) {
                EhiLogger.getInstance().removeListener(fileLogger);
                fileLogger.close();
                fileLogger = null;
            }
        }
    }
}
