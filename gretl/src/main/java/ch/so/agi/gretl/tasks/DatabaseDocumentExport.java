package ch.so.agi.gretl.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.DatabaseDocumentExportStep;
import ch.so.agi.gretl.util.TaskUtil;

public class DatabaseDocumentExport extends DefaultTask {
    protected GretlLogger log;
   
    @Input
    public Connector database;

    @Input
    public String qualifiedTableName;

    @Input
    public String documentColumn;
    
    @OutputDirectory 
    public File targetDir;
    
    @Input
    @Optional
    public String fileNamePrefix = null;
    
    @Input
    @Optional
    public String fileNameExtension = null;

    @TaskAction
    public void export() {
        log = LogEnvironment.getLogger(DatabaseDocumentExport.class);

        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        if (qualifiedTableName == null) {
            throw new IllegalArgumentException("qualifiedTableName must not be null");
        }
        if (documentColumn == null) {
            throw new IllegalArgumentException("documentColumn must not be null");
        }
        if (targetDir == null) {
            throw new IllegalArgumentException("targetDir must not be null");
        }

        try {
            DatabaseDocumentExportStep databaseDocumentExportStep = new DatabaseDocumentExportStep();
            databaseDocumentExportStep.execute(database, qualifiedTableName, documentColumn, targetDir.getAbsolutePath(), fileNamePrefix, fileNameExtension);
        } catch (Exception e) {
            log.error("Exception in DatabaseDocumentExport task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
