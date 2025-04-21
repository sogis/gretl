package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.XslTransformerStep;
import ch.so.agi.gretl.util.TaskUtil;

public class XslTransformer extends DefaultTask {
    protected GretlLogger log;

    private Object xslFile;
    private FileCollection xmlFile;
    private File outDirectory;
    private String fileExtension = "xtf";

    /**
     * Name (`String`) der XSLT-Datei, die im `src/main/resources/xslt`-Verzeichnis liegen muss oder `File`-Objekt (beliebiger Pfad).
     */
    @Input
    public Object getXslFile() {
        return xslFile;
    }
    
    /**
     * XML-Dateien, die transformiert werden sollen.
     */
    @InputFiles
    public FileCollection getXmlFile() {
        return xmlFile;
    }
    
    /**
     * Verzeichnis, in das die transformierte Datei gespeichert wird. Der Name der transformierten Datei entspricht standardmässig dem Namen der Input-Datei mit Endung `.xtf`.
     */
    @OutputDirectory
    public File getOutDirectory() {
        return outDirectory;
    }
    
    /**
     * Fileextension der Resultatdatei. Default: `xtf`
     */
    @Optional
    @Input
    public String getFileExtension() {
        return fileExtension;
    }

    public void setXslFile(Object xslFile) {
        this.xslFile = xslFile;
    }

    public void setXmlFile(FileCollection xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void setOutDirectory(File outDirectory) {
        this.outDirectory = outDirectory;
    }
    
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
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

        FileCollection xmlFilesCollection = (FileCollection) xmlFile;

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
            for (String dataFile : files) {
                XslTransformerStep xslTransformerStep = new XslTransformerStep();
                if (xslFile instanceof String) {
                    xslTransformerStep.execute((String) xslFile, new File(dataFile), outDirectory, fileExtension);
                } else if (xslFile instanceof File) {
                    xslTransformerStep.execute((File) xslFile, new File(dataFile), outDirectory, fileExtension);
                } else {
                    throw new GradleException("xslFile: illegal data type <"+xslFile.getClass()+">");
                }
            }
        } catch (Exception e) {
            log.error("Exception in XslTransformer task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
