package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.SqlExecutorStep;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This Class represents the Task which executes the SQLExecutorStep Only
 * this Class should execute the SQLExecutorStep. Users must use this Class to
 * access SQLExecutorStep
 */
public abstract class SqlExecutor extends DefaultTask {
    private static GretlLogger log;

    static {
        LogEnvironment.initGradleIntegrated();
        log = LogEnvironment.getLogger(SqlExecutor.class);
    }

    @Input
    public abstract ListProperty<String> getDatabase();

    @Input
    public abstract ListProperty<String> getSqlFiles();
    @Input
    @Optional
    public abstract Property<Object> getSqlParameters();

    @TaskAction
    public void executeSQLExecutor() {

        String taskName = this.getName();
        Connector database = buildDatabaseConnector();

        if (!getSqlFiles().isPresent()) {
            throw new GradleException("sqlFiles is null");
        }

        List<File> files = convertToFileList(getSqlFiles());

        try {
            SqlExecutorStep step = new SqlExecutorStep(taskName);
            if(!getSqlParameters().isPresent()) {
                step.execute(database, files, null);
            }else if(getSqlParameters().get() instanceof Map) {
                step.execute(database, files, (Map<String,String>)getSqlParameters().get());
            }else {
                List<java.util.Map<String,String>> paramList=(List<Map<String,String>>)getSqlParameters().get();
                for(Map<String,String> sqlParams:paramList) {
                    step.execute(database, files, sqlParams);
                }
            }
            log.info("Task start");
        } catch (Exception e) {
            log.error("Exception in creating / invoking SqlExecutorStep.", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    private List<File> convertToFileList(ListProperty<String> filePaths) {

        List<File> files = new ArrayList<>();

        for (String filePath : filePaths.get()) {
            if (filePath == null || filePath.length() == 0)
                throw new IllegalArgumentException("Filepaths must not be null or empty");

            File absolute = TaskUtil.createAbsolutePath(filePath, ((Task) this).getProject());
            files.add(absolute);
        }

        return files;
    }

    private Connector buildDatabaseConnector(){
        List<String> databaseDetails = getDatabase().get();
        if (databaseDetails.size() != 3) {
            throw new IllegalArgumentException("Values for db_uri, db_user, db_pass are required.");
        }

        String databaseUri = databaseDetails.get(0);
        String databaseUser = databaseDetails.get(1);
        String databasePassword = databaseDetails.get(2);
        return new Connector(databaseUri, databaseUser, databasePassword);
    }
}
