package ch.so.agi.gretl.tasks;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

public class Ili2pgDelete extends Ili2pgAbstractTask {
    @Input
    public String dataset = null;

    @TaskAction
    public void replaceData() {
        Config settings = createConfig();
        int function = Config.FC_DELETE;
        if (dataset == null) {
            return;
        }
        settings.setDatasetName(dataset);
        settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        run(function, settings);
    }
}
