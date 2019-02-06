package ch.so.agi.gretl.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class IliRepositorizer extends DefaultTask {
    protected GretlLogger log;

    @Input
    public String modelsDir = null;

    @OutputFile
    public Object dataFile = null;

    @TaskAction
    public void writeIliModelsFile() {
        log = LogEnvironment.getLogger(IliRepositorizer.class);
        
        log.lifecycle("Hallo Fubar.");
        
        
        
    }

}
