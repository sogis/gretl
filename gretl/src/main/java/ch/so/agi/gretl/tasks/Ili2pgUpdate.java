package ch.so.agi.gretl.tasks;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.interlis.iox_j.logging.FileLogger;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class Ili2pgUpdate extends Ili2pgAbstractTask {
    
    /**
     * Name der XTF-/ITF-Datei, die gelesen werden soll. Es können auch mehrere Dateien sein. `FileCollection` oder `List`.
     */
    @Input
    public abstract Property<Object> getDataFile();

    @TaskAction
    public void updateData() {
        Config settings = createConfig();
        int function = Config.FC_UPDATE;
        if (!getDataFile().isPresent()) {
            return;
        }

        // Liste mit saemtlichen Dateipfaeden oder ilidata-Ids.
        List<String> files = new ArrayList<>();
        Object dataFile = getDataFile().get();
        if (dataFile instanceof FileCollection) {
            FileCollection dataFilesCollection = (FileCollection) dataFile;
            if (dataFilesCollection.isEmpty()) {
                return;
            }
            for (File fileObj : dataFilesCollection) {
                String fileName = fileObj.getPath();
                files.add(fileName);
            }
        } else if (dataFile instanceof List) {            
            List<String> dataFileList = (ArrayList) dataFile;
            for (String fileName : dataFileList) {
                if (!fileName.startsWith("ilidata")) {
                    throw new GradleException("dataFile: must start with ilidata");
                }
                files.add(fileName);
            }    
        } else {            
            throw new GradleException("dataFile: illegal data type <"+dataFile.getClass()+">");
        }

        if (files.size() == 0) {
            return;
        }

        List<String> datasetNames = null;
        if (getDataset().isPresent()) {
            Object dataset = getDataset().get();
            if(dataset instanceof String) {
                datasetNames=new ArrayList<String>();
                datasetNames.add((String)dataset);
            }else {
                datasetNames=(List)dataset;
            }
            if(files.size()!=datasetNames.size()) {
                throw new GradleException("number of dataset names ("+datasetNames.size()+") doesn't match number of files ("+files.size()+")");
            }
        }
        
        ch.ehi.basics.logging.FileListener fileLogger=null;
        if(getLogFile().isPresent()){
            // setup logger here, so that multiple file imports result in one logfile
            java.io.File logFilepath=this.getProject().file(getLogFile().get());
            fileLogger=new FileLogger(logFilepath);
            EhiLogger.getInstance().addListener(fileLogger);
        }
        try {
            int i=0;
            for(String xtfFilename:files) {
                settings.setItfTransferfile(Ili2db.isItfFilename(xtfFilename));
                if(datasetNames!=null) {
                    settings.setDatasetName(datasetNames.get(i));
                }
                settings.setXtffile(xtfFilename);
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
