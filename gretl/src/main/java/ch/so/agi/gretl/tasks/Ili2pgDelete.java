package ch.so.agi.gretl.tasks;

import java.util.ArrayList;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

public class Ili2pgDelete extends Ili2pgAbstractTask {

    @TaskAction
    public void replaceData() {
        Config settings = createConfig();
        int function = Config.FC_DELETE;
        if (dataset == null) {
            return;
        }
        java.util.List<String> datasetNames=null;
        if (dataset != null) {
            if(dataset instanceof String) {
                datasetNames=new ArrayList<String>();
                datasetNames.add((String)dataset);
            }else {
                datasetNames=(java.util.List)dataset;
            }
        }
        settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        
        try {
            for(String datasetName:datasetNames) {
                settings.setDatasetName(datasetName);
                run(function, settings);
            }
        } catch (GradleException ge) {
            String msg = ge.getMessage();
            // If dataset does not exist, it will NOT throw an error.
            if (msg.contains("dataset") && msg.contains("doesn") && msg.contains("exist")) {
                return;
            } else {
                throw ge;
            }
        }
    }
}
