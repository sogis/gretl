package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.PublisherStep;
import ch.so.agi.gretl.util.SimiSvcApi;
import ch.so.agi.gretl.util.SimiSvcClient;
import ch.so.agi.gretl.util.TaskUtil;
import com.github.robtimus.filesystems.sftp.SFTPEnvironment;
import com.github.robtimus.filesystems.sftp.SFTPFileSystemProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.*;
import org.interlis2.validator.Validator;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Publisher extends DefaultTask {
    protected GretlLogger log;

    private String dataIdent = null; // Identifikator der Daten z.B. "ch.so.agi.vermessung.edit"
    private Endpoint target = null; // Zielverzeichnis
    private Object sourcePath = null; // Quelldatei z.B. "/path/file.xtf"
    private Connector database = null; //  Datenbank mit Quelldaten z.B. ["uri","user","password"]. Alternative zu sourcePath
    private String dbSchema = null; // Schema in der Datenbank z.B. "av"
    private String dataset = null; //  ili2db-Datasetname der Quelldaten "dataset"
    private String modelsToPublish = null; //  ili2db-Modellname(n) zur Auswahl der Quelldaten
    private String region; // Muster der Dateinamen oder Datasetnamen, falls die Publikation Regionen-weise erfolgt z.B. "[0-9][0-9][0-9][0-9]"
    private ListProperty<String> regions = getProject().getObjects().listProperty(String.class); // Liste der zu publizierenden Regionen (Dateinamen oder Datasetnamen). Nur falls die Publikation Regionen-weise erfolgen soll
    private ListProperty<String> _publishedRegions = getProject().getObjects().listProperty(String.class); // Falls die Publikation Regionen-weise erfolgt (region!=null): Liste der tatsaechlich publizierten Regionen
    private Object validationConfig = null; // Konfiguration fuer die Validierung (eine ilivalidator-config-Datei) z.B. "validationConfig.ini"
    private Boolean userFormats = false; // Benutzerformat (Geopackage, Shapefile, Dxf) erstellen
    private Endpoint kgdiService = null; // Endpunkt des SIMI-Services
    private Endpoint kgdiTokenService = null; // Endpunkt des Authentifizierung-Services
    private Object grooming = null; // Konfiguration fuer die Ausduennung z.B. "grooming.json"
    private String exportModels = null; // Das Export-Modell, indem die Daten exportiert werden
    private String modeldir = null;     // Dateipfade, die Modell-Dateien (ili-Dateien) enthalten
    private String proxy = null;        // Proxy Server fuer den Zugriff auf Modell Repositories
    private Integer proxyPort = null;    // Proxy Port fuer den Zugriff auf Modell Repositories
    private Date version = null;

    /**
     * Identifikator der Daten z.B. "ch.so.agi.vermessung.edit".
     */
    @Input
    public String getDataIdent() {
        return dataIdent;
    }

    /**
     * Zielverzeichnis z.B. `[ "sftp://ftp.server.ch/data", "user", "password" ]` oder einfach ein Pfad `[file("/out")]`
     */
    @Input
    public Endpoint getTarget() {
        return target;
    }

    /**
     * Quelldatei z.B. `file("/path/file.xtf")`
     */
    @InputFile
    @Optional
    public Object getSourcePath() {
        return sourcePath;
    }

    /**
     * Datenbank mit Quelldaten z.B. `["uri","user","password"]`. Alternative zu sourcePath.
     */
    @Input
    @Optional
    public Connector getDatabase() {
        return database;
    }

    /**
     * Schema in der Datenbank z.B. `"av"`
     */
    @Input
    @Optional
    public String getDbSchema() {
        return dbSchema;
    }

    /**
     * ili2db-Datasetname der Quelldaten "dataset" (Das ili2db-Schema muss also immer mit `--createBasketCol` erstellt werden)
     */
    @Input
    @Optional
    public String getDataset() {
        return dataset;
    }

    /**
     * INTERLIS-Modellnamen der Quelldaten in der DB (Nur für "einfache" Modelle, deren ili2db-Schema ohne `--createBasketCol` erstellt werden kann).
     */
    @Input
    @Optional
    public String getModelsToPublish() {
        return modelsToPublish;
    }

    /**
     * Muster (Regular Expression) der Dateinamen oder Datasetnamen, falls die Publikation Regionen-weise erfolgt z.B. `"[0-9][0-9][0-9][0-9]"`. Alternative zum Parameter regions,<br/><br/>Bei Quelle "Datei" ist die Angabe einer "stellvertretenden" Transferdatei mittels "sourcePath" zwingend. Bsp.: Bei sourcePath `file("/transferfiles/dummy.xtf")` werden alle im Ordner "transferfiles" enthaltenen Transferdateien mit dem Muster verglichen und bei "match" selektiert und verarbeitet.
     */
    @Input
    @Optional
    public String getRegion() {
        return region;
    }

    /**
     * Liste der zu publizierenden Regionen (Dateinamen oder Datasetnamen), falls die Publikation Regionen-weise erfolgen soll. Alternative zum Parameter `region`.
     */
    @Input
    @Optional
    public ListProperty<String> getRegions() {
        return regions;
    }

    /**
     * Liste der effektiv publizierten Regionen.
     */
    @Internal
    public ListProperty<String> getPublishedRegions() {
        // Falls die Publikation Regionen-weise erfolgt (region!=null): Liste der tatsaechlich publizierten Regionen
        return _publishedRegions;
    }

    /**
     * Konfiguration für die Validierung (eine ilivalidator-config-Datei) z.B. "validationConfig.ini".
     */
    @Input
    @Optional
    public Object getValidationConfig() {
        return validationConfig;
    }

    /**
     * Benutzerformat (Geopackage, Shapefile, Dxf) erstellen. Default: false
     */
    @Input
    @Optional
    public Boolean isUserFormats() {
        return userFormats;
    }

    /**
     * Endpunkt des SIMI-Services für die Rückmeldung des Publikationsdatums und die Erstellung des Beipackzettels, z.B. `["http://api.kgdi.ch/metadata","user","pwd"]`. Publisher ergänzt die URL fallabhängig mit `/pubsignal` respektive `/doc`.
     */
    @Input
    @Optional
    public Endpoint getKgdiService() {
        return kgdiService;
    }

    /**
     * Endpunkt des Authentifizierung-Services, z.B. `["http://api.kgdi.ch/metadata","user","pwd"]`. Publisher ergänzt die URL mit `/v2/oauth/token`.
     */
    @Input
    @Optional
    public Endpoint getKgdiTokenService() {
        return kgdiTokenService;
    }

    /**
     * Konfiguration für die Ausdünnung z.B. `"grooming.json"`. Ohne Angabe wird nicht aufgeräumt.
     */
    @InputFile
    @Optional
    public Object getGrooming() {
        return grooming;
    }

    /**
     * Das Export-Modell, indem die Daten exportiert werden. Der Parameter wird nur bei der Ausdünnung benötigt. Als Export-Modelle sind Basis-Modelle zulässig.
     */
    @Input
    @Optional
    public String getExportModels() {
        return exportModels;
    }

    /**
     * Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon `;` getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default: `%ITF_DIR;http://models.interlis.ch/`. `%ITF_DIR` ist ein Platzhalter für das Verzeichnis mit der ITF-Datei.
     */
    @Input
    @Optional
    public String getModeldir() {
        return modeldir;
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

    @Input
    @Optional
    public Date getVersion() {
        return version;
    }

    public void setDataIdent(String dataIdent) {
        this.dataIdent = dataIdent;
    }

    public void setTarget(Endpoint target) {
        this.target = target;
    }

    public void setSourcePath(Object sourcePath) {
        this.sourcePath = sourcePath;
    }

    public void setDatabase(List<String> databaseDetails) {
        this.database = TaskUtil.getDatabaseConnectorObject(databaseDetails);
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public void setModelsToPublish(String modelsToPublish) {
        this.modelsToPublish = modelsToPublish;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setRegions(List<String> regions) {
        this.regions.set(regions);
    }

    public void setValidationConfig(Object validationConfig) {
        this.validationConfig = validationConfig;
    }

    public void setUserFormats(Boolean userFormats) {
        this.userFormats = userFormats;
    }

    public void setKgdiService(Endpoint kgdiService) {
        this.kgdiService = kgdiService;
    }

    public void setKgdiTokenService(Endpoint kgdiTokenService) {
        this.kgdiTokenService = kgdiTokenService;
    }

    public void setGrooming(Object grooming) {
        this.grooming = grooming;
    }

    public void setExportModels(String exportModels) {
        this.exportModels = exportModels;
    }

    public void setModeldir(String modeldir) {
        this.modeldir = modeldir;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    @TaskAction
    public void publishAll() {
        log = LogEnvironment.getLogger(Publisher.class);
        
        PublisherStep step = new PublisherStep();
        Path sourceFile = null;

        if (sourcePath != null && database != null) {
            throw new IllegalArgumentException("only sourcePath OR database can be set");
        } else if (sourcePath != null) {
            sourceFile = getProject().file(sourcePath).toPath();
        } else if (database != null) {
            if (dbSchema == null) {
                throw new IllegalArgumentException("dbSchema must be set");
            }
            if (modelsToPublish == null && dataset == null && region == null && regions.get().isEmpty()) {
                throw new IllegalArgumentException("modelsToPublish OR dataset OR region OR regions must be set");
            } else if ((modelsToPublish != null?1:0) + (dataset != null?1:0) + (region != null?1:0) + (!regions.get().isEmpty()?1:0) > 1) {
                throw new IllegalArgumentException("only one of modelsToPublish OR dataset OR region OR regions can be set");
            }
        } else {
            throw new IllegalArgumentException("one of sourcePath OR database must be set");
        }

        log.info("target " + target);

        if (target == null) {
            throw new IllegalArgumentException("target must be set");
        }

        Path targetFile = target.getUrl().startsWith("sftp:")
                ? getSftpPath()
                : getProject().file(target.getUrl()).toPath();
        
        Path validationFile = validationConfig != null
                ? getProject().file(validationConfig).toPath()
                : null;
        
        Path groomingFile = grooming != null
                ? getProject().file(grooming).toPath()
                : null;
        
        Settings settings = new Settings();
        if (modeldir!=null) {
            settings.setValue(Validator.SETTING_ILIDIRS, modeldir);
        }
        if (proxy != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        }
        if (proxyPort != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
        }
        
        SimiSvcApi simiSvc = null;
        if (kgdiService != null) {
            if (!kgdiService.getUrl().isEmpty() && !kgdiService.getUser().isEmpty() && !kgdiService.getPassword().isEmpty()) {
                simiSvc=new SimiSvcClient();
                simiSvc.setup(kgdiService.getUrl(), kgdiService.getUser(), kgdiService.getPassword());
                if(kgdiTokenService!=null) {
                    simiSvc.setupTokenService(kgdiTokenService.getUrl(), kgdiTokenService.getUser(), kgdiTokenService.getPassword());
                }
            }
        }

        if (version == null) {
            version = new Date();
        }

        try {
            Files.createDirectories(getProject().getBuildDir().toPath());
            List<String> pubRegions=null;
            if (region!=null || !regions.get().isEmpty()) {
                pubRegions = new ArrayList<>();
            }

            List<String> regionsToPublish = regions.get().isEmpty() ? null : regions.get();
            if (database != null) {
                step.publishDatasetFromDb(version, dataIdent, database.connect(), dbSchema,dataset,modelsToPublish,exportModels,userFormats,targetFile, region,regionsToPublish,pubRegions, validationFile, groomingFile, settings,getProject().getBuildDir().toPath(),simiSvc);
            } else {
                step.publishDatasetFromFile(version, dataIdent, sourceFile, userFormats,targetFile, region, regionsToPublish,pubRegions, validationFile, groomingFile, settings,getProject().getBuildDir().toPath(),simiSvc);
            }
            if (pubRegions != null) {
                this._publishedRegions.set(pubRegions);
            }
        } catch (Exception e) {
            log.error("failed to run Publisher", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    private Path getSftpPath() {
        URI host = null;
        URI rawuri = null;
        String path = null;
        try {
            rawuri = new URI( target.getUrl());
            path=rawuri.getRawPath();
            if (rawuri.getPort() == -1) {
                host = new URI(rawuri.getScheme() + "://"+rawuri.getHost());
            } else {
                host = new URI(rawuri.getScheme() + "://"+rawuri.getHost() + ":"+rawuri.getPort());
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        SFTPEnvironment environment = new SFTPEnvironment()
                .withUsername(target.getUser())
                .withPassword(target.getPassword().toCharArray())
                .withKnownHosts(new File(System.getProperty("user.home"),".ssh/known_hosts"));

        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystems.newFileSystem( host, environment, SFTPFileSystemProvider.class.getClassLoader() );
            keepAlive(fileSystem);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return fileSystem.getPath(path);
    }
    
    private void keepAlive(FileSystem fileSystem) {        
        Thread keepAliveThread = new Thread(() -> {
            while (true) {
                try {
                    log.debug("sending keep-alive signal to sftp server");
                    SFTPFileSystemProvider.keepAlive(fileSystem);

                    Thread.sleep(60000);
                } catch (InterruptedException | IOException e) {
                    log.error(e.getMessage(), e);
                    break;
                }
            }
        });
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }
}
