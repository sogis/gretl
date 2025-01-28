package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.TransferSet;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.Db2DbStep;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class Db2Db extends DefaultTask {
    private static GretlLogger log;

    /**
     * Datenbank, aus der gelesen werden soll.
     */
    @Input
    public abstract ListProperty<String> getSourceDb();
    
    /**
     * Datenbank, in die geschrieben werden soll.
     */
    @Input
    public abstract ListProperty<String> getTargetDb();
    
    /**
     * Eine Liste von `TransferSet`s.
     */
    @Input
    public abstract ListProperty<TransferSet> getTransferSets();
    
    /**
     * Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden (Default: 5000). Für sehr grosse Tabellen muss ein kleinerer Wert gewählt werden.
     */
    @Input
    @Optional
    public abstract Property<Integer> getBatchSize();
    
    /**
     * Anzahl der Records, die auf einmal vom Datenbank-Cursor von der Quell-Datenbank zurückgeliefert werden (Standard: 5000). Für sehr grosse Tabellen muss ein kleinerer Wert gewählt werden.
     */
    @Input
    @Optional
    public abstract Property<Integer> getFetchSize();
    
    /**
     * Eine Map mit Paaren von Parameter-Name und Parameter-Wert (`Map<String,String>`). Oder eine Liste mit Paaren von Parameter-Name und Parameter-Wert (`List<Map<String,String>>`).
     */
    @Input
    @Optional
    public abstract Property<Object> getSqlParameters();

    @TaskAction
    public void executeTask() throws GradleException {
        log = LogEnvironment.getLogger(Db2Db.class);
        
        String taskName = ((Task) this).getName();
        List<TransferSet> transferSets = getTransferSets().get();
        Connector sourceDb = TaskUtil.getDatabaseConnectorObject(getSourceDb().get());
        Connector targetDb = TaskUtil.getDatabaseConnectorObject(getTargetDb().get());
        log.info(String.format("Start Db2DbTask(Name: %s SourceDb: %s TargetDb: %s Transfers: %s)", taskName, sourceDb,
                targetDb, transferSets));

        convertToAbsolutePaths(transferSets);

        Settings settings = new Settings();
        if (getBatchSize().isPresent()) {
            settings.setValue(Db2DbStep.SETTING_BATCH_SIZE, getBatchSize().get().toString());
        }
        if (getFetchSize().isPresent()) {
            settings.setValue(Db2DbStep.SETTING_FETCH_SIZE, getFetchSize().get().toString());
        }
        try {
            Db2DbStep step = new Db2DbStep(taskName);
            if(!getSqlParameters().isPresent()) {
                step.processAllTransferSets(sourceDb, targetDb, transferSets, settings, null);
            }else if(getSqlParameters().get() instanceof Map) {
                step.processAllTransferSets(sourceDb, targetDb, transferSets, settings, (Map<String,String>)getSqlParameters().get());
            }else {
                java.util.List<java.util.Map<String,String>> paramList=(List<Map<String,String>>)getSqlParameters().get();
                for(java.util.Map<String,String> sqlParams:paramList) {
                    step.processAllTransferSets(sourceDb, targetDb, transferSets, settings, sqlParams);
                }
            }
        } catch (Exception e) {
            log.error("Exception in creating / invoking Db2DbStep in Db2DbTask", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    private void convertToAbsolutePaths(List<TransferSet> transferSets) {
        for (TransferSet ts : transferSets) {
            File configured = ts.getInputSqlFile();
            File absolutePath = TaskUtil.createAbsolutePath(configured, ((Task) this).getProject());
            ts.setInputSqlFile(absolutePath);
        }
    }
}
