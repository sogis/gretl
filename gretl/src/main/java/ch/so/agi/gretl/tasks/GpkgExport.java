package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Db2Gpkg;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.*;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class GpkgExport extends DefaultTask {
    protected GretlLogger log;
    private Connector database;
    private Object dataFile;
    private Object dstTableName;
    private Object srcTableName;
    private String schemaName;
    private String encoding;
    private Integer batchSize;
    private Integer fetchSize;

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

        List<String> srcTableNames = getSrcTableNames();
        List<String> dstTableNames = getDstTableNames();

        if (srcTableNames.size() != dstTableNames.size()) {
            throw new GradleException("number of source table names ("+srcTableNames.size()+") doesn't match number of destination table names ("+dstTableNames.size()+")");
        }

        try (Connection conn = database.connect()) {
            int i = 0;
            for (String srcTableName : srcTableNames) {
                String dstTableName = dstTableNames.get(i);

                Settings settings = new Settings();
                settings.setValue(IoxWkfConfig.SETTING_DBTABLE, srcTableName);
                settings.setValue(IoxWkfConfig.SETTING_GPKGTABLE, dstTableName);
                // set optional parameters
                if (schemaName != null) {
                    settings.setValue(IoxWkfConfig.SETTING_DBSCHEMA, schemaName);
                }
                if (batchSize != null) {
                    settings.setValue(IoxWkfConfig.SETTING_BATCHSIZE, batchSize.toString());
                }
                if (fetchSize != null) {
                    settings.setValue(IoxWkfConfig.SETTING_FETCHSIZE, fetchSize.toString());
                }

                File data = this.getProject().file(dataFile);

                Db2Gpkg db2gpkg = new Db2Gpkg();
                db2gpkg.exportData(data, conn, settings);
                conn.commit();
                i++;
            }
        } catch (Exception e) {
            log.error("failed to run GpkgExport", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    @OutputFile
    public Object getDataFile() {
        return dataFile;
    }

    @Input
    public Object getDstTableName() {
        return dstTableName;
    }

    @Input
    public Object getSrcTableName() {
        return srcTableName;
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

    @Input
    @Optional
    public Integer getBatchSize() {
        return batchSize;
    }

    @Input
    @Optional
    public Integer getFetchSize() {
        return fetchSize;
    }

    @Input
    public Connector getDatabase() {
        return database;
    }

    public void setDatabase(List<String> databaseDetails) {
        this.database = TaskUtil.getDatabaseConnectorObject(databaseDetails);
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }

    public void setDstTableName(Object dstTableName) {
        this.dstTableName = dstTableName;
    }

    public void setSrcTableName(Object srcTableName) {
        this.srcTableName = srcTableName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public void setFetchSize(Integer fetchSize) {
        this.fetchSize = fetchSize;
    }

    @Internal
    List<String> getSrcTableNames() {
        List<String> srcTableNames;

        if (srcTableName instanceof String) {
            srcTableNames = new ArrayList<>();
            srcTableNames.add((String)srcTableName);
        } else {
            srcTableNames = (List)srcTableName;
        }

        return srcTableNames;
    }

    @Internal
    List<String> getDstTableNames() {
        List<String> dstTableNames;
        if (dstTableName instanceof String) {
            dstTableNames =  new ArrayList<>();
            dstTableNames.add((String)dstTableName);
        } else {
            dstTableNames = (List)dstTableName;
        }

        return dstTableNames;
    }
}
