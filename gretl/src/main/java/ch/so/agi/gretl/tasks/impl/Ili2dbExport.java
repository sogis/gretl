package ch.so.agi.gretl.tasks.impl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFiles;

public abstract class Ili2dbExport extends Ili2dbAbstractTask {

    /**
     * Entspricht der ili2db-Option `--export3`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getExport3();
    
    /**
     * Entspricht der ili2db-Option `--exportModels`.
     */    
    @Input
    @Optional
    public abstract Property<String> getExportModels();

    /**
     * Name der XTF-/ITF-/GML-Datei, die erstellt werden soll.
     */
    @OutputFiles
    public abstract Property<FileCollection> getDataFile();
}
