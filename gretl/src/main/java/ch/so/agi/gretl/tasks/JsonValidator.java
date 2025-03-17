package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;
import ch.so.agi.gretl.tasks.impl.JsonValidatorImpl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;

import java.util.ArrayList;
import java.util.List;

public class JsonValidator extends AbstractValidatorTask {

    // Ggf Umgang mit Top Level Array. Falls Reader es verlangt, für Benutzer aber "unlogisch".
    
    @TaskAction
    public void validate() {
        log = LogEnvironment.getLogger(JsonValidator.class);

        if (getDataFiles() == null) {
            return;
        }
        FileCollection dataFilesCollection=null;
        if (getDataFiles() instanceof FileCollection) {
            dataFilesCollection=(FileCollection)getDataFiles();
        } else {
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

        validationOk = new JsonValidatorImpl().validate(files.toArray(new String[files.size()]), settings);
        if (!validationOk && getFailOnError()) {
            throw new TaskExecutionException(this, new Exception("validation failed"));
        }
    }
}
