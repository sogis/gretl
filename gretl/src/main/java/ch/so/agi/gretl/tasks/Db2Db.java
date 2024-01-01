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
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;

public class Db2Db extends DefaultTask {
    private static GretlLogger log;

    static {
        LogEnvironment.initGradleIntegrated();
        log = LogEnvironment.getLogger(Db2Db.class);
    }

    /**
     * Datenbank aus der gelesen werden soll 
     */
    @Input
    public Connector sourceDb;
    /**
     * Datenbank in die geschrieben werden soll 
     */
    @Input
    public Connector targetDb;
    /**
     * Eine Liste von `TransferSet`s
     */
    @Input
    public List<TransferSet> transferSets;
    @Input
    @Optional
    public Integer batchSize = null;
    @Input
    @Optional
    public Integer fetchSize = null;
    @Input
    @Optional
    public Object sqlParameters = null;

    @TaskAction
    public void executeTask() throws Exception {
        String taskName = ((Task) this).getName();
        convertToAbsolutePaths(transferSets);

        log.info(String.format("Start Db2DbTask(Name: %s SourceDb: %s TargetDb: %s Transfers: %s)", taskName, sourceDb,
                targetDb, transferSets));

        Settings settings = new Settings();
        if (batchSize != null) {
            settings.setValue(Db2DbStep.SETTING_BATCH_SIZE, batchSize.toString());
        }
        if (fetchSize != null) {
            settings.setValue(Db2DbStep.SETTING_FETCH_SIZE, fetchSize.toString());
        }
        try {
            Db2DbStep step = new Db2DbStep(taskName);
            if(sqlParameters==null) {
                step.processAllTransferSets(sourceDb, targetDb, transferSets, settings, null);
            }else if(sqlParameters instanceof java.util.Map) {
                step.processAllTransferSets(sourceDb, targetDb, transferSets, settings, (java.util.Map<String,String>)sqlParameters);
            }else {
                java.util.List<java.util.Map<String,String>> paramList=(java.util.List<java.util.Map<String,String>>)sqlParameters;
                for(java.util.Map<String,String> sqlParams:paramList) {
                    step.processAllTransferSets(sourceDb, targetDb, transferSets, settings, sqlParams);
                }
            }
        } catch (Exception e) {
            log.error("Exception in creating / invoking Db2DbStep in Db2DbTask", e);

            GradleException gradleEx = TaskUtil.toGradleException(e);
            throw gradleEx;
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
