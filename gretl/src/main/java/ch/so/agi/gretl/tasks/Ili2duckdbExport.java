package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2dbExport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;

public abstract class Ili2duckdbExport extends Ili2dbExport {

    @TaskAction
    public void exportData() {
        Config settings = createConfig();
        int function = Config.FC_EXPORT;
        if (!getDataFile().isPresent()) {
            return;
        }
        if (getExport3().getOrElse(false)) {
            settings.setVer3_export(true);
        }
        if (getExportModels().isPresent()) {
            settings.setExportModels(getExportModels().get());
        }
        
        FileCollection dataFilesCollection = (FileCollection)getDataFile().get();
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }

        List<String> files = new ArrayList<>();
        for (File fileObj : dataFilesCollection) {
            String fileName = fileObj.getPath();
            files.add(fileName);
        }
        
        List<String> datasetNames = null;
        if (getDataset().isPresent()) {
            Object dataset = getDataset().get();
            datasetNames=(List)dataset;
            if (files.size() != datasetNames.size()) {
                throw new GradleException("number of dataset names ("+datasetNames.size()+") doesn't match number of files ("+files.size()+")");
            }
        }
        
        int i=0;
        for (String xtfFilename:files) {
            settings.setItfTransferfile(Ili2db.isItfFilename(xtfFilename));
            if (datasetNames != null) {
                settings.setDatasetName(datasetNames.get(i));
            }
            settings.setXtffile(xtfFilename);
            run(function, settings);            
            i++;
        }
    }
    
    private Config createConfig() {
        Config settings = new Config();
        new ch.ehi.ili2duckdb.DuckDBMain().initConfig(settings);
        
        settings.setStrokeArcs(settings.STROKE_ARCS_ENABLE);
        
        return settings;
    }
}
