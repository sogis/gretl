package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Db2Gpkg;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class GpkgExport extends DatabaseTask {
    protected GretlLogger log;
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
        final Connector connector = createConnector();

        if (connector == null) {
            throw new IllegalArgumentException("connector must not be null");
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

        try (Connection conn = connector.connect()) {
            int i = 0;
            for (final String srcTblName: srcTableNames) {
                final String dstTblName = dstTableNames.get(i);
                final Db2Gpkg db2gpkg = new Db2Gpkg();
                db2gpkg.exportData(this.getProject().file(dataFile), conn, getSettings(srcTblName, dstTblName));
                i++;
            }
        } catch (Exception e) {
            log.error("failed to run GpkgExport", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    private List<String> getSrcTableNames() {
        List<String> result;

        if (srcTableName instanceof String) {
            result = new ArrayList<>();
            result.add((String)srcTableName);
        } else {
            result = (List<String>) srcTableName;
        }

        return result;
    }

    private List<String> getDstTableNames() {
        List<String> result;

        if (dstTableName instanceof String) {
            result =  new ArrayList<>();
            result.add((String) dstTableName);
        } else {
            result = (List) dstTableName;
        }

        return result;
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

    private Settings getSettings(String sourceTableName, String destinationTableName) {
        final Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_DBTABLE, sourceTableName);
        settings.setValue(IoxWkfConfig.SETTING_GPKGTABLE, destinationTableName);

        // Set optional parameters
        if (schemaName != null) {
            settings.setValue(IoxWkfConfig.SETTING_DBSCHEMA, schemaName);
        }
        if (batchSize != null) {
            settings.setValue(IoxWkfConfig.SETTING_BATCHSIZE, batchSize.toString());
        }
        if (fetchSize != null) {
            settings.setValue(IoxWkfConfig.SETTING_FETCHSIZE, fetchSize.toString());
        }

        return settings;
    }
}
