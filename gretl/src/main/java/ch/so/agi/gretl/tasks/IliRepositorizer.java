package ch.so.agi.gretl.tasks;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import ch.interlis.ili2c.Ili2cException;
import ch.interlis.iox.IoxException;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.IliRepositorizerStep;
import ch.so.agi.gretl.util.TaskUtil;

public class IliRepositorizer extends DefaultTask {
    protected GretlLogger log;

    @Input
    public Object modelsDir = null;

    @OutputFile
    public Object dataFile = null;

    @TaskAction
    public void writeIliModelsFile() {
        log = LogEnvironment.getLogger(IliRepositorizer.class);
        if (modelsDir == null) {
            throw new IllegalArgumentException("modelsDir must not be null");
        }
        if (dataFile == null) {
            throw new IllegalArgumentException("dataFile must not be null");
        }

        try {
            IliRepositorizerStep iliRepositorizer = new IliRepositorizerStep();
            iliRepositorizer.build(this.getProject().file(dataFile).getAbsolutePath(), this.getProject().file(modelsDir));
        } catch (Exception e) {
            log.error("failed to run IliRepositorizer", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
