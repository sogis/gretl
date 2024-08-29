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
import ch.interlis.ioxwkf.dbtools.Gpkg2db;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class GpkgImport extends DatabaseTask {
    protected GretlLogger log;
    private Object dataFile;
    private String srcTableName;
    private String dstTableName;
    private String schemaName;
    private Integer batchSize;
    private Integer fetchSize;

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

    @TaskAction
    public void importData() {
        log = LogEnvironment.getLogger(GpkgImport.class);
        createConnector();

        if (srcTableName == null) {
            throw new IllegalArgumentException("srcTableName must not be null");
        }
        if (dstTableName == null) {
            throw new IllegalArgumentException("dstTableName must not be null");
        }        
        if (dataFile == null) {
            throw new IllegalArgumentException("dataFile must not be null");
        }

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

        File data = this.getProject().file(dataFile);
        java.sql.Connection conn = null;
        try {
            conn = getDatabase().connect();
            if (conn == null) {
                throw new IllegalArgumentException("connection must not be null");
            }
            
            Gpkg2db gpkg2db=new Gpkg2db();
            gpkg2db.importData(data, conn, settings);
            conn.commit();
            conn.close();
            conn = null;
        } catch (Exception e) {
            log.error("failed to run GpkgImport", e);
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
