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
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.iox.IoxException;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.Csv2ParquetStep;
import ch.so.agi.gretl.steps.OgdMetaPublisherStep;

public class OgdMetaPublisher extends DefaultTask {
    protected GretlLogger log;

    @Internal
    public File configFile;
        
    @Internal
    public File outputDir;
        
    @TaskAction
    public void run() {
        log = LogEnvironment.getLogger(OgdMetaPublisher.class);

        if (configFile == null) {
            throw new IllegalArgumentException("configFile must not be null");
        }

        if (outputDir == null) {
            throw new IllegalArgumentException("outputDir");
        }

        try {
            OgdMetaPublisherStep ogdMetaPublisherStep = new OgdMetaPublisherStep();
            ogdMetaPublisherStep.execute(configFile.toPath(), outputDir.toPath());
            log.lifecycle("Meta file written: " + outputDir.getAbsolutePath());
        } catch (IOException | Ili2cException | IoxException e) {
            throw new GradleException("Could not write meta file: " + e.getMessage());
        }
    }
}
