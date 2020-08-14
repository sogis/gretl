package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class Ili2pgExport extends Ili2pgAbstractTask {
    @Input
    @Optional
    public boolean export3 = false;

    @OutputFile
    public Object dataFile = null;

    @TaskAction
    public void exportData() {
        Config settings = createConfig();
        int function = Config.FC_EXPORT;
        if (dataFile == null) {
            return;
        }
        if (export3) {
            settings.setVer3_export(true);
        }
        
        String xtfFilename = this.getProject().file(dataFile).getPath();
        if (Ili2db.isItfFilename(xtfFilename)) {
            settings.setItfTransferfile(true);
        }
        settings.setXtffile(xtfFilename);
        run(function, settings);
    }
}
