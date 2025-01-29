package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import java.io.File;
import java.sql.SQLException;

public abstract class Ili2pgAbstractTask extends DefaultTask {
    protected GretlLogger log = LogEnvironment.getLogger(Ili2pgAbstractTask.class);

    /**
     * Datenbank aus der exportiert werden soll.
     */
    @Input
    public abstract ListProperty<String> getDatabase();

    /**
     * Entspricht der ili2pg-Option `--dbschema`.
     */
    @Input
    @Optional
    public abstract Property<String> getDbschema();

    /**
     * Entspricht der ili2pg-Option `--proxy`.
     */
    @Input
    @Optional
    public abstract Property<String> getProxy();

    /**
     * Entspricht der ili2pg-Option `--proxyPort`.
     */
    @Input
    @Optional
    public abstract Property<Integer> getProxyPort();

    /**
     * Entspricht der ili2pg-Option `--modeldir`.
     */
    @Input
    @Optional
    public abstract Property<String> getModeldir();

    /**
     * Entspricht der ili2pg-Option `--models`.
     */
    @Input
    @Optional
    public abstract Property<String> getModels();

    /**
     * Entspricht der ili2pg-Option `--dataset`.
     */
    @Input
    @Optional
    public abstract Property<Object> getDataset();

    /**
     * Entspricht der ili2pg-Option `--baskets`.
     */
    @Input
    @Optional
    public abstract Property<String> getBaskets();

    /**
     * Entspricht der ili2pg-Option `--topics`.
     */
    @Input
    @Optional
    public abstract Property<String> getTopics();

    /**
     * Entspricht der ili2pg-Option `--importTid`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getImportTid();

    /**
     * Entspricht der ili2pg-Option `--exportTid`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getExportTid();
    
    /**
     * Entspricht der ili2pg-Option `--importBid`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getImportBid();
    
    /**
     * Entspricht der ili2pg-Option `--preScript`.
     */
    @InputFile
    @Optional
    public abstract Property<File> getPreScript();

    /**
     * Entspricht der ili2pg-Option `--postScript`.
     */
    @InputFile
    @Optional
    public abstract Property<File> getPostScript();

    /**
     * Entspricht der ili2pg-Option `--deleteData`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDeleteData();

    /**
     * Entspricht der ili2pg-Option `--logFile`.
     */
    @OutputFile
    @Optional
    public abstract Property<Object> getLogFile();

    /**
     * Entspricht der ili2pg-Option `--trace`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getTrace();

    /**
     * Entspricht der ili2pg-Option `--validConfigFile`.
     */
    @InputFile
    @Optional
    public abstract Property<File> getValidConfigFile();
    
    /**
     * Entspricht der ili2pg-Option `--disableValidation`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableValidation();

    /**
     * Entspricht der ili2pg-Option `--disableAreaValidation`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableAreaValidation();

    /**
     * Entspricht der ili2pg-Option `--forceTypeValidation`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getForceTypeValidation();

    /**
     * Entspricht der ili2pg-Option `--strokeArcs`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getStrokeArcs();

    /**
     * Entspricht der ili2pg-Option `--skipPolygonBuilding`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSkipPolygonBuilding();

    /**
     * Entspricht der ili2pg-Option `--skipGeometryErrors`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSkipGeometryErrors();

    /**
     * Entspricht der ili2pg-Option `--iligml20`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getIligml20();

    /**
     * Entspricht der ili2pg-Option `--disableRounding`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableRounding();

    /**
     * Entspricht der ili2pg-Option `--failOnException`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getFailOnException();

    /**
     * Entspricht der ili2pg-Option `--datasetSubstring`.
     */
    @Input
    @Optional
    public abstract ListProperty<Integer> getDatasetSubstring();

    protected void run(int function, Config settings) {
        if (!getDatabase().isPresent()) {
            throw new IllegalArgumentException("database must not be null");
        }

        settings.setFunction(function);

        if (getProxy().isPresent()) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, getProxy().get());
        }
        if (getProxyPort().isPresent()) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, getProxyPort().get().toString());
        }

        if (getDbschema().isPresent()) {
            settings.setDbschema(getDbschema().get());
        }
        if (getModeldir().isPresent()) {
            settings.setModeldir(getModeldir().get());
        }
        if (getModels().isPresent()) {
            settings.setModels(getModels().get());
        }
        if (getBaskets().isPresent()) {
            settings.setBaskets(getBaskets().get());
        }
        if (getTopics().isPresent()) {
            settings.setTopics(getTopics().get());
        }
        if (getImportTid().getOrElse(false)) {
            settings.setImportTid(true);
        }        
        if (getExportTid().getOrElse(false)) {
            settings.setExportTid(true);
        }
        if (getImportBid().getOrElse(false)) {
            settings.setImportBid(true);
        }
        if (getPreScript().isPresent()) {
            settings.setPreScript(this.getProject().file(getPreScript().get()).getPath());
        }
        if (getPostScript().isPresent()) {
            settings.setPostScript(this.getProject().file(getPostScript().get()).getPath());
        }
        if (getDeleteData().getOrElse(false)) {
            settings.setDeleteMode(Config.DELETE_DATA);
        }
        if(function!=Config.FC_IMPORT && function!=Config.FC_UPDATE && function!=Config.FC_REPLACE) {
            if (getLogFile().isPresent()) {
                settings.setLogfile(this.getProject().file(getLogFile().get()).getPath());
            }
        }
        if (getTrace().getOrElse(false)) {
            EhiLogger.getInstance().setTraceFilter(false);
        }
        if (getValidConfigFile().isPresent()) {
            settings.setValidConfigFile(this.getProject().file(getValidConfigFile().get()).getPath());
        }
        if (getDisableValidation().getOrElse(false)) {
            settings.setValidation(false);
        }
        if (getDisableAreaValidation().getOrElse(false)) {
            settings.setDisableAreaValidation(true);
        }
        if (getForceTypeValidation().getOrElse(false)) {
            settings.setOnlyMultiplicityReduction(true);
        }
        if (getStrokeArcs().getOrElse(false)) {
            settings.setStrokeArcs(settings.STROKE_ARCS_ENABLE);
        }
        if (getSkipPolygonBuilding().getOrElse(false)) {
            Ili2db.setSkipPolygonBuilding(settings);
        }
        if (getSkipGeometryErrors().getOrElse(false)) {
            settings.setSkipGeometryErrors(true);
        }
        if (getIligml20().getOrElse(false)) {
            settings.setTransferFileFormat(Config.ILIGML20);
        }
        if (getDisableRounding().getOrElse(false)) {
            settings.setDisableRounding(true);
        }

        Connector database = TaskUtil.getDatabaseConnectorObject(getDatabase().get());

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
            if (e instanceof Ili2dbException && !getFailOnException().getOrElse(true)) {
                log.lifecycle(e.getMessage());
                return;
            }

            log.error("failed to run ili2pg", e);

            throw TaskUtil.toGradleException(e);
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
