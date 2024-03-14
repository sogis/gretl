package ch.so.agi.gretl.tasks;

import org.gradle.api.tasks.TaskAction;

import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

public class Ili2pgValidate extends Ili2pgAbstractTask {

    @TaskAction
    public void validateData() {
        Config settings = createConfig();
        int function = Config.FC_VALIDATE;

        // TODO: https://github.com/claeis/ili2db/issues/514
        settings.setDburl(database.getDbUri());

        java.util.List<String> datasetNames = null;

        if (dataset != null) {
            if (dataset instanceof String) {
                datasetNames = new java.util.ArrayList<String>();
                datasetNames.add((String) dataset);
            } else {
                datasetNames = (java.util.List) dataset;
            }
        }

        if (datasetNames != null) {
            for (String ds : datasetNames) {
                settings.setDatasetName(ds);
                run(function, settings);
            }
            return;
        }
        run(function, settings);
    }
}
