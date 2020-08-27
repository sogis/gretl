package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2gpkgAbstractTask;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

public class Ili2gpkgImport extends Ili2gpkgAbstractTask {
    @InputFile
    public Object dataFile = null;

    @Input
    @Optional
    public boolean coalesceJson = false;    

    @Input
    @Optional
    public boolean nameByTopic = false;

    @Input
    @Optional
    public String defaultSrsCode = null;

    @Input
    @Optional
    public boolean createEnumTabs = false;
    
    @Input
    @Optional
    public boolean createMetaInfo = false;

    @TaskAction
    public void importData() {
        Config settings = createConfig();
        // Probably the most wanted use case with GeoPackage?
        settings.setDoImplicitSchemaImport(true);
        
        if (coalesceJson) {
            settings.setJsonTrafo(settings.JSON_TRAFO_COALESCE);
        }

        if (nameByTopic) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_TOPIC);
        }

        if (defaultSrsCode != null) {
            settings.setDefaultSrsCode(defaultSrsCode);
        }

        if (createEnumTabs) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_MULTI);
        }
        
        if (createMetaInfo) {
            settings.setCreateMetaInfo(true);
        }

        int function = Config.FC_IMPORT;
        if (dataFile == null) {
            return;
        }
        String xtfFilename = this.getProject().file(dataFile).getPath();
        if (Ili2db.isItfFilename(xtfFilename)) {
            settings.setItfTransferfile(true);
        }
        settings.setXtffile(xtfFilename);
        run(function, settings);
    }
}
