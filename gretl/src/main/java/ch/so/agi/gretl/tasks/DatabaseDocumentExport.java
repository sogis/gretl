package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.DatabaseDocumentExportStep;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;

public class DatabaseDocumentExport extends DefaultTask {
    protected GretlLogger log;
    private Connector database;
    private String qualifiedTableName;
    private String documentColumn;
    private File targetDir;
    private String fileNamePrefix = null;
    private String fileNameExtension = null;

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
            throw TaskUtil.toGradleException(e);
        }
    }

    @Input
    public String getQualifiedTableName() {
        return qualifiedTableName;
    }

    @Input
    public String getDocumentColumn() {
        return documentColumn;
    }

    @OutputDirectory
    public File getTargetDir() {
        return targetDir;
    }

    @Input
    @Optional
    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    @Input
    @Optional
    public String getFileNameExtension() {
        return fileNameExtension;
    }

    @Input
    public Connector getDatabase() {
        return database;
    }

    public void setDatabase(List<String> databaseDetails) {
        this.database = TaskUtil.getDatabaseConnectorObject(databaseDetails);
    }

    public void setQualifiedTableName(String qualifiedTableName) {
        this.qualifiedTableName = qualifiedTableName;
    }

    public void setDocumentColumn(String documentColumn) {
        this.documentColumn = documentColumn;
    }

    public void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public void setFileNameExtension(String fileNameExtension) {
        this.fileNameExtension = fileNameExtension;
    }
}
