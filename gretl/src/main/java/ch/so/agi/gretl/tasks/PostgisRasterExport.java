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


public class PostgisRasterExport extends DefaultTask {
    private GretlLogger log;

    @Input
    public Connector database;

    @Input
    public String sqlFile;

    @Input
    @Optional
    public java.util.Map<String,String> sqlParameters = null;

    @OutputFile
    public Object dataFile = null;

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
