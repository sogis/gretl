package ch.so.agi.gretl.tasks;


import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;
import ch.so.agi.gretl.tasks.impl.CsvValidatorImpl;
import ch.so.agi.gretl.tasks.impl.ShpValidatorImpl;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.interlis2.validator.Validator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CsvValidator extends AbstractValidatorTask {
    private GretlLogger log;
    @Input
    @Optional
    public boolean firstLineIsHeader=true;
    @Input
    @Optional
    public Character valueDelimiter=null;
    @Input
    @Optional
    public Character valueSeparator=null;
    @Optional
    public String encoding=null;

    @TaskAction
    public void validate() {
        log = LogEnvironment.getLogger(CsvValidator.class);

        if (dataFiles==null || dataFiles.size()==0) {
            return;
        }
        List<String> files=new ArrayList<String>();
        for(Object fileObj:dataFiles) {
            String fileName=this.getProject().file(fileObj).getPath();
            files.add(fileName);
        }
        
        Settings settings=new Settings();
        initSettings(settings);
        // set optional parameters
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE,firstLineIsHeader?IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER:IoxWkfConfig.SETTING_FIRSTLINE_AS_VALUE);
        if(valueDelimiter!=null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER,valueDelimiter.toString());
        }
        if(valueSeparator!=null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR,valueSeparator.toString());
        }
        if(encoding!=null) {
            settings.setValue(CsvReader.ENCODING, encoding);
        }

        validationOk=new CsvValidatorImpl().validate(files.toArray(new String[files.size()]), settings);
        if(!validationOk && failOnError) {
            throw new TaskExecutionException(this,new Exception("validation failed"));
        }
    }

}

