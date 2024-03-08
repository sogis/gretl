package ch.so.agi.gretl.tasks;

import java.io.File;
import java.sql.SQLException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.Db2Csv;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class CsvExport extends DefaultTask {
    protected GretlLogger log;
    private Connector database;
    private Object dataFile = null;
    private String tableName = null;

    private Boolean firstLineIsHeader = true;

    private Character valueDelimiter = null;

    private Character valueSeparator = null;

    private String schemaName = null;

    private String[] attributes = null;

    private String encoding = null;

    @Input
    public Connector getDatabase() {
        return database;
    }
    @InputFile
    public Object getDataFile() {
        return dataFile;
    }
    @Input
    public String getTableName() {
        return tableName;
    }
    @Input
    @Optional
    public Boolean isFirstLineIsHeader() {
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

    @Optional
    public String[] getAttributes(){
        return attributes;
    }

    @Optional
    public String getEncoding(){
        return encoding;
    }

    @TaskAction
    public void exportData() {
        log = LogEnvironment.getLogger(CsvExport.class);
        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null");
        }
        if (dataFile == null) {
            return;
        }
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_DBTABLE, tableName);
        // set optional parameters
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE,
                firstLineIsHeader ? IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER : IoxWkfConfig.SETTING_FIRSTLINE_AS_VALUE);
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

        File data = this.getProject().file(dataFile);
        java.sql.Connection conn = null;
        try {
            conn = database.connect();
            if (conn == null) {
                throw new IllegalArgumentException("connection must not be null");
            }
            Db2Csv db2csv = new Db2Csv();
            if (attributes != null) {
                db2csv.setAttributes(attributes);
            }
            db2csv.exportData(data, conn, settings);
            conn.commit();
            conn.close();
            conn = null;
        } catch (Exception e) {
            log.error("failed to run CsvExport", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        } finally {
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException e) {
                    log.error("failed to rollback/close", e);
                }
                conn = null;
            }
        }
    }
}
