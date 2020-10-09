package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import ch.ehi.basics.settings.Settings;
import ch.ehi.basics.view.GenericFileFilter;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class Av2geobau extends DefaultTask {
    protected GretlLogger log;
    
    @InputFile
    public Object itfFiles = null;
    
    @OutputDirectory
    public Object dxfDirectory = null;
    
    @Input
    @Optional
    public String modeldir = null;
    
    @OutputFile
    @Optional
    public Object logFile = null;
    
    @Input
    @Optional
    public String proxy = null;
    
    @Input
    @Optional
    public Integer proxyPort = null;
    
    @TaskAction
    public void runTransformation() {
        log = LogEnvironment.getLogger(Av2geobau.class);
        
        Settings settings=new Settings();
        settings.setValue(org.interlis2.av2geobau.Av2geobau.SETTING_ILIDIRS, org.interlis2.av2geobau.Av2geobau.SETTING_DEFAULT_ILIDIRS);
        if (itfFiles == null) {
            throw new IllegalArgumentException("itfFiles must not be null");
        }
        if (dxfDirectory == null) {
            throw new IllegalArgumentException("dxfDirectory must not be null");
        }
        if(modeldir!=null) {
            settings.setValue(org.interlis2.av2geobau.Av2geobau.SETTING_ILIDIRS, modeldir);
        }
        if(proxy!=null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        }
        if(proxyPort!=null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
        }

        FileCollection dataFilesCollection=null;
        if(itfFiles instanceof FileCollection) {
            dataFilesCollection=(FileCollection)itfFiles;
        }else {
            dataFilesCollection=getProject().files(itfFiles);
        }
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<java.io.File> files = new ArrayList<java.io.File>();
        for (java.io.File fileObj : dataFilesCollection) {
            files.add(fileObj);
        }
        File dxfDir = this.getProject().file(dxfDirectory);
        
        boolean ok=true;
        try {
            for(File itf:files) {
                File dxf=new File(dxfDir,GenericFileFilter.stripFileExtension(itf.getName())+".dxf");
                ok = org.interlis2.av2geobau.Av2geobau.convert(itf,dxf,settings);
                if(!ok) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("failed to run Av2geobau", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        } 
        if(!ok) {
            throw new TaskExecutionException(this, new Exception("Av2geobau failed"));
        }
    }
}
