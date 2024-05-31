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
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public abstract class Ili2pgAbstractTask extends DefaultTask {
    protected GretlLogger log;

    @Input
    public abstract Property<List<String>> getDatabase();

    @Input
    @Optional
    public abstract Property<String> getDbschema();

    @Input
    @Optional
    public abstract Property<String> getProxy();

    @Input
    @Optional
    public abstract Property<Integer> getProxyPort();

    @Input
    @Optional
    public abstract Property<String> getModeldir();

    @Input
    @Optional
    public abstract Property<String> getModels();

    @Input
    @Optional
    public abstract Property<Object> getDataset();

    @Input
    @Optional
    public abstract Property<String> getBaskets();

    @Input
    @Optional
    public abstract Property<String> getTopics();

    @Input
    @Optional
    public abstract Property<Boolean> getIsImportTid();

    @Input
    @Optional
    public abstract Property<Boolean> getIsExportTid();
    @Input
    @Optional
    public abstract Property<Boolean> getIsImportBid();
    @InputFile
    @Optional
    public abstract Property<File> getPreScript();

    @InputFile
    @Optional
    public abstract Property<File> getPostScript();

    @Input
    @Optional
    public abstract Property<Boolean> getIsDeleteData();

    @OutputFile
    @Optional
    public abstract Property<Object> getLogFile();

    @Input
    @Optional
    public abstract Property<Boolean> getIsTrace();

    @InputFile
    @Optional
    public abstract Property<File> getValidConfigFile();
    @Input
    @Optional
    public abstract Property<Boolean> getIsDisableValidation();

    @Input
    @Optional
    public abstract Property<Boolean> getIsDisableAreaValidation();

    @Input
    @Optional
    public abstract Property<Boolean> getIsForceTypeValidation();

    @Input
    @Optional
    public abstract Property<Boolean> getIsStrokeArcs();

    @Input
    @Optional
    public abstract Property<Boolean> getIsSkipPolygonBuilding();

    @Input
    @Optional
    public abstract Property<Boolean> getIsSkipGeometryErrors();

    @Input
    @Optional
    public abstract Property<Boolean> getIsIligml20();

    @Input
    @Optional
    public abstract Property<Boolean> getIsDisableRounding();

    @Input
    @Optional
    public abstract Property<Boolean> getIsFailOnException();

    @Input
    @Optional
    public abstract Property<Range<Integer>> getDatasetSubstring();

    protected void run(int function, Config settings) {
        log = LogEnvironment.getLogger(Ili2pgAbstractTask.class);

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
        if (getIsImportTid().get()) {
            settings.setImportTid(true);
        }        
        if (getIsExportTid().get()) {
            settings.setExportTid(true);
        }
        if (getIsImportBid().get()) {
            settings.setImportBid(true);
        }
        if (getPreScript().isPresent()) {
            settings.setPreScript(this.getProject().file(getPreScript().get()).getPath());
        }
        if (getPostScript().isPresent()) {
            settings.setPostScript(this.getProject().file(getPostScript().get()).getPath());
        }
        if (getIsDeleteData().get()) {
            settings.setDeleteMode(Config.DELETE_DATA);
        }
        if(function!=Config.FC_IMPORT && function!=Config.FC_UPDATE && function!=Config.FC_REPLACE) {
            if (getLogFile().isPresent()) {
                settings.setLogfile(this.getProject().file(getLogFile().get()).getPath());
            }
        }
        if (getIsTrace().get()) {
            EhiLogger.getInstance().setTraceFilter(false);
        }
        if (getValidConfigFile().isPresent()) {
            settings.setValidConfigFile(this.getProject().file(getValidConfigFile().get()).getPath());
        }
        if (getIsDisableValidation().get()) {
            settings.setValidation(false);
        }
        if (getIsDisableAreaValidation().get()) {
            settings.setDisableAreaValidation(true);
        }
        if (getIsForceTypeValidation().get()) {
            settings.setOnlyMultiplicityReduction(true);
        }
        if (getIsStrokeArcs().get()) {
            settings.setStrokeArcs(settings.STROKE_ARCS_ENABLE);
        }
        if (getIsSkipPolygonBuilding().get()) {
            Ili2db.setSkipPolygonBuilding(settings);
        }
        if (getIsSkipGeometryErrors().get()) {
            settings.setSkipGeometryErrors(true);
        }
        if (getIsIligml20().get()) {
            settings.setTransferFileFormat(Config.ILIGML20);
        }
        if (getIsDisableRounding().get()) {
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
            if (e instanceof Ili2dbException && !getIsFailOnException().get()) {
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
