package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;
import ch.so.agi.gretl.tasks.impl.CsvValidatorImpl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;

import java.util.ArrayList;
import java.util.List;

public class CsvValidator extends AbstractValidatorTask {
    private Boolean firstLineIsHeader = true;
    private Character valueDelimiter = null;
    private Character valueSeparator = null;
    private String encoding = null;

    @Input
    @Optional
    public Boolean getFirstLineIsHeader() {
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
    @Input
    @Optional
    public String getEncoding(){
        return encoding;
    }

    public void setFirstLineIsHeader(Boolean firstLineIsHeader) {
        this.firstLineIsHeader = firstLineIsHeader;
    }

    public void setValueDelimiter(Character valueDelimiter) {
        this.valueDelimiter = valueDelimiter;
    }

    public void setValueSeparator(Character valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
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
        if (!validationOk && getFailOnError()) {
            throw new TaskExecutionException(this, new Exception("validation failed"));
        }
    }
}
