package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
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
    public Object outputDirectory = null;
    
    @Input
    @Optional
    public String modeldir = null;
    
    @Input
    @Optional
    public String language = "de";
    
    @Input
    @Optional
    public boolean zip = false;

    @TaskAction
    public void runTransformation() {
        log = LogEnvironment.getLogger(Av2ch.class);
        
        if (inputFile == null) {
            throw new IllegalArgumentException("inputFile must not be null");
        }
        if (outputDirectory == null) {
            throw new IllegalArgumentException("outputDirectory must not be null");
        }
        
        if (!language.equalsIgnoreCase("de") && !language.equalsIgnoreCase("it")) {
            throw new IllegalArgumentException("language '" + language + "' is not supported.");
        }
        
        if (outputDirectory instanceof File) {
            ((File) outputDirectory).mkdirs();
        } else {
            new File((String) outputDirectory).mkdirs();
        }
        
        FileCollection dataFilesCollection = null;
        if (inputFile instanceof FileCollection) {
            dataFilesCollection = (FileCollection)inputFile;
        } else {
            dataFilesCollection = this.getProject().files(inputFile);
        }
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<java.io.File> files = new ArrayList<java.io.File>();
        for (java.io.File fileObj : dataFilesCollection) {
            files.add(fileObj);
        }
       
        try {
            for (File itf : files) {
                String inputFileName = this.getProject().file(itf).getAbsolutePath();
                String outputPath = this.getProject().file(outputDirectory).getAbsolutePath();
                String outputFileName = this.getProject().file(itf).getName();
                
                ch.so.agi.av.Av2ch av2ch = new ch.so.agi.av.Av2ch();

                if (modeldir != null) {
                    av2ch.setModeldir(modeldir);
                }
       
                av2ch.convert(inputFileName, outputPath, outputFileName, language);
                
                if (zip) {
                    String outZipFileName = Paths.get(outputPath, outputFileName + ".zip").toFile().getAbsolutePath();
                    FileOutputStream fileOutputStream = new FileOutputStream(outZipFileName);
                    ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

                    File itfFile = Paths.get(outputPath, outputFileName).toFile();
                    ZipEntry dxfZipEntry = new ZipEntry(itfFile.getName());
                    zipOutputStream.putNextEntry(dxfZipEntry);
                    new FileInputStream(itfFile).getChannel().transferTo(0, itfFile.length(), Channels.newChannel(zipOutputStream));

                    zipOutputStream.closeEntry();
                    zipOutputStream.close();
                    
                    //itfFile.delete();
                }
            }
        } catch (Exception e) {
            log.error("failed to run Av2ch", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        } 
    }
}
