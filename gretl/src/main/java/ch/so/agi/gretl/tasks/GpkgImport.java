package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Gpkg2db;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.DatabaseTask;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;

public class GpkgImport extends DatabaseTask {
    protected GretlLogger log;
    private Object dataFile;
    private String srcTableName;
    private String dstTableName;
    private String schemaName;
    private Integer batchSize;
    private Integer fetchSize;

    @TaskAction
    public void importData() {
        log = LogEnvironment.getLogger(GpkgImport.class);
        Connector connector = createConnector();

        if (connector == null) {
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

        try (Connection conn = connector.connect()) {
            Gpkg2db gpkg2db = new Gpkg2db();
            gpkg2db.importData(data, conn, settings);
            conn.commit();
            // conn.rollback()
        } catch (Exception e) {
            log.error("failed to run GpkgImport", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    @InputFile
    public Object getDataFile(){
        return dataFile;
    }

    @Input
    String getSrcTableName() {
        return srcTableName;
    }

    @Input
    public String getDstTableName(){
        return dstTableName;
    }

    @Input
    @Optional
    public String getSchemaName(){
      return schemaName;
    }

    @Input
    @Optional
    public Integer getBatchSize(){
        return batchSize;
    }

    @Input
    @Optional
    public Integer getFetchSize(){
        return fetchSize;
    }

    public void setDataFile(Object dataFile) {
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
