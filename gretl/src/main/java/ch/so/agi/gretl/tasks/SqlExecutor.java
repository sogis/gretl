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
 * This class represents the task which executes the SQLExecutorStep. Only
 * this class should execute the SQLExecutorStep.
 */
public abstract class SqlExecutor extends DefaultTask {
    private static GretlLogger log;

    /**
     * Datenbank, in die importiert werden soll.
     */
    @Input
    public abstract ListProperty<String> getDatabase();

    /**
     * Name der SQL-Datei aus der SQL-Statements gelesen und ausgeführt werden.
     */
    @Input
    public abstract ListProperty<String> getSqlFiles();
    
    /**
     * Eine Map mit Paaren von Parameter-Name und Parameter-Wert (`Map<String,String>`). Oder eine Liste mit Paaren von Parameter-Name und Parameter-Wert (`List<Map<String,String>>`).
     */
    @Input
    @Optional
    public abstract Property<Object> getSqlParameters();

    @TaskAction
    public void executeSQLExecutor() {
        log = LogEnvironment.getLogger(SqlExecutor.class);
        
        String taskName = this.getName();

        if (!getDatabase().isPresent()) {
            throw new GradleException("database is null");
        }

        if (!getSqlFiles().isPresent()) {
            throw new GradleException("sqlFiles is null");
        }

        Connector database = TaskUtil.getDatabaseConnectorObject(getDatabase().get());

        List<File> files = convertToFileList(getSqlFiles());

        try {
            SqlExecutorStep step = new SqlExecutorStep(taskName);
            if (!getSqlParameters().isPresent()) {
                step.execute(database, files, null);
            } else if (getSqlParameters().get() instanceof Map) {
                step.execute(database, files, (Map<String,String>)getSqlParameters().get());
            } else {
                List<java.util.Map<String,String>> paramList = (List<Map<String,String>>)getSqlParameters().get();
                for (Map<String,String> sqlParams : paramList) {
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
}
