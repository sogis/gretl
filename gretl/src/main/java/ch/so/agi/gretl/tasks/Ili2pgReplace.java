package ch.so.agi.gretl.tasks;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.interlis.iox_j.logging.FileLogger;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import groovy.lang.Range;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class Ili2pgReplace extends Ili2pgAbstractTask {
    /*
        Input kann hier FileCollection, File oder String sein.
     */
    @Input
    public abstract Property<Object> getDataFile();

    @TaskAction
    public void replaceData() {
        Config settings = createConfig();
        int function = Config.FC_REPLACE;
        if (!getDataFile().isPresent()) {
            return;
        }
        
        // Liste mit saemtlicheen Dateipfaeden oder ilidata-Ids.
        List<String> files = new ArrayList<>();
        FileCollection dataFilesCollection;
        Object dataFile = getDataFile().get();
        if(dataFile instanceof FileCollection) {
            dataFilesCollection = (FileCollection) dataFile;
            
            if (dataFilesCollection.isEmpty()) {
                return;
            }
            
            for (File fileObj : dataFilesCollection) {
                String fileName = fileObj.getPath();
                files.add(fileName);
            }
        } else if(dataFile instanceof File) {
            File file = (File) dataFile;
            files.add(file.getAbsolutePath());
        } else if(dataFile instanceof String) {
            String fileName = (String) dataFile;
            if (fileName.startsWith("ilidata")) {
                files.add(fileName);
            } else {
                File file = this.getProject().file(fileName);
                files.add(file.getAbsolutePath());
            }
        } else {            
            List<String> dataFileList = (ArrayList) dataFile;
            for (String fileName : dataFileList) {
                
                if (fileName.startsWith("ilidata")) {
                    files.add(fileName);
                } else {
                    File file = this.getProject().file(fileName);
                    files.add(file.getAbsolutePath());
                }
            }    
            if (files.size() == 0) {
                return;
            }
        }
        
        List<String> datasetNames=null;
        
        if (getDataFile().isPresent()) {
            Object dataset = getDataset().get();
            if(dataset instanceof String) {
                datasetNames=new ArrayList<>();
                datasetNames.add((String)dataset);
            } else if (dataset instanceof FileCollection) {
                Set<File> datasetFiles = ((FileTree)dataset).getFiles();
                datasetNames = new ArrayList<>();
                Range<Integer> datasetSubstring = getDatasetSubstring().isPresent() ? getDatasetSubstring().get() : null;
                for (File datasetFile : datasetFiles) {
                    if (datasetSubstring != null) {
                        if (datasetSubstring.size() > 1) {
                            datasetNames.add(datasetFile.getName().replaceFirst("[.][^.]+$", "").substring(datasetSubstring.getFrom(), datasetSubstring.getTo()));
                        } else {
                            datasetNames.add(datasetFile.getName().replaceFirst("[.][^.]+$", "").substring(datasetSubstring.getFrom()));
                        }
                    } else {
                        datasetNames.add(datasetFile.getName().replaceFirst("[.][^.]+$", ""));
                    }
                }
            } else {
                datasetNames=new ArrayList<>();
                if (getDatasetSubstring().isPresent()) {
                    List<String> fileNames = (List)dataset;
                    Range<Integer> datasetSubstring = getDatasetSubstring().get();
                    for (String fileName : fileNames) {
                        if (datasetSubstring.size() > 1) {
                            datasetNames.add(fileName.substring(datasetSubstring.getFrom(), datasetSubstring.getTo()));
                        } else {
                            datasetNames.add(fileName.substring(datasetSubstring.getFrom()));
                        }
                    }
                } else {
                    datasetNames=(List)dataset;
                }
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
                settings.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
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
