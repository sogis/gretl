package ch.so.agi.gretl.tasks;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.*;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Db2Gpkg;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class GpkgExport extends DefaultTask {
    private GretlLogger log;
    
    private Connector database;
    private File dataFile;
    private Object dstTableName;
    private Object srcTableName;
    private String schemaName;
    private Integer batchSize;
    private Integer fetchSize;

    /**
     * Name der GeoPackage-Datei, die erstellt werden soll.
     */
    @OutputFile
    public File getDataFile() {
        return dataFile;
    }

    /**
     * Name der Tabelle(n) in der GeoPackage-Datei. `String` oder `List`.
     */
    @Input
    public Object getDstTableName() {
        return dstTableName;
    }

    /**
     * Name der DB-Tabelle(n), die exportiert werden soll(en). `String` oder `List`.
     */
    @Input
    public Object getSrcTableName() {
        return srcTableName;
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
     * Anzahl der Records, die pro Batch in die Ziel-Datenbank (GeoPackage) geschrieben werden. Default: 5000
     */
    @Input
    @Optional
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Anzahl der Records, die pro Fetch aus der Quell-Datenbank gelesen werden. Default: 5000
     */
    @Input
    @Optional
    public Integer getFetchSize() {
        return fetchSize;
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


    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

    public void setDstTableName(Object dstTableName) {
        this.dstTableName = dstTableName;
    }

    public void setSrcTableName(Object srcTableName) {
        this.srcTableName = srcTableName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public void setFetchSize(Integer fetchSize) {
        this.fetchSize = fetchSize;
    }

    @TaskAction
    public void exportData() {
        log = LogEnvironment.getLogger(GpkgExport.class);

        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        if (srcTableName == null) {
            throw new IllegalArgumentException("srcTableName must not be null");
        }
        if (dstTableName == null) {
            throw new IllegalArgumentException("dstTableName must not be null");
        }
        if (dataFile == null) {
            return;
        }

        List<String> srcTableNames = getSrcTableNames();
        List<String> dstTableNames = getDstTableNames();

        if (srcTableNames.size() != dstTableNames.size()) {
            throw new GradleException("number of source table names ("+srcTableNames.size()+") doesn't match number of destination table names ("+dstTableNames.size()+")");
        }

        Connection conn = null;
        try {
            conn = database.connect();
            if (conn == null) {
                throw new IllegalArgumentException("connection must not be null");
            }

            int i=0;
            for (String srcTableName : srcTableNames) {
                String dstTableName = dstTableNames.get(i);

                Settings settings = new Settings();
                settings.setValue(IoxWkfConfig.SETTING_DBTABLE, srcTableName);
                settings.setValue(IoxWkfConfig.SETTING_GPKGTABLE, dstTableName);
                // set optional parameters
                if (schemaName != null) {
                    settings.setValue(IoxWkfConfig.SETTING_DBSCHEMA, schemaName);
                }
                if (batchSize != null) {
                    settings.setValue(IoxWkfConfig.SETTING_BATCHSIZE, batchSize.toString());
                }
                if (fetchSize != null) {
                    settings.setValue(IoxWkfConfig.SETTING_FETCHSIZE, fetchSize.toString());
                }

                File data = this.getProject().file(dataFile);

                Db2Gpkg db2gpkg = new Db2Gpkg();
                db2gpkg.exportData(data, conn, settings);
                conn.commit();
                i++;
            }
        } catch (Exception e) {
            log.error("failed to run GpkgExport", e);
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

    List<String> getSrcTableNames() {
        List<String> srcTableNames;

        if (srcTableName instanceof String) {
            srcTableNames = new ArrayList<>();
            srcTableNames.add((String)srcTableName);
        } else {
            srcTableNames = (List)srcTableName;
        }

        return srcTableNames;
    }

    List<String> getDstTableNames() {
        List<String> dstTableNames;
        if (dstTableName instanceof String) {
            dstTableNames =  new ArrayList<>();
            dstTableNames.add((String)dstTableName);
        } else {
            dstTableNames = (List)dstTableName;
        }

        return dstTableNames;
    }
}
