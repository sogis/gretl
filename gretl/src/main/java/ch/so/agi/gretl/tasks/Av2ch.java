package ch.so.agi.gretl.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class Av2ch extends DefaultTask {
    protected GretlLogger log;
    
    @Input
    public Object inputFile = null;
    
    @Input
    public Object outputFile = null;
    
    @Input
    @Optional
    public String language = "de";

    @TaskAction
    public void runTransformation() {
        log = LogEnvironment.getLogger(Av2ch.class);
        
        if (inputFile == null) {
            throw new IllegalArgumentException("inputFile must not be null");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("outputFile must not be null");
        }
        if (!language.equalsIgnoreCase("de") && !language.equalsIgnoreCase("it")) {
            throw new IllegalArgumentException("language '" + language + "' is not supported.");
        }

        String inputFileName = this.getProject().file(inputFile).getAbsolutePath();
        String outputPath = this.getProject().file(outputFile).getParent();
        String outputFileName = this.getProject().file(outputFile).getName();
        
        try {
            ch.so.agi.av.Av2ch av2ch = new ch.so.agi.av.Av2ch();
            av2ch.convert(inputFileName, outputPath, outputFileName, language);
        } catch (Exception e) {
            log.error("failed to run Av2ch", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        } 
    }
}
