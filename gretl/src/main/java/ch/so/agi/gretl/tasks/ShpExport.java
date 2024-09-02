package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Db2Shp;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.interlis.ioxwkf.shp.ShapeReader;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;

public class ShpExport extends DatabaseTask {
    protected GretlLogger log;
    private Object dataFile;
    private String tableName;
    private String schemaName;
    private String encoding;

    @TaskAction
    public void exportData() {
        log = LogEnvironment.getLogger(ShpExport.class);
        final Connector connector = createConnector();

        if (connector == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null");
        }
        if (dataFile == null) {
            return;
        }
        Settings settings = getSettings();

        File data = this.getProject().file(dataFile);

        try (Connection conn = connector.connect()) {
            Db2Shp db2shp = new Db2Shp();
            db2shp.exportData(data, conn, settings);
        } catch (Exception e) {
            log.error("failed to run ShpExport", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    @OutputFile
    public Object getDataFile() {
        return dataFile;
    }

    @Input
    public String getTableName() {
        return tableName;
    }

    @Input
    @Optional
    public String getSchemaName() {
        return schemaName;
    }

    @Input
    @Optional
    public String getEncoding() {
        return encoding;
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    private Settings getSettings() {
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_DBTABLE, tableName);

        // set optional parameters
        if (schemaName != null) {
            settings.setValue(IoxWkfConfig.SETTING_DBSCHEMA, schemaName);
        }
        if (encoding != null) {
            settings.setValue(ShapeReader.ENCODING, encoding);
        }

        return settings;
    }
}
