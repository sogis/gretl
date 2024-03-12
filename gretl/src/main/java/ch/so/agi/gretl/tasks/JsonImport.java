package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.List;

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

    private Connector database;

    private String qualifiedTableName = null;

    private String jsonFile = null;

    private String columnName = null;

    private Boolean deleteAllRows = false;

    @Input
    public Connector getDatabase() {
        return database;
    }

    @Input
    public String getQualifiedTableName() {
        return qualifiedTableName;
    }

    @Input
    public String getJsonFile() {
        return jsonFile;
    }

    @Input
    public String getColumnName() {
        return columnName;
    }

    @Input
    @Optional
    public Boolean isDeleteAllRows() {
        return deleteAllRows;
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

    public void setQualifiedTableName(String qualifiedTableName) {
        this.qualifiedTableName = qualifiedTableName;
    }

    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setDeleteAllRows(Boolean deleteAllRows) {
        this.deleteAllRows = deleteAllRows;
    }

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
