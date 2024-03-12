package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.SqlExecutorStep;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This Class represents the the Task which executes the SQLExecutorStep Only
 * this Class should execute the SQLExecutorStep. Users must use this Class to
 * access SQLExecutorStep
 */
public class SqlExecutor extends DefaultTask {
    private static GretlLogger log;

    static {
        LogEnvironment.initGradleIntegrated();
        log = LogEnvironment.getLogger(SqlExecutor.class);
    }


    private Connector database;
    private List<String> sqlFiles;
    private Object sqlParameters = null;

    @Input
    public Connector getDatabase() {
        return database;
    }

    @Input
    public List<String> getSqlFiles() {
        return sqlFiles;
    }
    @Input
    @Optional
    public Object getSqlParameters() {
        return sqlParameters;
    }

    public void setDatabase(List<String> databaseDetails) {
        if (databaseDetails.size() != 3) {
            throw new IllegalArgumentException("Values for db_uri, db_user, db_pass are required.");
        }

        String databaseUri = databaseDetails.get(0);
        String databaseUser = databaseDetails.get(1);
        String databasePassword = databaseDetails.get(2);

        this.database = new Connector(databaseUri, databaseUser, databasePassword);
    }

    public void setSqlFiles(List<String> sqlFiles) {
        this.sqlFiles = sqlFiles;
    }

    public void setSqlParameters(Object sqlParameters) {
        this.sqlParameters = sqlParameters;
    }

    @TaskAction
    public void executeSQLExecutor() {

        String taskName = this.getName();

        if (sqlFiles == null) {
            throw new GradleException("sqlFiles is null");
        }

        List<File> files = convertToFileList(sqlFiles);

        try {
            SqlExecutorStep step = new SqlExecutorStep(taskName);
            if(sqlParameters==null) {
                step.execute(database, files, null);
            }else if(sqlParameters instanceof java.util.Map) {
                step.execute(database, files, (java.util.Map<String,String>)sqlParameters);
            }else {
                java.util.List<java.util.Map<String,String>> paramList=(java.util.List<java.util.Map<String,String>>)sqlParameters;
                for(java.util.Map<String,String> sqlParams:paramList) {
                    step.execute(database, files, sqlParams);
                }
                
            }
            log.info("Task start");
        } catch (Exception e) {
            log.error("Exception in creating / invoking SqlExecutorStep.", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }

    private List<File> convertToFileList(List<String> filePaths) {

        List<File> files = new ArrayList<>();

        for (String filePath : filePaths) {
            if (filePath == null || filePath.length() == 0)
                throw new IllegalArgumentException("Filepaths must not be null or empty");

            File absolute = TaskUtil.createAbsolutePath(filePath, ((Task) this).getProject());
            files.add(absolute);
        }

        return files;
    }
}
