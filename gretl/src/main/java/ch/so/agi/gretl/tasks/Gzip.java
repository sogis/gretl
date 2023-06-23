package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.steps.GzipStep;

public class Gzip extends DefaultTask {
    protected GretlLogger log;

    @Internal
    public File dataFile;
    
    @Internal
    public File gzipFile;
    
    @TaskAction
    public void run() {
        if (dataFile == null) {
            throw new IllegalArgumentException("dataFile must not be null");
        }

        if (gzipFile == null) {
            throw new IllegalArgumentException("gzipFile must not be null");
        }

        GzipStep gzipStep = new GzipStep();
        try {
            gzipStep.execute(dataFile, gzipFile);
        } catch (IOException e) {
            throw new GradleException("Could not gzip file: " + e.getMessage());
        }
    }
}
