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
    String dstTableName = null;    
    @Input
    String srcTableName = null;
    @Input
    @Optional
    public String schemaName = null;
    @Input
    @Optional
    public String encoding = null;

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
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_DBTABLE, srcTableName);
        settings.setValue(IoxWkfConfig.SETTING_GPKGTABLE, dstTableName);        
        // set optional parameters
        if (schemaName != null) {
            settings.setValue(IoxWkfConfig.SETTING_DBSCHEMA, schemaName);
        }

        File data = this.getProject().file(dataFile);
        java.sql.Connection conn = null;
        try {
            conn = database.connect();
            if (conn == null) {
                throw new IllegalArgumentException("connection must not be null");
            }
            Db2Gpkg db2shp = new Db2Gpkg();
            db2shp.exportData(data, conn, settings);
            conn.commit();
            conn.close();
            conn = null;
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
