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
    @Input
    @Optional
    public abstract Property<Boolean> isExport3();
    @Input
    @Optional
    public abstract Property<String> getExportModels();

    @OutputFiles
    public abstract Property<Object> getDataFile();

    @TaskAction
    public void exportData() {
        Config settings = createConfig();
        int function = Config.FC_EXPORT;
        if (!getDataFile().isPresent()) {
            return;
        }
        if (isExport3().get()) {
            settings.setVer3_export(true);
        }
        if (getExportModels().isPresent()) {
            settings.setExportModels(getExportModels().get());
        }
        FileCollection dataFilesCollection=null;
        Object dataFile = getDataFile().get();
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
        List<String> datasetNames=null;
        Object dataset = getDataset().get();
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
        }
        
        int i=0;
        for(String xtfFilename:files) {
            if (Ili2db.isItfFilename(xtfFilename)) {
                settings.setItfTransferfile(true);
            }else {
                settings.setItfTransferfile(false);
            }
            if(datasetNames!=null) {
                settings.setDatasetName(datasetNames.get(i));
            }
            settings.setXtffile(xtfFilename);
            run(function, settings);            
            i++;
        }
    }
}
