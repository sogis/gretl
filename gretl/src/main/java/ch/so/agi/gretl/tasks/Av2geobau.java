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
import org.gradle.api.tasks.*;

import ch.ehi.basics.settings.Settings;
import ch.ehi.basics.view.GenericFileFilter;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

public class Av2geobau extends DefaultTask {
    private GretlLogger log;
    
    private FileCollection itfFiles = null;
    private File dxfDirectory = null;
    private String modeldir = null;
    private File logFile = null;
    private String proxy = null;
    private Integer proxyPort = null;
    private Boolean zip = false;

    /**
     * ITF-Dateien, die nach DXF transformiert werden soll. 
     */
    @InputFiles
    public FileCollection getItfFiles() {
        return itfFiles;
    }
    
    /**
     * Verzeichnis, in das die DXF-Dateien gespeichert werden.
     */
    @OutputDirectory
    public File getDxfDirectory() {
        return dxfDirectory;
    }

    /**
     * INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator).
     */
    @Input
    @Optional
    public String getModeldir() {
        return modeldir;
    }

    /**
     * Schreibt die log-Meldungen der Konvertierung in eine Text-Datei.
     */
    @OutputFile
    @Optional
    public File getLogFile() {
        return logFile;
    }

    /**
     * Proxy-Server für den Zugriff auf Modell-Repositories.
     */
    @Input
    @Optional
    public String getProxy() {
        return proxy;
    }

    /**
     * Proxy-Port für den Zugriff auf Modell-Repositories.
     */
    @Input
    @Optional
    public Integer getProxyPort() {
        return proxyPort;
    }
    
    /**
     * Die zu erstellende Datei wird gezippt und es werden zusätzliche Dateien (Musterplan, Layerbeschreibung, Hinweise) hinzugefügt. Default: `false`
     */
    @Input
    @Optional
    public Boolean getZip() {
        return zip;
    }

    public void setItfFiles(FileCollection itfFiles) {
        this.itfFiles = itfFiles;
    }

    public void setDxfDirectory(File dxfDirectory) {
        this.dxfDirectory = dxfDirectory;
    }

    public void setModeldir(String modeldir) {
        this.modeldir = modeldir;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setZip(Boolean zip) {
        this.zip = zip;
    }

    @TaskAction
    public void runTransformation() {
        log = LogEnvironment.getLogger(Av2geobau.class);
        
        dxfDirectory.mkdirs();
        
        Settings settings = new Settings();
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

        FileCollection dataFilesCollection = (FileCollection) itfFiles;
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<File> files = new ArrayList<java.io.File>();
        for (File fileObj : dataFilesCollection) {
            files.add(fileObj);
        }
        File dxfDir = this.getProject().file(dxfDirectory);
        
        boolean ok=true;
        try {
            for(File itf : files) {
                File dxf = new File(dxfDir,GenericFileFilter.stripFileExtension(itf.getName())+".dxf");
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
