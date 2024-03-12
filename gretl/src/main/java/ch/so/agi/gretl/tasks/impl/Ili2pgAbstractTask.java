package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import groovy.lang.Range;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class Ili2pgAbstractTask extends DefaultTask {
    protected GretlLogger log;


    public Connector database;

    public String dbschema = null;

    public String proxy = null;

    public Integer proxyPort = null;


    public String modeldir = null;

    public String models = null;

    public Object dataset = null;

    public String baskets = null;

    public String topics = null;

    public Boolean importTid = false;

    public Boolean exportTid = false;

    public Boolean importBid = false;

    public File preScript = null;

    public File postScript = null;

    public Boolean deleteData = false;

    public Object logFile = null;

    public Boolean trace = false;

    public File validConfigFile = null;

    public Boolean disableValidation = false;

    public Boolean disableAreaValidation = false;

    public Boolean forceTypeValidation = false;

    public Boolean strokeArcs = false;

    public Boolean skipPolygonBuilding = false;

    public Boolean skipGeometryErrors = false;

    public Boolean iligml20 = false;

    public Boolean disableRounding = false;

    public Boolean failOnException = true;

    public Range<Integer> datasetSubstring = null;

    @Input
    public Connector getDatabase() {
        return database;
    }

    @Input
    @Optional
    public String getDbschema() {
        return dbschema;
    }

    @Input
    @Optional
    public String getProxy() {
        return proxy;
    }

    @Input
    @Optional
    public Integer getProxyPort() {
        return proxyPort;
    }

    @Input
    @Optional
    public String getModeldir() {
        return modeldir;
    }

    @Input
    @Optional
    public String getModels() {
        return models;
    }

    @Input
    @Optional
    public Object getDataset() {
        return dataset;
    }

    @Input
    @Optional
    public String getBaskets() {
        return baskets;
    }

    @Input
    @Optional
    public String getTopics() {
        return topics;
    }

    @Input
    @Optional
    public Boolean isImportTid() {
        return importTid;
    }

    @Input
    @Optional
    public Boolean isExportTid() {
        return exportTid;
    }

    @Input
    @Optional
    public Boolean isImportBid() {
        return importBid;
    }

    @InputFile
    @Optional
    public File getPreScript() {
        return preScript;
    }

    @InputFile
    @Optional
    public File getPostScript() {
        return postScript;
    }

    @Input
    @Optional
    public Boolean isDeleteData() {
        return deleteData;
    }

    @OutputFile
    @Optional
    public Object getLogFile() {
        return logFile;
    }

    @Input
    @Optional
    public Boolean isTrace() {
        return trace;
    }

    @InputFile
    @Optional
    public File getValidConfigFile() {
        return validConfigFile;
    }

    @Input
    @Optional
    public Boolean isDisableValidation() {
        return disableValidation;
    }

    @Input
    @Optional
    public Boolean isDisableAreaValidation() {
        return disableAreaValidation;
    }

    @Input
    @Optional
    public Boolean isForceTypeValidation() {
        return forceTypeValidation;
    }

    @Input
    @Optional
    public Boolean isStrokeArcs() {
        return strokeArcs;
    }

    @Input
    @Optional
    public Boolean isSkipPolygonBuilding() {
        return skipPolygonBuilding;
    }

    @Input
    @Optional
    public Boolean isSkipGeometryErrors() {
        return skipGeometryErrors;
    }

    @Input
    @Optional
    public Boolean isIligml20() {
        return iligml20;
    }

    @Input
    @Optional
    public Boolean isDisableRounding() {
        return disableRounding;
    }

    @Input
    @Optional
    public Boolean isFailOnException() {
        return failOnException;
    }

    @Input
    @Optional
    public Range<Integer> getDatasetSubstring() {
        return datasetSubstring;
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

    public void setDbschema(String dbschema) {
        this.dbschema = dbschema;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setModeldir(String modeldir) {
        this.modeldir = modeldir;
    }

    public void setModels(String models) {
        this.models = models;
    }

    public void setDataset(Object dataset) {
        this.dataset = dataset;
    }

    public void setBaskets(String baskets) {
        this.baskets = baskets;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public void setImportTid(Boolean importTid) {
        this.importTid = importTid;
    }

    public void setExportTid(Boolean exportTid) {
        this.exportTid = exportTid;
    }

    public void setImportBid(Boolean importBid) {
        this.importBid = importBid;
    }

    public void setPreScript(File preScript) {
        this.preScript = preScript;
    }

    public void setPostScript(File postScript) {
        this.postScript = postScript;
    }

    public void setDeleteData(Boolean deleteData) {
        this.deleteData = deleteData;
    }

    public void setLogFile(Object logFile) {
        this.logFile = logFile;
    }

    public void setTrace(Boolean trace) {
        this.trace = trace;
    }

    public void setValidConfigFile(File validConfigFile) {
        this.validConfigFile = validConfigFile;
    }

    public void setDisableValidation(Boolean disableValidation) {
        this.disableValidation = disableValidation;
    }

    public void setDisableAreaValidation(Boolean disableAreaValidation) {
        this.disableAreaValidation = disableAreaValidation;
    }

    public void setForceTypeValidation(Boolean forceTypeValidation) {
        this.forceTypeValidation = forceTypeValidation;
    }

    public void setStrokeArcs(Boolean strokeArcs) {
        this.strokeArcs = strokeArcs;
    }

    public void setSkipPolygonBuilding(Boolean skipPolygonBuilding) {
        this.skipPolygonBuilding = skipPolygonBuilding;
    }

    public void setSkipGeometryErrors(Boolean skipGeometryErrors) {
        this.skipGeometryErrors = skipGeometryErrors;
    }

    public void setIligml20(Boolean iligml20) {
        this.iligml20 = iligml20;
    }

    public void setDisableRounding(Boolean disableRounding) {
        this.disableRounding = disableRounding;
    }

    public void setFailOnException(Boolean failOnException) {
        this.failOnException = failOnException;
    }

    public void setDatasetSubstring(Range<Integer> datasetSubstring) {
        this.datasetSubstring = datasetSubstring;
    }

    protected void run(int function, Config settings) {
        log = LogEnvironment.getLogger(Ili2pgAbstractTask.class);

        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        
        settings.setFunction(function);

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
        if (baskets != null) {
            settings.setBaskets(baskets);
        }
        if (topics != null) {
            settings.setTopics(topics);
        }
        if (importTid) {
            settings.setImportTid(true);
        }        
        if (exportTid) {
            settings.setExportTid(true);
        }
        if (importBid) {
            settings.setImportBid(true);
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
        if(function!=Config.FC_IMPORT && function!=Config.FC_UPDATE && function!=Config.FC_REPLACE) {
            if (logFile != null) {
                settings.setLogfile(this.getProject().file(logFile).getPath());
            }
        }
        if (trace) {
            EhiLogger.getInstance().setTraceFilter(false);
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
        if (disableRounding) {
            settings.setDisableRounding(true);;
        }        

        try {
            java.sql.Connection conn = database.connect();
            if (conn == null) {
                throw new IllegalArgumentException("connection must not be null");
            }
            settings.setJdbcConnection(conn);
            Ili2db.readSettingsFromDb(settings);
            Ili2db.run(settings, null);
            conn.commit();
            database.close();
        } catch (Exception e) {
            if (e instanceof Ili2dbException && !failOnException) {
                log.lifecycle(e.getMessage());
                return;
            }

            log.error("failed to run ili2pg", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        } finally {
            
            if (!database.isClosed()) {
                try {
                    database.connect().rollback();
                } catch (SQLException e) {
                    log.error("failed to rollback", e);
                }finally {
                    try {
                        database.close();
                    } catch (SQLException e) {
                        log.error("failed to close", e);
                    }
                }
            }
        }
    }

    protected Config createConfig() {
        Config settings = new Config();
        new ch.ehi.ili2pg.PgMain().initConfig(settings);
        return settings;
    }
}
