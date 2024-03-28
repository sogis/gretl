package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.PostgisRasterExportStep;
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

public class PostgisRasterExport extends DefaultTask {
    private GretlLogger log;

    private Connector database;
    private String sqlFile;

    private Map<String, String> sqlParameters = null;

    private Object dataFile = null;

    @Input
    public Connector getDatabase() {
        return database;
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

    public void setDatabase(List<String> databaseDetails){
        if (databaseDetails.size() != 3) {
            throw new IllegalArgumentException("Values for db_uri, db_user, db_pass are required.");
        }

        String databaseUri = databaseDetails.get(0);
        String databaseUser = databaseDetails.get(1);
        String databasePassword = databaseDetails.get(2);

        this.database = new Connector(databaseUri, databaseUser, databasePassword);
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

    @TaskAction
    public void exportRaster() {
        log = LogEnvironment.getLogger(PostgisRasterExport.class);

        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
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
            step.execute(database, sql, data, sqlParameters);
        } catch (Exception e) {
            log.error("Exception in creating / invoking PostgisRasterExportStep.", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
