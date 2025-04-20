package ch.so.agi.gretl.tasks.impl;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;

public abstract class Ili2dbImport extends Ili2dbAbstractTask {
    
    /**
     * Name der XTF-/ITF-Datei, die gelesen werden soll. Es können auch mehrere Dateien sein. `FileCollection` oder `List`.
     */
    @InputFiles
    public abstract Property<Object> getDataFile();
}
