package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.interlis2.validator.Validator;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.Csv2ParquetStep;

import javax.annotation.Nullable;

public class Csv2Parquet extends DefaultTask {
    protected GretlLogger log;

    private File csvFile;
    private Boolean firstLineIsHeader = true;
    private Character valueDelimiter = null;
    private Character valueSeparator = null;
    private String encoding;
    private String models;
    private String modeldir;
    private File outputDir;

    @Internal
    public File getCsvFile() {
        return csvFile;
    }

    @Internal
    @Nullable
    public Boolean isFirstLineIsHeader() {
        return firstLineIsHeader;
    }

    @Internal
    @Nullable
    public Character getValueDelimiter() {
        return valueDelimiter;
    }

    @Internal
    @Nullable
    public Character getValueSeparator() {
        return valueSeparator;
    }

    @Internal
    @Nullable
    public String getEncoding() {
        return encoding;
    }

    @Internal
    @Nullable
    public String getModels() {
        return models;
    }

    @Internal
    @Nullable
    public String getModeldir() {
        return modeldir;
    }

    @Internal
    @Nullable
    public File getOutputDir() {
        return outputDir;
    }

    @TaskAction
    public void run() {
        log = LogEnvironment.getLogger(Csv2Parquet.class);

        if (csvFile == null) {
            throw new IllegalArgumentException("csvFile must not be null");
        }
        
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE,
                firstLineIsHeader ? IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER : IoxWkfConfig.SETTING_FIRSTLINE_AS_VALUE);
        
        if (valueDelimiter != null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, valueDelimiter.toString());
        }
        
        if (valueSeparator != null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, valueSeparator.toString());
        }
        
        if (encoding != null) {
            settings.setValue(CsvReader.ENCODING, encoding);
        }
        
        if (models != null) {
            settings.setValue(Validator.SETTING_MODELNAMES, models);
        }

        if (modeldir != null) {
            settings.setValue(Validator.SETTING_ILIDIRS, modeldir);
        }

        if (outputDir == null) {
            outputDir = csvFile.getParentFile();
        }

        try {
            Csv2ParquetStep csv2ParquetStep = new Csv2ParquetStep();
            csv2ParquetStep.execute(csvFile.toPath(), outputDir.toPath(), settings);
            log.lifecycle("Parquet file written: " + csvFile.getParentFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new GradleException("Could not write Parquet file: " + e.getMessage());
        }
    }

}
