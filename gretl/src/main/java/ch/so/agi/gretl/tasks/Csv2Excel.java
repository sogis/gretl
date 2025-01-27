package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.interlis2.validator.Validator;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.Csv2ExcelStep;

public class Csv2Excel extends DefaultTask {
    protected GretlLogger log;

    private File csvFile;
    private Boolean firstLineIsHeader = true;
    private Character valueDelimiter = null;
    private Character valueSeparator = null;
    private String encoding;
    private String models;
    private String modeldir;
    private File outputDir;

    /**
     * CSV-Datei, die konvertiert werden soll.
     */
    @InputFile
    public File getCsvFile() {
        return csvFile;
    }

    /**
     * Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true
     */
    @Input
    @Optional
    public Boolean getFirstLineIsHeader() {
        return firstLineIsHeader;
    }

    /**
     * Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default `"`
     */
    @Input
    @Optional
    public Character getValueDelimiter() {
        return valueDelimiter;
    }

    /**
     * Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: `,`
     */
    @Input
    @Optional
    public Character getValueSeparator() {
        return valueSeparator;
    }

    /**
     * Zeichencodierung der CSV-Datei, z.B. `UTF-8`. Default: Systemeinstellung
     */
    @Input
    @Optional
    public String getEncoding() {
        return encoding;
    }
    
    /**
     * INTERLIS-Modell für Definition der Datentypen in der Excel-Datei.
     */
    @Input
    @Optional
    public String getModels() {
        return models;
    }

    /**
     * INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator).
     */
    @Input
    @Optional
    public String getModeldir() {
        return modeldir;
    }

    /**
     * Verzeichnis, in das die Excel-Datei gespeichert wird. Default: Verzeichnis, in dem die CSV-Datei vorliegt.
     */
    @OutputDirectory
    @Optional
    public File getOutputDir() {
        return outputDir;
    }

    public void setCsvFile(File csvFile) {
        this.csvFile = csvFile;
    }

    public void setFirstLineIsHeader(Boolean firstLineIsHeader) {
        this.firstLineIsHeader = firstLineIsHeader;
    }

    public void setValueDelimiter(Character valueDelimiter) {
        this.valueDelimiter = valueDelimiter;
    }

    public void setValueSeparator(Character valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setModels(String models) {
        this.models = models;
    }

    public void setModeldir(String modeldir) {
        this.modeldir = modeldir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @TaskAction
    public void run() {
        log = LogEnvironment.getLogger(Csv2Excel.class);

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
            outputDir = getCsvFile().getParentFile();
        }

        try {
            Csv2ExcelStep csv2ExcelStep = new Csv2ExcelStep();
            csv2ExcelStep.execute(getCsvFile().toPath(), getOutputDir().toPath(), settings);
            log.lifecycle("Excel file written: " + getCsvFile().getParentFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new GradleException("Could not write Excel file: " + e.getMessage());
        }
    }

}
