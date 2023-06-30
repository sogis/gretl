package ch.so.agi.gretl.tasks;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Db2Gpkg;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class GpkgExport extends DefaultTask {
    protected GretlLogger log;
    @Input
    public Connector database;
    @InputFile
    public Object dataFile = null;
    @Input
    Object dstTableName = null;    
    @Input
    Object srcTableName = null;
    @Input
    @Optional
    public String schemaName = null;
    @Input
    @Optional
    public String encoding = null;
    @Input
    @Optional
    public Integer batchSize = null;
    @Input
    @Optional
    public Integer fetchSize = null;

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
        
        List<String> srcTableNames = null;
        if (srcTableName instanceof String) {
            srcTableNames =  new ArrayList<String>();
            srcTableNames.add((String)srcTableName);
        } else {
            srcTableNames = (List)srcTableName;
        }
        
        List<String> dstTableNames = null;
        if (dstTableName instanceof String) {
            dstTableNames =  new ArrayList<String>();
            dstTableNames.add((String)dstTableName);
        } else {
            dstTableNames = (List)dstTableName;
        }
        
        if (srcTableNames.size() != dstTableNames.size()) {
            throw new GradleException("number of source table names ("+srcTableNames.size()+") doesn't match number of destination table names ("+dstTableNames.size()+")");
        }

        java.sql.Connection conn = null;
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
                //conn.close();
                //conn = null; 
                
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
}
