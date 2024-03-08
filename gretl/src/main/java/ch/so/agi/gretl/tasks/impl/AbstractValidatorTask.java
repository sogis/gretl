package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.settings.Settings;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.interlis2.validator.Validator;

import java.util.List;

public class AbstractValidatorTask extends DefaultTask {

    private Object dataFiles;

    private String models = null;

    private String modeldir = null;

    private Object configFile = null;

    private boolean forceTypeValidation = false;

    private boolean disableAreaValidation = false;

    private boolean multiplicityOff = false;

    private boolean allObjectsAccessible = false;

    private boolean skipPolygonBuilding = false;

    private Object logFile = null;

    private Object xtflogFile = null;

    private Object pluginFolder = null;

    private String proxy = null;

    private Integer proxyPort = null;

    private boolean failOnError = true;
    private boolean validationOk = true;

    @InputFiles
    public Object getDataFiles() {
        return dataFiles;
    }

    @Input
    @Optional
    public String getModels() {
        return models;
    }

    @Input
    @Optional
    public String getModeldir() {
        return modeldir;
    }

    @InputFile
    @Optional
    public Object getConfigFile() {
        return configFile;
    }

    @Input
    @Optional
    public boolean isForceTypeValidation() {
        return forceTypeValidation;
    }

    @Input
    @Optional
    public boolean isDisableAreaValidation() {
        return disableAreaValidation;
    }

    @Input
    @Optional
    public boolean isMultiplicityOff() {
        return multiplicityOff;
    }

    @Input
    @Optional
    public boolean isAllObjectsAccessible() {
        return allObjectsAccessible;
    }

    @Input
    @Optional
    public boolean isSkipPolygonBuilding() {
        return skipPolygonBuilding;
    }

    @OutputFile
    @Optional
    public Object getLogFile() {
        return logFile;
    }

    @OutputFile
    @Optional
    public Object getXtflogFile() {
        return xtflogFile;
    }

    @InputDirectory
    @Optional
    public Object getPluginFolder() {
        return pluginFolder;
    }

    @Input
    @Optional
    public String getProxy() {
        return proxy;
    }

    @Input
    @Optional
    public Integer getProxyPort() {
        return proxyPort;
    }

    @Input
    @Optional
    public boolean isFailOnError() {
        return failOnError;
    }

    public boolean isValidationOk() {
        return validationOk;
    }

    public void setValidationOk(boolean validationOk) {
        this.validationOk = validationOk;
    }

    protected void initSettings(Settings settings) {
        settings.setValue(Validator.SETTING_DISABLE_STD_LOGGER, Validator.TRUE);
        if (models != null) {
            settings.setValue(Validator.SETTING_MODELNAMES, models);
        }
        if (modeldir != null) {
            settings.setValue(Validator.SETTING_ILIDIRS, modeldir);
        }
        if (configFile != null) {
            settings.setValue(Validator.SETTING_CONFIGFILE, this.getProject().file(configFile).getPath());
        }
        if (forceTypeValidation) {
            settings.setValue(Validator.SETTING_FORCE_TYPE_VALIDATION, Validator.TRUE);
        }
        if (disableAreaValidation) {
            settings.setValue(Validator.SETTING_DISABLE_AREA_VALIDATION, Validator.TRUE);
        }
        if (multiplicityOff) {
            settings.setValue(Validator.SETTING_MULTIPLICITY_VALIDATION,
                    ch.interlis.iox_j.validator.ValidationConfig.OFF);
        }
        if (allObjectsAccessible) {
            settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE, Validator.TRUE);
        }
        if (skipPolygonBuilding) {
            settings.setValue(ch.interlis.iox_j.validator.Validator.CONFIG_DO_ITF_LINETABLES,
                    ch.interlis.iox_j.validator.Validator.CONFIG_DO_ITF_LINETABLES_DO);
        }
        if (logFile != null) {
            settings.setValue(Validator.SETTING_LOGFILE, this.getProject().file(logFile).getPath());
        }
        if (xtflogFile != null) {
            settings.setValue(Validator.SETTING_XTFLOG, this.getProject().file(xtflogFile).getPath());
        }
        if (pluginFolder != null) {
            settings.setValue(Validator.SETTING_PLUGINFOLDER, this.getProject().file(pluginFolder).getPath());
        }
        if (proxy != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        }
        if (proxyPort != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
        }
    }
}
