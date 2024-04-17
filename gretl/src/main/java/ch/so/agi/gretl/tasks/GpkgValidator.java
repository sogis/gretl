package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;
import ch.so.agi.gretl.tasks.impl.GpkgValidatorImpl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.util.ArrayList;
import java.util.List;

public class GpkgValidator extends AbstractValidatorTask {
    private GretlLogger log;
    private String tableName;

    @Input
    public String getTableName(){
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @TaskAction
    public void validate() {
        log = LogEnvironment.getLogger(GpkgValidator.class);

        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null");
        }        
        if (getDataFiles() == null) {
            return;
        }
        FileCollection dataFilesCollection=null;
        if(getDataFiles() instanceof FileCollection) {
            dataFilesCollection=(FileCollection)getDataFiles();
        }else {
            dataFilesCollection=getProject().files(getDataFiles());
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

        validationOk = new GpkgValidatorImpl().validate(files.toArray(new String[files.size()]), settings);
        if (!validationOk && isFailOnError()) {
            throw new TaskExecutionException(this, new Exception("validation failed"));
        }
    }
}
