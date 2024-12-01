package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.GzipStep;

public abstract class Gzip extends DefaultTask {
    protected GretlLogger log;

    @InputFile
    public abstract Property<File> getDataFile();

    @OutputFile
    public abstract Property<File> getGzipFile();

    @TaskAction
    public void run() {
        log = LogEnvironment.getLogger(Gzip.class);

        if (!getDataFile().isPresent()) {
            throw new IllegalArgumentException("dataFile must not be null");
        }

        if (!getGzipFile().isPresent()) {
            getDataFile().set(new File(getDataFile().get().getAbsolutePath() + ".gz"));
        }

        GzipStep gzipStep = new GzipStep();
        try {
            gzipStep.execute(getDataFile().get(), getGzipFile().get());
            log.lifecycle("Gzip file written: " + getGzipFile().get().getAbsolutePath());
        } catch (IOException e) {
            throw new GradleException("Could not gzip file: " + e.getMessage());
        }
    }
}
