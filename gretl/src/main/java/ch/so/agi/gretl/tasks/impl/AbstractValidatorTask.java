package ch.so.agi.gretl.tasks.impl;


import ch.ehi.basics.settings.Settings;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.interlis2.validator.Validator;

import java.util.List;


public class AbstractValidatorTask extends DefaultTask {
    @InputFiles
    public List<Object> dataFiles;
    @Input
    @Optional
    public String models = null;
    @Input
    @Optional
    public String modeldir = null;
    @InputFile
    @Optional
    public Object configFile = null;
    @Input
    @Optional
    public boolean forceTypeValidation = false;
    @Input
    @Optional
    public boolean disableAreaValidation = false;
    @Input
    @Optional
    public boolean multiplicityOff = false;
    @Input
    @Optional
    public boolean allObjectsAccessible=false;
    @Input
    @Optional
    public boolean skipPolygonBuilding=false;
    @OutputFile
    @Optional
    public Object logFile = null;
    @OutputFile
    @Optional
    public Object xtflogFile = null;
    @InputDirectory
    @Optional
    public Object pluginFolder = null;
    @Input
    @Optional
    public String proxy = null;
    @Input
    @Optional
    public Integer proxyPort = null;
    @Input
    @Optional
    public boolean failOnError=true;
    public boolean validationOk=true;

    protected void initSettings(Settings settings) {
        settings.setValue(Validator.SETTING_DISABLE_STD_LOGGER, Validator.TRUE);
            if(models!=null) {
                settings.setValue(Validator.SETTING_MODELNAMES, models);
            }
            if(modeldir!=null) {
                settings.setValue(Validator.SETTING_ILIDIRS, modeldir);
            }
            if(configFile!=null) {
                settings.setValue(Validator.SETTING_CONFIGFILE, this.getProject().file(configFile).getPath());
            }
            if(forceTypeValidation) {
                settings.setValue(Validator.SETTING_FORCE_TYPE_VALIDATION,Validator.TRUE);
            }
            if(disableAreaValidation) {
                settings.setValue(Validator.SETTING_DISABLE_AREA_VALIDATION,Validator.TRUE);
            }
            if(multiplicityOff) {
                settings.setValue(Validator.SETTING_MULTIPLICITY_VALIDATION,ch.interlis.iox_j.validator.ValidationConfig.OFF);
            }
            if(allObjectsAccessible){
                settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE,Validator.TRUE);
            }
            if(skipPolygonBuilding) {
                settings.setValue(ch.interlis.iox_j.validator.Validator.CONFIG_DO_ITF_LINETABLES, ch.interlis.iox_j.validator.Validator.CONFIG_DO_ITF_LINETABLES_DO);
            }
            if(logFile!=null) {
                settings.setValue(Validator.SETTING_LOGFILE, this.getProject().file(logFile).getPath());
            }
            if(xtflogFile!=null) {
                settings.setValue(Validator.SETTING_XTFLOG, this.getProject().file(xtflogFile).getPath());
            }
            if(pluginFolder!=null) {
                settings.setValue(Validator.SETTING_PLUGINFOLDER, this.getProject().file(pluginFolder).getPath());
            }
            if(proxy!=null) {
                settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
            }
            if(proxyPort!=null) {
                settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
            }
    }

}

