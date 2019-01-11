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
 * This Class represents the the Task which executes the SQLExecutorStep
 * Only this Class should execute the SQLExecutorStep. Users must use this Class to access SQLExecutorStep
 */
public class SqlExecutor extends DefaultTask {
    private static GretlLogger log;

    static{
        LogEnvironment.initGradleIntegrated();
        log = LogEnvironment.getLogger(SqlExecutor.class);
    }

    @Input
    public Connector database;


    @Input
    public List<String> sqlFiles;

    @Input
    @Optional
    public java.util.Map<String,String> sqlParameters=null;

    @TaskAction
    public void executeSQLExecutor() {

        String taskName = this.getName();

        if (sqlFiles==null) {
            throw new GradleException("sqlFiles is null");
        }


        List<File> files = convertToFileList(sqlFiles);

        try {
            SqlExecutorStep step = new SqlExecutorStep(taskName);
            step.execute(database, files,sqlParameters);
            log.info("Task start");
        } catch (Exception e) {
            log.error("Exception in creating / invoking SqlExecutorStep.", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }

    private List<File> convertToFileList(List<String> filePaths){

        List<File> files = new ArrayList<>();

        for(String filePath : filePaths)
        {
            if(filePath == null || filePath.length() == 0)
                throw new IllegalArgumentException("Filepaths must not be null or empty");

            File absolute = TaskUtil.createAbsolutePath(filePath, ((Task)this).getProject());
            files.add(absolute);
        }

        return files;
    }
}

