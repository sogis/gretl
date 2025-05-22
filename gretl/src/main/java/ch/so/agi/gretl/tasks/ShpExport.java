package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Db2Shp;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.interlis.ioxwkf.shp.ShapeReader;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ShpExport extends DefaultTask {
    protected GretlLogger log;
    
    private Connector database;
    private File dataFile = null;
    private String tableName = null;
    private String schemaName = null;
    private String encoding = null;

    /**
     * Name der SHP-Datei, die erstellt werden soll.
     */
    @OutputFile
    public File getDataFile() {
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
     * Name des DB-Schemas, in dem die DB-Tabelle ist.
     */
    @Input
    @Optional
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Zeichencodierung der SHP-Datei, z.B. `UTF-8`. Default: Systemeinstellung
     */
    @Input
    @Optional
    public String getEncoding() {
        return encoding;
    }

    /**
     * Datenbank, aus der exportiert werden soll.
     */
    @Input
    public Connector getDatabase() {
        return database;
    }

    public void setDatabase(List<String> databaseDetails) {
        this.database = TaskUtil.getDatabaseConnectorObject(databaseDetails);
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @TaskAction
    public void exportData() {
        log = LogEnvironment.getLogger(ShpExport.class);

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
        if (schemaName != null) {
            settings.setValue(IoxWkfConfig.SETTING_DBSCHEMA, schemaName);
        }
        if (encoding != null) {
            settings.setValue(ShapeReader.ENCODING, encoding);
        }

        File data = this.getProject().file(dataFile);
        Connection conn = null;
        try {
            conn = database.connect();
            if (conn == null) {
                throw new IllegalArgumentException("connection must not be null");
            }
            Db2Shp db2shp = new Db2Shp();
            db2shp.exportData(data, conn, settings);
            conn.commit();
            conn.close();
            conn = null;
        } catch (Exception e) {
            log.error("failed to run ShpExport", e);
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
}
