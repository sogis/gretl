package ch.so.agi.gretl.tasks;

import org.gradle.api.tasks.TaskAction;

import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2dbImportSchema;

public abstract class Ili2duckdbImportSchema extends Ili2dbImportSchema {
    
    @TaskAction
    public void importSchema() {
        Config settings = createConfig();
        int function = Config.FC_SCHEMAIMPORT;
        String iliFilename = null;
        if (getIliFile().isPresent()) {
            Object iliFile = getIliFile().get();
            if (iliFile instanceof String
                    && ch.ehi.basics.view.GenericFileFilter.getFileExtension((String) iliFile) == null) {
                iliFilename = (String) iliFile;
            } else {
                iliFilename = this.getProject().file(iliFile).getPath();
            }
        }
        settings.setXtffile(iliFilename);
        
        if (getIliMetaAttrs().isPresent()) {
            String iliMetaAttrsFilename = this.getProject().file(getIliMetaAttrs().get()).getPath();
            settings.setIliMetaAttrsFile(iliMetaAttrsFilename);
        }
        
        init(settings);
        run(function, settings);
    }
    
    private Config createConfig() {
        Config settings = new Config();
        new ch.ehi.ili2duckdb.DuckDBMain().initConfig(settings);
        
        settings.setStrokeArcs(settings.STROKE_ARCS_ENABLE);
        
        return settings;
    }
}
