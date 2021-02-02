package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;
import ch.so.agi.gretl.tasks.impl.CsvValidatorImpl;
import ch.so.agi.gretl.tasks.impl.GpkgValidatorImpl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.interlis2.validator.Validator;

import java.util.ArrayList;
import java.util.List;

public class GpkgValidator extends AbstractValidatorTask {
    private GretlLogger log;
    @Input
    public String tableName = null;

    @TaskAction
    public void validate() {
        log = LogEnvironment.getLogger(GpkgValidator.class);

        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null");
        }        
        if (dataFiles == null) {
            return;
        }
        FileCollection dataFilesCollection=null;
        if(dataFiles instanceof FileCollection) {
            dataFilesCollection=(FileCollection)dataFiles;
        }else {
            dataFilesCollection=getProject().files(dataFiles);
        }
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<String> files = new ArrayList<String>();
        for (java.io.File fileObj : dataFilesCollection) {
            String fileName = fileObj.getPath();
            files.add(fileName);
        }

        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_GPKGTABLE, tableName);
        initSettings(settings);

        System.err.println("****************");
        System.err.println("*2***************");
        System.err.println(files.toArray(new String[files.size()]).length);
        
        // failed to write logfile
        //settings.setValue(Validator.SETTING_LOGFILE, "/Users/stefan/gaga/fubar.log");
        
        System.err.println(settings);
        
        

        ch.so.agi.gretl.tasks.impl.GpkgValidatorImpl validator = new ch.so.agi.gretl.tasks.impl.GpkgValidatorImpl();
        validator.validate(files.toArray(new String[files.size()]), settings);
        
//        validationOk = new GpkgValidatorImpl().validate(files.toArray(new String[files.size()]), settings);

        System.err.println(validationOk);
        System.err.println("*3*3**************");

        if (!validationOk && failOnError) {
            throw new TaskExecutionException(this, new Exception("validation failed"));
        }
    }
}
