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
    protected GretlLogger log;

    @InputFile
    public Object dbfile;
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
    public Object dataset = null;
    @Input
    @Optional
    public String baskets = null;
    @Input
    @Optional
    public String topics = null;
    @Input
    @Optional
    public boolean importTid = false;
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
    @Input
    @Optional
    public boolean trace = false;
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
