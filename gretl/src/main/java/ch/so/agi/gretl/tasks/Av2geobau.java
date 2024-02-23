package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private Object itfFiles = null;
    private Object dxfDirectory = null;
    private String modeldir = null;
    private Object logFile = null;
    private String proxy = null;
    private Integer proxyPort = null;
    private Boolean zip = false;

    @InputFile
    public Object getItfFiles() {
        return itfFiles;
    }
    @OutputDirectory
    public Object getDxfDirectory() {
        return dxfDirectory;
    }

    @Input
    @Optional
    public String getModeldir() {
        return modeldir;
    }

    @OutputFile
    @Optional
    public Object getLogFile() {
        return logFile;
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
    public Boolean isZip() {
        return zip;
    }

    @TaskAction
    public void runTransformation() {
        log = LogEnvironment.getLogger(Av2geobau.class);
        
        if (dxfDirectory instanceof File) {
            ((File) dxfDirectory).mkdirs();
        } else {
            new File((String) dxfDirectory).mkdirs();
        }
        
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
                
                if (zip) {
                    String outZipFileName = Paths.get(dxfDir.getAbsolutePath(), dxf.getName().replaceFirst("[.][^.]+$", "") + ".zip").toFile().getAbsolutePath();
                    FileOutputStream fileOutputStream = new FileOutputStream(outZipFileName);
                    ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

                    ZipEntry dxfZipEntry = new ZipEntry(dxf.getName());
                    zipOutputStream.putNextEntry(dxfZipEntry);
                    new FileInputStream(dxf).getChannel().transferTo(0, dxf.length(), Channels.newChannel(zipOutputStream));

                    List<String> addonFileNames = Arrays.asList("DXF_Geobau_Layerdefinition.pdf", "Hinweise.pdf", "Musterplan.pdf");
                    for (String addonFileName : addonFileNames) {
                        File addonFile = Paths.get(dxfDir.getAbsolutePath(), addonFileName).toFile();
                        InputStream addonInputStream = Av2geobau.class.getResourceAsStream("/av2geobau/"+addonFileName); 
                        Files.copy(addonInputStream, addonFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        addonInputStream.close();
                        
                        ZipEntry layerDefinitionZipEntry = new ZipEntry(addonFile.getName());
                        zipOutputStream.putNextEntry(layerDefinitionZipEntry);
                        new FileInputStream(addonFile).getChannel().transferTo(0, addonFile.length(), Channels.newChannel(zipOutputStream));
                    }
                    
                    zipOutputStream.closeEntry();
                    zipOutputStream.close();
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
