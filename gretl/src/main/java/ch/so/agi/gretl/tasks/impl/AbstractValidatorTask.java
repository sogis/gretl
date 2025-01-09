package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.logging.GretlLogger;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.interlis2.validator.Validator;

import java.io.File;
import java.util.List;

public class AbstractValidatorTask extends DefaultTask {
    protected GretlLogger log;

    private Object dataFiles;

    private String models = null;

    private String modeldir = null;

    private Object configFile = null;
    
    private Object metaConfigFile = null;

    private Boolean forceTypeValidation = false;

    private Boolean disableAreaValidation = false;

    private Boolean multiplicityOff = false;

    private Boolean allObjectsAccessible = false;

    private Boolean skipPolygonBuilding = false;

    private Object logFile = null;

    private Object xtflogFile = null;

    private Object pluginFolder = null;

    private String proxy = null;

    private Integer proxyPort = null;

    private Boolean failOnError = true;
    protected boolean validationOk = true;

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

    @Input
    @Optional
    public Object getConfigFile() {
        return configFile;
    }

    @Input
    @Optional
    public Object getMetaConfigFile() {
        return metaConfigFile;
    }

    @Input
    @Optional
    public Boolean getForceTypeValidation() {
        return forceTypeValidation;
    }

    @Input
    @Optional
    public Boolean getDisableAreaValidation() {
        return disableAreaValidation;
    }
    @Input
    @Optional
    public Boolean getMultiplicityOff() {
        return multiplicityOff;
    }

    @Input
    @Optional
    public Boolean getAllObjectsAccessible() {
        return allObjectsAccessible;
    }

    @Input
    @Optional
    public Boolean getSkipPolygonBuilding() {
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
    public Boolean getFailOnError() {
        return failOnError;
    }

    public void setDataFiles(Object dataFiles) {
        this.dataFiles = dataFiles;
    }

    public void setModels(String models) {
        this.models = models;
    }

    public void setModeldir(String modeldir) {
        this.modeldir = modeldir;
    }

    public void setConfigFile(Object configFile) {
        this.configFile = configFile;
    }
    
    public void setMetaConfigFile(Object metaConfigFile) {
        this.metaConfigFile = metaConfigFile;
    }

    public void setForceTypeValidation(Boolean forceTypeValidation) {
        this.forceTypeValidation = forceTypeValidation;
    }

    public void setDisableAreaValidation(Boolean disableAreaValidation) {
        this.disableAreaValidation = disableAreaValidation;
    }

    public void setMultiplicityOff(Boolean multiplicityOff) {
        this.multiplicityOff = multiplicityOff;
    }

    public void setAllObjectsAccessible(Boolean allObjectsAccessible) {
        this.allObjectsAccessible = allObjectsAccessible;
    }

    public void setSkipPolygonBuilding(Boolean skipPolygonBuilding) {
        this.skipPolygonBuilding = skipPolygonBuilding;
    }

    public void setLogFile(Object logFile) {
        this.logFile = logFile;
    }

    public void setXtflogFile(Object xtflogFile) {
        this.xtflogFile = xtflogFile;
    }

    public void setPluginFolder(Object pluginFolder) {
        this.pluginFolder = pluginFolder;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setFailOnError(Boolean failOnError) {
        this.failOnError = failOnError;
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
        if (metaConfigFile != null) {
            if (metaConfigFile instanceof File) {
                settings.setValue(Validator.SETTING_META_CONFIGFILE, this.getProject().file(metaConfigFile).getPath());                
            } else {
                settings.setValue(Validator.SETTING_META_CONFIGFILE, metaConfigFile.toString());                                
            }
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
