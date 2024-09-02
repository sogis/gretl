package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.interlis.ioxwkf.dbtools.Shp2db;
import ch.interlis.ioxwkf.shp.ShapeReader;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;

public class ShpImport extends DatabaseTask {
    protected GretlLogger log;

    private Object dataFile;
    private String tableName;
    private String schemaName;
    private String encoding;
    private Integer batchSize;

    @TaskAction
    public void importData() {
        log = LogEnvironment.getLogger(ShpImport.class);
        final Connector connector = createConnector();

        if (connector == null) {
            throw new IllegalArgumentException("connector must not be null");
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
            Shp2db shp2db = new Shp2db();
            shp2db.importData(data, conn, settings);
        } catch (Exception e) {
            log.error("failed to run ShpImport", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    @InputFile
    public Object getDataFile() {
        return dataFile;
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }

    @Input
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Input
    @Optional
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @Input
    @Optional
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Input
    @Optional
    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
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
        if (batchSize != null) {
            settings.setValue(IoxWkfConfig.SETTING_BATCHSIZE, batchSize.toString());
        }

        return settings;
    }
}
