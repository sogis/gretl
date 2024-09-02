package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.Db2Csv;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class CsvExport extends DatabaseTask {
    protected GretlLogger log;
    private Object dataFile;
    private String tableName;
    private boolean firstLineIsHeader = true;
    private Character valueDelimiter;
    private Character valueSeparator;
    private String schemaName;
    private String[] attributes;
    private String encoding;

    @TaskAction
    public void exportData() {
        log = LogEnvironment.getLogger(CsvExport.class);
        final Connector connector = createConnector();

        if (connector == null) {
            throw new IllegalArgumentException("connector must not be null");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null");
        }
        if (dataFile == null) {
            return;
        }

        Settings settings = getSettings();
        File data = this.getProject().file(dataFile);

        try (Connection conn = connector.connect()) {
            Db2Csv db2csv = new Db2Csv();
            if (attributes != null) {
                db2csv.setAttributes(attributes);
            }
            db2csv.exportData(data, conn, settings);
        } catch (Exception e) {
            log.error("failed to run CsvExport", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    @OutputFile
    public Object getDataFile() {
        return dataFile;
    }

    @Input
    public String getTableName() {
        return tableName;
    }

    @Input
    @Optional
    public Boolean getFirstLineIsHeader() {
        return firstLineIsHeader;
    }

    @Input
    @Optional
    public Character getValueDelimiter() {
        return valueDelimiter;
    }

    @Input
    @Optional
    public Character getValueSeparator() {
        return valueSeparator;
    }

    @Input
    @Optional
    public String getSchemaName() {
        return schemaName;
    }

    @Input
    @Optional
    public String[] getAttributes(){
        return attributes;
    }

    @Input
    @Optional
    public String getEncoding(){
        return encoding;
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    private Settings getSettings() {
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_DBTABLE, tableName);

        // set optional parameters
        settings.setValue(
                IoxWkfConfig.SETTING_FIRSTLINE,
                firstLineIsHeader ? IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER : IoxWkfConfig.SETTING_FIRSTLINE_AS_VALUE
        );
        if (valueDelimiter != null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, valueDelimiter.toString());
        }
        if (valueSeparator != null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, valueSeparator.toString());
        }
        if (schemaName != null) {
            settings.setValue(IoxWkfConfig.SETTING_DBSCHEMA, schemaName);
        }
        if (encoding != null) {
            settings.setValue(CsvReader.ENCODING, encoding);
        }

        return settings;
    }
}
