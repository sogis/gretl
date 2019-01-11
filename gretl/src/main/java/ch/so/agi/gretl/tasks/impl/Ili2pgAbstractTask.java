package ch.so.agi.gretl.tasks.impl;


import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.SQLException;


public abstract class Ili2pgAbstractTask extends DefaultTask {
    protected GretlLogger log;

    @Input
    public Connector database;
    @Input
    @Optional
    public String dbschema = null;
    @Input
    @Optional
    public String proxy = null;
    @Input
    @Optional
    public Integer proxyPort = null;
    
    @Input
    @Optional
    public String modeldir = null;
    @Input
    @Optional
    public String models = null;
    @Input
    @Optional
    public String dataset = null;
    @Input
    @Optional
    public String baskets = null;
    @Input
    @Optional
    public String topics = null;
    @InputFile
    @Optional
    public File preScript = null;
    @InputFile
    @Optional
    public File postScript = null;
    @Input
    @Optional
    public boolean deleteData = false;
    @OutputFile
    @Optional
    public Object logFile = null;
    @InputFile
    @Optional
    public File validConfigFile = null;
    @Input
    @Optional
    public boolean disableValidation = false;
    @Input
    @Optional
    public boolean disableAreaValidation = false;
    @Input
    @Optional
    public boolean forceTypeValidation = false;
    @Input
    @Optional
    public boolean strokeArcs = false;
    @Input
    @Optional
    public boolean skipPolygonBuilding = false;
    @Input
    @Optional
    public boolean skipGeometryErrors = false;
    @Input
    @Optional
    public boolean iligml20 = false;

    protected void run(int function,Config settings) {
        log = LogEnvironment.getLogger(Ili2pgAbstractTask.class);

        if (database==null) {
            throw new IllegalArgumentException("database must not be null");
        }
        
        settings.setFunction(function);
        
        String xtfFilename=settings.getXtffile();
        if (proxy != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        }
        if (proxyPort != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
        }

        if (dbschema != null) {
            settings.setDbschema(dbschema);
        }
        if (modeldir != null) {
            settings.setModeldir(modeldir);
        }
        if (models != null) {
            settings.setModels(models);
        }
        if (dataset != null) {
            settings.setDatasetName(dataset);
        }
        if (baskets != null) {
            settings.setBaskets(baskets);
        }
        if (topics != null) {
            settings.setTopics(topics);
        }
        if (preScript != null) {
            settings.setPreScript(this.getProject().file(preScript).getPath());
        }
        if (postScript != null) {
            settings.setPostScript(this.getProject().file(postScript).getPath());
        }
        if (deleteData) {
            settings.setDeleteMode(Config.DELETE_DATA);
        }
        if (logFile != null) {
            settings.setLogfile(this.getProject().file(logFile).getPath());
        }
        if (validConfigFile != null) {
            settings.setValidConfigFile(this.getProject().file(validConfigFile).getPath());
        }
        if (disableValidation) {
            settings.setValidation(false);
        }
        if (disableAreaValidation) {
            settings.setDisableAreaValidation(true);
        }
        if (forceTypeValidation) {
            settings.setOnlyMultiplicityReduction(true);
        }
        if (strokeArcs) {
            settings.setStrokeArcs(settings.STROKE_ARCS_ENABLE);
        }
        if (skipPolygonBuilding) {
            Ili2db.setSkipPolygonBuilding(settings);
        }
        if (skipGeometryErrors) {
            settings.setSkipGeometryErrors(true);
        }
        if (iligml20) {
            settings.setTransferFileFormat(Config.ILIGML20);
        }
            
        java.sql.Connection conn=null;
        try {
            conn=database.connect();
            if(conn==null) {
                throw new IllegalArgumentException("connection must not be null");
            }
            settings.setJdbcConnection(conn);
            Ili2db.readSettingsFromDb(settings);
            Ili2db.run(settings, null);
            conn.commit();
            conn.close();
            conn=null;
        } catch (Exception e) {
            log.error("failed to run ili2pg", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }finally {
            if(conn!=null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException e) {
                    log.error("failed to rollback/close", e);
                }
                conn=null;
            }
        }
    }
    protected Config createConfig() {
        Config settings=new Config();
        new ch.ehi.ili2pg.PgMain().initConfig(settings);
        return settings;
        
    }
}

