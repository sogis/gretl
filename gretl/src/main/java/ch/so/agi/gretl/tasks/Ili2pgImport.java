package ch.so.agi.gretl.tasks;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.interlis.iox_j.logging.FileLogger;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

public class Ili2pgImport extends Ili2pgAbstractTask {
    @InputFile
    public Object dataFile = null;

    @TaskAction
    public void importData() {
        Config settings = createConfig();
        int function = Config.FC_IMPORT;
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
        
        ch.ehi.basics.logging.FileListener fileLogger=null;
        if(logFile!=null){
            // setup logger here, so that multiple file imports result in one logfile
            java.io.File logFilepath=this.getProject().file(logFile);
            fileLogger=new FileLogger(logFilepath);
            EhiLogger.getInstance().addListener(fileLogger);
        }
        try {
            for(String xtfFilename:files) {
                if (Ili2db.isItfFilename(xtfFilename)) {
                    settings.setItfTransferfile(true);
                }else {
                    settings.setItfTransferfile(false);
                }
                settings.setXtffile(xtfFilename);
                run(function, settings);            
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
