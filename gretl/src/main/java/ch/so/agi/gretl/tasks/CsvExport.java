package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.Db2Csv;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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

    /**
     * Name der CSV-Datei, die erstellt werden soll.
     */
    @OutputFile
    public Object getDataFile() {
        return dataFile;
    }

    /**
     * Name der DB-Tabelle, die exportiert werden soll.
     */
    @Input
    public String getTableName() {
        return tableName;
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
     * Name des DB-Schemas, in dem die DB-Tabelle ist.
     */
    @Input
    @Optional
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Spalten der DB-Tabelle, die exportiert werden sollen. Definiert die Reihenfolge der Spalten in der CSV-Datei. Default: alle Spalten
     */
    @Input
    @Optional
    public String[] getAttributes(){
        return attributes;
    }

    /**
     * Zeichencodierung der CSV-Datei, z.B. "UTF-8". Default: Systemeinstellung
     */
    @Input
    @Optional
    public String getEncoding(){
        return encoding;
    }

    /**
     * Datenbank aus der exportiert werden soll.
     */
    @Input
    public Connector getDatabase() {
        return database;
    }

    public void setDatabase(List<String> databaseDetails) {
        this.database = TaskUtil.getDatabaseConnectorObject(databaseDetails);
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

        Settings settings = getSettings();
        File data = this.getProject().file(dataFile);
        Connection conn = null;
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
            throw TaskUtil.toGradleException(e);
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

    private Settings getSettings() {
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

        return settings;
    }
}
