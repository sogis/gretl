package ch.so.agi.gretl.tasks.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public abstract class Ili2dbAbstractTask extends DefaultTask {
    protected GretlLogger log = LogEnvironment.getLogger(Ili2dbAbstractTask.class);

//    /**
//     * Datenbank aus der exportiert werden soll.
//     */
//    @Input
//    public abstract ListProperty<String> getDatabase();

    /**
     * Datenbank-Datei in die importiert oder exportiert werden soll.
     */
    @OutputFile
    public abstract Property<File> getDbfile();
    
    /**
     * Entspricht der ili2db-Option `--dbschema`.
     */
    @Input
    @Optional
    public abstract Property<String> getDbschema();

    /**
     * Entspricht der ili2db-Option `--proxy`.
     */
    @Input
    @Optional
    public abstract Property<String> getProxy();

    /**
     * Entspricht der ili2db-Option `--proxyPort`.
     */
    @Input
    @Optional
    public abstract Property<Integer> getProxyPort();

    /**
     * Entspricht der ili2db-Option `--modeldir`.
     */
    @Input
    @Optional
    public abstract Property<String> getModeldir();

    /**
     * Entspricht der ili2db-Option `--models`.
     */
    @Input
    @Optional
    public abstract Property<String> getModels();

    /**
     * Entspricht der ili2db-Option `--dataset`. Darf `FileCollection` oder `List` sein.
     */
    @Input
    @Optional
    public abstract Property<Object> getDataset();

    /**
     * Entspricht der ili2db-Option `--baskets`.
     */
    @Input
    @Optional
    public abstract Property<String> getBaskets();

    /**
     * Entspricht der ili2db-Option `--topics`.
     */
    @Input
    @Optional
    public abstract Property<String> getTopics();

    /**
     * Entspricht der ili2db-Option `--importTid`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getImportTid();

    /**
     * Entspricht der ili2db-Option `--exportTid`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getExportTid();
    
    /**
     * Entspricht der ili2db-Option `--importBid`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getImportBid();
    
    /**
     * Entspricht der ili2db-Option `--preScript`.
     */
    @InputFile
    @Optional
    public abstract Property<File> getPreScript();

    /**
     * Entspricht der ili2db-Option `--postScript`.
     */
    @InputFile
    @Optional
    public abstract Property<File> getPostScript();

    /**
     * Entspricht der ili2db-Option `--deleteData`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDeleteData();

    /**
     * Entspricht der ili2db-Option `--logFile`.
     */
    @OutputFile
    @Optional
    public abstract Property<File> getLogFile();

    /**
     * Entspricht der ili2db-Option `--trace`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getTrace();

    /**
     * Entspricht der ili2db-Option `--validConfigFile`.
     */
    @InputFile
    @Optional
    public abstract Property<File> getValidConfigFile();
    
    /**
     * Entspricht der ili2db-Option `--disableValidation`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableValidation();

    /**
     * Entspricht der ili2db-Option `--disableAreaValidation`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableAreaValidation();

    /**
     * Entspricht der ili2db-Option `--forceTypeValidation`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getForceTypeValidation();

    /**
     * Entspricht der ili2db-Option `--strokeArcs`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getStrokeArcs();

    /**
     * Entspricht der ili2db-Option `--skipPolygonBuilding`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSkipPolygonBuilding();

    /**
     * Entspricht der ili2db-Option `--skipGeometryErrors`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSkipGeometryErrors();

    /**
     * Entspricht der ili2db-Option `--iligml20`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getIligml20();

    /**
     * Entspricht der ili2db-Option `--disableRounding`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableRounding();

    /**
     * Entspricht der ili2db-Option `--failOnException`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getFailOnException();

    @Input
    @Optional
    public abstract ListProperty<Integer> getDatasetSubstring();
    
    protected void run(int function, Config settings) {
//        if (!getDatabase().isPresent()) {
//            throw new IllegalArgumentException("database must not be null");
//        }

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

        try {
            File dbfile = null;
            if (getDbfile().getOrNull() != null) {
                dbfile = getDbfile().get(); 
            } else {
                throw new IllegalArgumentException("dbfile must not be null");
            }
            
            String dbFileName = this.getProject().file(dbfile).getPath();
            settings.setDbfile(dbFileName);
            settings.setDburl("jdbc:duckdb:" + settings.getDbfile());

            Ili2db.readSettingsFromDb(settings);
            Ili2db.run(settings, null);
        } catch (Exception e) {
            log.error("failed to run ili2db", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }

//        Connector database = TaskUtil.getDatabaseConnectorObject(getDatabase().get());

//        try {
//            Connection conn = database.connect();
//            if (conn == null) {
//                throw new IllegalArgumentException("connection must not be null");
//            }
//            settings.setJdbcConnection(conn);
//            Ili2db.readSettingsFromDb(settings);
//            Ili2db.run(settings, null);
//            conn.commit();
//            database.close();
//        } catch (Exception e) {
//            if (e instanceof Ili2dbException && !getFailOnException().getOrElse(true)) {
//                log.lifecycle(e.getMessage());
//                return;
//            }
//
//            log.error("failed to run ili2db", e);
//
//            throw TaskUtil.toGradleException(e);
//        } finally {
//            
//            if (!database.isClosed()) {
//                try {
//                    database.connect().rollback();
//                } catch (SQLException e) {
//                    log.error("failed to rollback", e);
//                } finally {
//                    try {
//                        database.close();
//                    } catch (SQLException e) {
//                        log.error("failed to close", e);
//                    }
//                }
//            }
//        }
    }


}
