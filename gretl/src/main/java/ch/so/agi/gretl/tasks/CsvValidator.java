package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;
import ch.so.agi.gretl.tasks.impl.CsvValidatorImpl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.util.ArrayList;
import java.util.List;

public class CsvValidator extends AbstractValidatorTask {
    private GretlLogger log;
    private Boolean firstLineIsHeader = true;
    private Character valueDelimiter = null;
    private Character valueSeparator = null;
    private String encoding = null;

    @Input
    @Optional
    public Boolean isFirstLineIsHeader() {
        return firstLineIsHeader;
    }

    @Input
    @Optional
    public Character getValueDelimiter() {
        return valueDelimiter;
    }

    @Input
    @Optional
    public Character getValueSeparator() {
        return valueSeparator;
    }
    @Optional
    public String getEncoding(){
        return encoding;
    }

    @TaskAction
    public void validate() {
        log = LogEnvironment.getLogger(CsvValidator.class);

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
        initSettings(settings);
        // set optional parameters
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE,
                firstLineIsHeader ? IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER : IoxWkfConfig.SETTING_FIRSTLINE_AS_VALUE);
        if (valueDelimiter != null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, valueDelimiter.toString());
        }
        if (valueSeparator != null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, valueSeparator.toString());
        }
        if (encoding != null) {
            settings.setValue(CsvReader.ENCODING, encoding);
        }

        validationOk = new CsvValidatorImpl().validate(files.toArray(new String[files.size()]), settings);
        if (!validationOk && isFailOnError()) {
            throw new TaskExecutionException(this, new Exception("validation failed"));
        }
    }
}
