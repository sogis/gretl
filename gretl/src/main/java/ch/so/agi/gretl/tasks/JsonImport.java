package ch.so.agi.gretl.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.JsonImportStep;
import ch.so.agi.gretl.util.TaskUtil;

public class JsonImport extends DefaultTask {
    protected GretlLogger log;

    @Input
    public Connector database;

    @Input
    public String qualifiedTableName = null;

    @Input
    public String jsonFile = null;
    
    @Input 
    public String columnName = null;
    
    @Input
    @Optional
    public boolean deleteAllRows = false;
    
    @TaskAction
    public void importJsonFile() {
        log = LogEnvironment.getLogger(JsonImport.class);
        
        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        if (qualifiedTableName == null) {
            throw new IllegalArgumentException("qualifiedTableName must not be null");
        }
        if (jsonFile == null) {
            throw new IllegalArgumentException("jsonFile must not be null");
        }
        if (columnName == null) {
            throw new IllegalArgumentException("columnName must not be null");
        }

        File data = this.getProject().file(jsonFile);
        
        try {
            JsonImportStep jsonImportStep = new JsonImportStep();
            jsonImportStep.execute(database, data, qualifiedTableName, columnName, deleteAllRows);
        } catch (Exception e) {
            log.error("Exception in JsonImport task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
