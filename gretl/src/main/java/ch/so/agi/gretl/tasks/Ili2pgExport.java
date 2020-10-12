package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class Ili2pgExport extends Ili2pgAbstractTask {
    @Input
    @Optional
    public boolean export3 = false;

    @OutputFile
    public Object dataFile = null;

    @TaskAction
    public void exportData() {
        Config settings = createConfig();
        int function = Config.FC_EXPORT;
        if (dataFile == null) {
            return;
        }
        if (export3) {
            settings.setVer3_export(true);
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
