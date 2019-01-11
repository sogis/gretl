package ch.so.agi.gretl.tasks;


import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;
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


public class IliValidator extends AbstractValidatorTask {
    private GretlLogger log;

    @TaskAction
    public void validate() {
        log = LogEnvironment.getLogger(IliValidator.class);

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
        validationOk=new Validator().validate(files.toArray(new String[files.size()]), settings);
        if(!validationOk && failOnError) {
            throw new TaskExecutionException(this,new Exception("validation failed"));
        }
    }

}

