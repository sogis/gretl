package ch.so.agi.gretl.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.XslTransformerStep;
import ch.so.agi.gretl.util.TaskUtil;

public class XslTransformer extends DefaultTask {
    protected GretlLogger log;

    @Input
    public String xslFileName;
    
    @Input
    public File xmlFile;
            
    @Input
    public File outDirectory;

    @TaskAction
    public void transform() {
        log = LogEnvironment.getLogger(XslTransformer.class);
        
        if (xslFileName == null) {
            throw new IllegalArgumentException("xslFileName must not be null");
        }
        if (xmlFile == null) {
            throw new IllegalArgumentException("xmlFile must not be null");
        }
        if (outDirectory == null) {
            throw new IllegalArgumentException("outDirectory must not be null");
        }

        try {
            XslTransformerStep xslTransformerStep = new XslTransformerStep();
            xslTransformerStep.execute(xslFileName, xmlFile, outDirectory);
        } catch (Exception e) {
            log.error("Exception in XslTransformer task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
