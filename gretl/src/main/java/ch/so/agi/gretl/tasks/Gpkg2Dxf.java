package ch.so.agi.gretl.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.Gpkg2DxfStep;
import ch.so.agi.gretl.steps.Gpkg2ShpStep;
import ch.so.agi.gretl.util.TaskUtil;

public class Gpkg2Dxf extends DefaultTask {
    protected GretlLogger log;
    
    private Object dataFile;
    private File outputDir = null;

    @Input
    public Object getDataFile() {
        return dataFile;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @TaskAction
    public void run() {
        log = LogEnvironment.getLogger(Gpkg2Dxf.class);
        
        if (dataFile == null) {
            throw new IllegalArgumentException("dataFile must not be null");
        }
        if (outputDir == null) {
            throw new IllegalArgumentException("outputDir must not be null");
        }
        
        String dataFileName = this.getProject().file(dataFile).getAbsolutePath();
        String outputPath = this.getProject().file(outputDir).getAbsolutePath();

        try {
            Gpkg2DxfStep gpkg2DxfStep = new Gpkg2DxfStep();
            gpkg2DxfStep.execute(dataFileName, outputPath);
        } catch (Exception e) {
            log.error("failed to run Gpkg2Dxf", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
