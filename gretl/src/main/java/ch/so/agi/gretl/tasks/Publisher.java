package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.interlis2.validator.Validator;

import com.github.robtimus.filesystems.sftp.SFTPEnvironment;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.PublisherStep;
import ch.so.agi.gretl.util.TaskUtil;

public class Publisher extends DefaultTask {
    protected GretlLogger log;
    
    @Input
    public String dataIdent=null; // Identifikator der Daten z.B. "ch.so.agi.vermessung.edit"
    //@Output
    public Endpoint target=null; // Zielverzeichnis
    @InputFile
    @Optional
    public Object sourcePath=null; // Quelldatei z.B. "/path/file.xtf"
    @Input
    @Optional
    public Connector database=null; //  Datenbank mit Quelldaten z.B. ["uri","user","password"]. Alternative zu sourcePath
    @Input
    @Optional
    public String dbSchema=null; // Schema in der Datenbank z.B. "av"
    @Input
    @Optional
    public String dataset=null; //  ili2db-Datasetname der Quelldaten "dataset"
    @Input
    @Optional
    public String region=null; // Muster der der Dateinamen oder Datasetnamen, falls die Publikation Regionen-weise erfolgt z.B. "[0-9][0-9][0-9][0-9]"     
    @InputFile
    @Optional
    public Object validationConfig=null; // Konfiguration fuer die Validierung (eine ilivalidator-config-Datei) z.B. "validationConfig.ini"
    @Input
    @Optional
    public boolean userFormats=false; // Benutzerformat (Geopackage, Shapefile, Dxf) erstellen
    @Input
    @Optional
    public Endpoint kgdiService=null; // Endpunkt des SIMI-Services
    @InputFile
    @Optional
    public Object grooming=null; // Konfiguration fuer die Ausduennung z.B. "grooming.json"
    @Input
    @Optional
    public String exportModels=null; // Das Export-Modell, indem die Daten exportiert werden 
    @Input
    @Optional
    public String modeldir=null;     // Dateipfade, die Modell-Dateien (ili-Dateien) enthalten
    @Input
    @Optional
    public String proxy=null;        // Proxy Server fuer den Zugriff auf Modell Repositories
    @Input
    @Optional
    public Integer proxyPort=null;    // Proxy Port fuer den Zugriff auf Modell Repositories
    @Input
    @Optional
    public String versionTag=null;

    
    @TaskAction
    public void publishAll() {
        log = LogEnvironment.getLogger(Publisher.class);
        PublisherStep step=new PublisherStep();
        Path sourceFile=null;
        if(sourcePath!=null && database!=null) {
            throw new IllegalArgumentException("only sourcePath OR database can be set");
        }else if(sourcePath!=null) {
            sourceFile=getProject().file(sourcePath).toPath();
        }else if(database!=null) {
            if(dbSchema==null) {
                throw new IllegalArgumentException("dbSchema must be set");
            }
            if(dataset==null) {
                throw new IllegalArgumentException("dataset must be set");
            }
        }else {
            throw new IllegalArgumentException("one of sourcePath OR database must be set");
        }
        Path targetFile=null;
        if(target!=null) {
            try {
                java.net.URI uri=new java.net.URI(target.getUrl());
                if(uri.getScheme().equals("sftp")) {
                    URI host=null;
                    URI rawuri=null;
                    String path=null;
                    try {
                        rawuri = new URI( target.getUrl());
                        path=rawuri.getRawPath();
                        if(rawuri.getPort()==-1) {
                            host= new URI(rawuri.getScheme()+"://"+rawuri.getHost());
                        }else {
                            host= new URI(rawuri.getScheme()+"://"+rawuri.getHost()+":"+rawuri.getPort());
                        }
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(e);
                    }
                    SFTPEnvironment environment = new SFTPEnvironment()
                            .withUsername(target.getUser())
                            .withPassword(target.getPassword().toCharArray())
                            .withKnownHosts(new File(System.getProperty("user.home"),".ssh/known_hosts"));
                    FileSystem fileSystem=null;
                    try {
                        fileSystem = FileSystems.newFileSystem( host, environment );
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                    targetFile = fileSystem.getPath(path);
                }else if(uri.getScheme().equals("file")){
                    targetFile=getProject().file(target.getUrl()).toPath();
                }else {
                    throw new IllegalArgumentException("target.url must be a valid file or sftp URI");
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("target.url must be a valid file or sftp URI");
            }
        }else {
            throw new IllegalArgumentException("target must be set");
        }
        Path validationFile=null;
        if(validationConfig!=null) {
            validationFile=getProject().file(validationConfig).toPath();
        }
        Path groomingFile=null;
        if(grooming!=null) {
            groomingFile=getProject().file(grooming).toPath();
        }
        Settings settings=new Settings();
        if(modeldir!=null) {
            settings.setValue(Validator.SETTING_ILIDIRS, modeldir);
        }
        if (proxy != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        }
        if (proxyPort != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
        }
        if(versionTag==null) {
            versionTag=new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        }
        try {
            if(database!=null) {
                step.publishDatasetFromDb(versionTag, dataIdent, database.connect(), dbSchema,dataset,exportModels,targetFile, region,null, validationFile, groomingFile, settings,getProject().getBuildDir().toPath());
            }else {
                step.publishFromFile(versionTag, dataIdent, sourceFile, targetFile, region, null, validationFile, groomingFile, settings,getProject().getBuildDir().toPath());
            }
        } catch (Exception e) {
            log.error("failed to run Publisher", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
