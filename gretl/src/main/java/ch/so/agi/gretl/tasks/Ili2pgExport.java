package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

public abstract class Ili2pgExport extends Ili2pgAbstractTask {
    
    /**
     * Entspricht der ili2pg-Option `--export3`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getExport3();
    
    /**
     * Entspricht der ili2pg-Option `--exportModels`.
     */    
    @Input
    @Optional
    public abstract Property<String> getExportModels();

    /**
     * Name der XTF-/ITF-/GML-Datei, die erstellt werden soll. `FileCollection` oder `String`.
     */
    @OutputFiles
    public abstract Property<Object> getDataFile();

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
        FileCollection dataFilesCollection;
        Object dataFile = getDataFile().get();
        if (dataFile instanceof FileCollection) {
            dataFilesCollection = (FileCollection)dataFile;
        } else {
            dataFilesCollection = getProject().files(dataFile);
        }
        if (dataFilesCollection.isEmpty()) {
            return;
        }
        List<String> files = new ArrayList<>();
        for (java.io.File fileObj : dataFilesCollection) {
            String fileName = fileObj.getPath();
            files.add(fileName);
        }
        List<String> datasetNames = null;
        if (getDataset().isPresent()) {
            Object dataset = getDataset().get();
            if (dataset instanceof String) {
                datasetNames=new ArrayList<>();
                datasetNames.add((String)dataset);
            }else {
                datasetNames=(List)dataset;
            }
            if (files.size() != datasetNames.size()) {
                throw new GradleException("number of dataset names ("+datasetNames.size()+") doesn't match number of files ("+files.size()+")");
            }
        }
        
        int i=0;
        for(String xtfFilename:files) {
            settings.setItfTransferfile(Ili2db.isItfFilename(xtfFilename));
            if (datasetNames != null) {
                settings.setDatasetName(datasetNames.get(i));
            }
            settings.setXtffile(xtfFilename);
            run(function, settings);            
            i++;
        }
    }
}
