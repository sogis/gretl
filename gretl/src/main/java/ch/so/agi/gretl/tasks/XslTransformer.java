package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.XslTransformerStep;
import ch.so.agi.gretl.util.TaskUtil;

public class XslTransformer extends DefaultTask {
    protected GretlLogger log;

    private Object xslFile;
    private Object xmlFile;
    private File outDirectory;

    @Input
    public Object getXslFile() {
        return xslFile;
    }
    @Input
    public Object getXmlFile() {
        return xmlFile;
    }
    @InputDirectory
    public File getOutDirectory() {
        return outDirectory;
    }

    public void setXslFile(Object xslFile) {
        this.xslFile = xslFile;
    }

    public void setXmlFile(Object xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void setOutDirectory(File outDirectory) {
        this.outDirectory = outDirectory;
    }

    @TaskAction
    public void transform() {
        log = LogEnvironment.getLogger(XslTransformer.class);
        
        if (xslFile == null) {
            throw new IllegalArgumentException("xslFile must not be null");
        }
        if (xmlFile == null) {
            throw new IllegalArgumentException("xmlFile must not be null");
        }
        if (outDirectory == null) {
            throw new IllegalArgumentException("outDirectory must not be null");
        }

        FileCollection xmlFilesCollection = null;
        if(xmlFile instanceof FileCollection) {
            xmlFilesCollection = (FileCollection)xmlFile;
        } else {
            xmlFilesCollection = getProject().files(xmlFile);
        }
        if (xmlFilesCollection == null || xmlFilesCollection.isEmpty()) {
            // TODO: passt das? Job geht weiter.
            return;
        }
        List<String> files = new ArrayList<String>();
        for (File fileObj : xmlFilesCollection) {
            String fileName = fileObj.getAbsolutePath();
            files.add(fileName);
        }
        
        try {
            for(String dataFile : files) {
                XslTransformerStep xslTransformerStep = new XslTransformerStep();
                if (xslFile instanceof String) {
                    xslTransformerStep.execute((String) xslFile, new File(dataFile), outDirectory);
                } else {
                    xslTransformerStep.execute((File) xslFile, new File(dataFile), outDirectory);
                }
            }
        } catch (Exception e) {
            log.error("Exception in XslTransformer task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
