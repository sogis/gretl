package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Gpkg2db;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;
import java.util.List;

public class GpkgImport extends DefaultTask {
    private GretlLogger log;
    
    private Connector database;
    private File dataFile;
    private String srcTableName;
    private String dstTableName;
    private String schemaName;
    private Integer batchSize;
    private Integer fetchSize;

    /**
     * Name der GeoPackage-Datei, die gelesen werden soll.
     */
    @InputFile
    public File getDataFile(){
        return dataFile;
    }

    /**
     * Name der GeoPackage-Tabelle, die importiert werden soll.
     */
    @Input
    String getSrcTableName() {
        return srcTableName;
    }

    /**
     * Name der DB-Tabelle, in die importiert werden soll.
     */
    @Input
    public String getDstTableName(){
        return dstTableName;
    }

    /**
     * Name des DB-Schemas, in dem die DB-Tabelle ist.
     */
    @Input
    @Optional
    public String getSchemaName(){
      return schemaName;
    }
    
    /**
     * Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden. Default: 5000
     */
    @Input
    @Optional
    public Integer getBatchSize(){
        return batchSize;
    }

    /**
     * Anzahl der Records, die pro Fetch aus der Quell-Datenbank gelesen werden. Default: 5000
     */
    @Input
    @Optional
    public Integer getFetchSize(){
        return fetchSize;
    }

    /**
     * Datenbank, in die importiert werden soll.
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

    public void setSrcTableName(String srcTableName) {
        this.srcTableName = srcTableName;
    }

    public void setDstTableName(String dstTableName) {
        this.dstTableName = dstTableName;
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
    public void importData() {
        log = LogEnvironment.getLogger(GpkgImport.class);

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
            throw new IllegalArgumentException("dataFile must not be null");
        }

        Settings settings = getSettings();

        File data = this.getProject().file(dataFile);

        try (Connection conn = database.connect()) {
            Gpkg2db gpkg2db = new Gpkg2db();
            gpkg2db.importData(data, conn, settings);
            conn.commit();
        } catch (Exception e) {
            log.error("failed to run GpkgImport", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    private Settings getSettings() {
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_GPKGTABLE, srcTableName);
        settings.setValue(IoxWkfConfig.SETTING_DBTABLE, dstTableName);

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

        return settings;
    }
}
