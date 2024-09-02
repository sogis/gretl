package ch.so.agi.gretl.tasks;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.interlis.ioxwkf.dbtools.Shp2db;
import ch.interlis.ioxwkf.shp.ShapeReader;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class ShpImport extends DefaultTask {
    protected GretlLogger log;

    private Connector database;
    private Object dataFile = null;
    private String tableName = null;
    private String schemaName = null;
    private String encoding = null;
    private Integer batchSize = null;


    @Input
    public Connector getDatabase() {
        return database;
    }

    public void setDatabase(List<String> databaseDetails){
        if (databaseDetails.size() != 3) {
            throw new IllegalArgumentException("Values for db_uri, db_user, db_pass are required.");
        }

        String databaseUri = databaseDetails.get(0);
        String databaseUser = databaseDetails.get(1);
        String databasePassword = databaseDetails.get(2);

        this.database = new Connector(databaseUri, databaseUser, databasePassword);
    }

    @InputFile
    public Object getDataFile() {
        return dataFile;
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }

    @Input
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Input
    @Optional
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @Input
    @Optional
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Input
    @Optional
    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    @TaskAction
    public void importData() {
        log = LogEnvironment.getLogger(ShpImport.class);
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
        if (batchSize != null) {
            settings.setValue(IoxWkfConfig.SETTING_BATCHSIZE, batchSize.toString());
        }
        File data = this.getProject().file(dataFile);
        java.sql.Connection conn = null;
        try {
            conn = database.connect();
            if (conn == null) {
                throw new IllegalArgumentException("connection must not be null");
            }
            Shp2db shp2db = new Shp2db();
            shp2db.importData(data, conn, settings);
            conn.commit();
            conn.close();
            conn = null;
        } catch (Exception e) {
            log.error("failed to run ShpImport", e);
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