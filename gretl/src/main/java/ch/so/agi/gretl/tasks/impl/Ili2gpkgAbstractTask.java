package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import java.io.File;

public abstract class Ili2gpkgAbstractTask extends DefaultTask {
    private GretlLogger log;

    private File dbfile;
    private String proxy = null;
    private Integer proxyPort = null;
    private String modeldir = null;
    private String models = null;
    private Object dataset = null;
    private String baskets = null;
    private String topics = null;
    private Boolean importTid = false;
    private File preScript = null;
    private File postScript = null;
    private Boolean deleteData = false;
    private File logFile = null;
    private Boolean trace = false;
    private File validConfigFile = null;
    private Boolean disableValidation = false;
    private Boolean disableAreaValidation = false;
    private Boolean forceTypeValidation = false;
    private Boolean strokeArcs = false;
    private Boolean skipPolygonBuilding = false;
    private Boolean skipGeometryErrors = false;
    private Boolean iligml20 = false;

    /**
     * GeoPackage-Datei in die importiert werden soll.
     */
    @OutputFile
    public File getDbfile() {
        return dbfile;
    }

    /**
     * Entspricht der ili2gpkg Option `--proxy`
     */
    @Input
    @Optional
    public String getProxy() {
        return proxy;
    }

    /**
     * Entspricht der ili2gpkg-Option `--proxyPort`
     */
    @Input
    @Optional
    public Integer getProxyPort() {
        return proxyPort;
    }

    /**
     * Entspricht der ili2gpkg-Option `--modeldir`
     */
    @Input
    @Optional
    public String getModeldir() {
        return modeldir;
    }

    /**
     * Entspricht der ili2gpkg-Option `--models`
     */
    @Input
    @Optional
    public String getModels() {
        return models;
    }
    
    /**
     * Entspricht der ili2gpkg-Option `--dataset`. `List` oder `String`.
     */
    @Input
    @Optional
    public Object getDataset() {
        return dataset;
    }

    /**
     * Entspricht der ili2gpkg-Option `--baskets`
     */
    @Input
    @Optional
    public String getBaskets() {
        return baskets;
    }

    /**
     * Entspricht der ili2gpkg-Option `--topics`
     */
    @Input
    @Optional
    public String getTopics() {
        return topics;
    }

    /**
     * Entspricht der ili2gpkg-Option `--importTid`
     */
    @Input
    @Optional
    public Boolean isImportTid() {
        return importTid;
    }

    /**
     * Entspricht der ili2gpkg-Option `--preScript`
     */
    @InputFile
    @Optional
    public File getPreScript() {
        return preScript;
    }

    /**
     * Entspricht der ili2gpkg-Option `--postScript`
     */
    @InputFile
    @Optional
    public File getPostScript() {
        return postScript;
    }

    /**
     * Entspricht der ili2gpkg-Option `--deleteData`
     */
    @Input
    @Optional
    public Boolean isDeleteData() {
        return deleteData;
    }

    /**
     * Entspricht der ili2gpkg-Option `--logFile`
     */
    @OutputFile
    @Optional
    public File getLogFile() {
        return logFile;
    }

    /**
     * Entspricht der ili2gpkg-Option `--trace`
     */
    @Input
    @Optional
    public Boolean isTrace() {
        return trace;
    }

    /**
     * Entspricht der ili2gpkg-Option `--validConfigFile`
     */
    @InputFile
    @Optional
    public File getValidConfigFile() {
        return validConfigFile;
    }

    /**
     * Entspricht der ili2gpkg-Option `--disableValidation`
     */
    @Input
    @Optional
    public Boolean isDisableValidation() {
        return disableValidation;
    }

    /**
     * Entspricht der ili2gpkg-Option `--disableAreaValidation`
     */
    @Input
    @Optional
    public Boolean isDisableAreaValidation() {
        return disableAreaValidation;
    }

    /**
     * Entspricht der ili2gpkg-Option `--forceTypeValidation`
     */
    @Input
    @Optional
    public Boolean isForceTypeValidation() {
        return forceTypeValidation;
    }

    /**
     * Entspricht der ili2gpkg-Option `--strokeArcs`
     */
    @Input
    @Optional
    public Boolean isStrokeArcs() {
        return strokeArcs;
    }

    /**
     * Entspricht der ili2gpkg-Option `--skipPolygonBuilding`
     */
    @Input
    @Optional
    public Boolean isSkipPolygonBuilding() {
        return skipPolygonBuilding;
    }

    /**
     * Entspricht der ili2gpkg-Option `--skipGeometryErrors`
     */
    @Input
    @Optional
    public Boolean isSkipGeometryErrors() {
        return skipGeometryErrors;
    }

    /**
     * Entspricht der ili2gpkg-Option `--iligml20`
     */
    @Input
    @Optional
    public Boolean isIligml20() {
        return iligml20;
    }

    public void setDbfile(File dbfile) {
        this.dbfile = dbfile;
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

    public void setPreScript(File preScript) {
        this.preScript = preScript;
    }

    public void setPostScript(File postScript) {
        this.postScript = postScript;
    }

    public void setDeleteData(Boolean deleteData) {
        this.deleteData = deleteData;
    }

    public void setLogFile(File logFile) {
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

    protected void run(int function, Config settings) {
        log = LogEnvironment.getLogger(Ili2gpkgAbstractTask.class);

        if (dbfile == null) {
            throw new IllegalArgumentException("database must not be null");
        }

        settings.setFunction(function);

        if (proxy != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        }
        if (proxyPort != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
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
        if (preScript != null) {
            settings.setPreScript(this.getProject().file(preScript).getPath());
        }
        if (postScript != null) {
            settings.setPostScript(this.getProject().file(postScript).getPath());
        }
        if (deleteData) {
            settings.setDeleteMode(Config.DELETE_DATA);
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

        try {
            String dbFileName = this.getProject().file(dbfile).getPath();
            settings.setDbfile(dbFileName);
            settings.setDburl("jdbc:sqlite:" + settings.getDbfile());

            Ili2db.readSettingsFromDb(settings);
            Ili2db.run(settings, null);
        } catch (Exception e) {
            log.error("failed to run ili2pg", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }

    protected Config createConfig() {
        Config settings = new Config();
        new ch.ehi.ili2gpkg.GpkgMain().initConfig(settings);
        return settings;
    }
}
