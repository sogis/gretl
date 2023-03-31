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

        run(function, settings);
    }
}

