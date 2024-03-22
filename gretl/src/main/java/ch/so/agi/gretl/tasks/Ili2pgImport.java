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

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

public class Ili2pgImport extends Ili2pgAbstractTask {
    private Object dataFile;
    @InputFiles
    public Object getDataFile(){
        return dataFile;
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }

    @TaskAction
    public void importData() {
        Config settings = createConfig();
        int function = Config.FC_IMPORT;
        if (dataFile == null) {
            return;
        }
        
        // Liste mit saemtlicheen Dateipfaeden oder ilidata-Ids.
        List<String> files = new ArrayList<String>();

        FileCollection dataFilesCollection = null;
        if(dataFile instanceof FileCollection) {
            dataFilesCollection = (FileCollection) dataFile;
            
            if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
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

        List<String> datasetNames = null;
        if (dataset != null) {
            if(dataset instanceof String) {
                datasetNames=new ArrayList<String>();
                datasetNames.add((String)dataset);
            } else if (dataset instanceof FileCollection) {
                Set<File> datasetFiles = ((FileTree)dataset).getFiles();
                datasetNames = new ArrayList<String>();                
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
                datasetNames=new ArrayList<String>();
                if (datasetSubstring != null) {
                    List<String> fileNames = (java.util.List)dataset;
                    for (String fileName : fileNames) {
                        if (datasetSubstring.size() > 1) {
                            datasetNames.add(fileName.substring(datasetSubstring.getFrom(), datasetSubstring.getTo()));
                        } else {
                            datasetNames.add(fileName.substring(datasetSubstring.getFrom()));
                        }
                    }
                } else {
                    datasetNames=(java.util.List)dataset;
                }
            }
            if(files.size()!=datasetNames.size()) {
                throw new GradleException("number of dataset names ("+datasetNames.size()+") doesn't match number of files ("+files.size()+")");
            }
        }
        
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
