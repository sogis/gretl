package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.PostgisRasterExportStep;
import ch.so.agi.gretl.tasks.impl.DatabaseTask;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PostgisRasterExport extends DatabaseTask {
    private GretlLogger log;

    private String sqlFile;
    private Map<String, String> sqlParameters = null;
    private Object dataFile = null;

    @TaskAction
    public void exportRaster() {
        log = LogEnvironment.getLogger(PostgisRasterExport.class);
        final Connector connector = createConnector();

        if (connector == null) {
            throw new IllegalArgumentException("connector must not be null");
        }

        if (sqlFile == null) {
            throw new IllegalArgumentException("sqlFile must not be null");
        }

        if (dataFile == null) {
            throw new IllegalArgumentException("dataFile must not be null");
        }

        File sql = this.getProject().file(sqlFile);
        File data = this.getProject().file(dataFile);

        try {
            PostgisRasterExportStep step = new PostgisRasterExportStep();
            step.execute(connector, sql, data, sqlParameters);
        } catch (Exception e) {
            log.error("Exception in creating / invoking PostgisRasterExportStep.", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    @Input
    public String getSqlFile() {
        return sqlFile;
    }

    @Input
    @Optional
    public Map<String, String> getSqlParameters() {
        return sqlParameters;
    }

    @OutputFile
    public Object getDataFile() {
        return dataFile;
    }

    public void setSqlFile(String sqlFile) {
        this.sqlFile = sqlFile;
    }

    public void setSqlParameters(Map<String, String> sqlParameters) {
        this.sqlParameters = sqlParameters;
    }

    public void setDataFile(Object dataFile) {
        this.dataFile = dataFile;
    }
}
