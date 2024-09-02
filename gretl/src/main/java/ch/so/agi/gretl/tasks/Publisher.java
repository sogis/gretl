package ch.so.agi.gretl.tasks;

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

import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.*;
import org.interlis2.validator.Validator;

import com.github.robtimus.filesystems.sftp.SFTPEnvironment;
import com.github.robtimus.filesystems.sftp.SFTPFileSystemProvider;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.PublisherStep;
import ch.so.agi.gretl.util.SimiSvcApi;
import ch.so.agi.gretl.util.SimiSvcClient;
import ch.so.agi.gretl.util.TaskUtil;

public class Publisher extends DatabaseTask {
    protected GretlLogger log;

    // Liste der zu publizierenden Regionen (Dateinamen oder Datasetnamen). Nur falls die Publikation Regionen-weise erfolgen soll
    private final ListProperty<String> regions = getProject().getObjects().listProperty(String.class);

    // Falls die Publikation Regionen-weise erfolgt (region!=null): Liste der tatsaechlich publizierten Regionen
    private final ListProperty<String> publishedRegions = getProject().getObjects().listProperty(String.class);

    // Identifikator der Daten z.B. "ch.so.agi.vermessung.edit"
    private String dataIdent;

    // Zielverzeichnis
    private Endpoint target;

    // Quelldatei z.B. "/path/file.xtf"
    private Object sourcePath;

    // Schema in der Datenbank z.B. "av"
    private String dbSchema;

    //  ili2db-Datasetname der Quelldaten "dataset"
    private String dataset;

    // ili2db-Modellname(n) zur Auswahl der Quelldaten
    private String modelsToPublish;

    // Muster der Dateinamen oder Datasetnamen, falls die Publikation Regionen-weise erfolgt z.B. "[0-9][0-9][0-9][0-9]"
    private String region;

    // Konfiguration fuer die Validierung (eine ilivalidator-config-Datei) z.B. "validationConfig.ini"
    private Object validationConfig;

    // Benutzerformat (Geopackage, Shapefile, Dxf) erstellen
    private boolean userFormats;

    // Endpunkt des SIMI-Services
    private Endpoint kgdiService;

    // Endpunkt des Authentifizierung-Services
    private Endpoint kgdiTokenService;

    // Konfiguration fuer die Ausduennung z.B. "grooming.json"
    private Object grooming;

    // Das Export-Modell, indem die Daten exportiert werden
    private String exportModels;

    // Dateipfade, die Modell-Dateien (ili-Dateien) enthalten
    private String modeldir;

    // Proxy Server fuer den Zugriff auf Modell Repositories
    private String proxy;

    // Proxy Port fuer den Zugriff auf Modell Repositories
    private Integer proxyPort;

    private Date version;

    @TaskAction
    public void publishAll() {
        log = LogEnvironment.getLogger(Publisher.class);
        PublisherStep step = new PublisherStep();
        Path sourceFile = getSourceFile();

        validateOrThrow();

        log.info("target " + target);

        Path targetFile = target.getUrl().startsWith("sftp:")
                ? processSftp()
                : getProject().file(target.getUrl()).toPath();

        Path validationFile = getValidationFile();
        Path groomingFile = getGroomingFile();
        Settings settings = getSettings();
        SimiSvcApi simiSvc = getSimiSvc();

        if (version == null) {
            version = new Date();
        }

        try {
            Files.createDirectories(getProject().getBuildDir().toPath());
            List<String> pubRegions = null;
            if (region!=null || !regions.get().isEmpty()) {
                pubRegions = new ArrayList<>();
            }

            List<String> regionsToPublish = regions.get().isEmpty() ? null : regions.get();

            if (getDbUri() != null) {
                step.publishDatasetFromDb(version, dataIdent, createConnector().connect(), dbSchema,dataset,modelsToPublish,exportModels,userFormats,targetFile, region,regionsToPublish,pubRegions, validationFile, groomingFile, settings,getProject().getBuildDir().toPath(), simiSvc);
            } else {
                step.publishDatasetFromFile(version, dataIdent, sourceFile, userFormats,targetFile, region, regionsToPublish,pubRegions, validationFile, groomingFile, settings,getProject().getBuildDir().toPath(),simiSvc);
            }

            if (pubRegions != null) {
                this.publishedRegions.set(pubRegions);
            }
        } catch (Exception e) {
            log.error("failed to run Publisher", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    @Input
    public String getDataIdent() {
        return dataIdent;
    }
    @Input
    public Endpoint getTarget() {
        return target;
    }

    /*
     *  @InputFile kann hier nicht verwendet werden, da die Datei existieren muss.
     *  Bei einem ersten Run dieses Tasks kann es sein, dass die Datei noch nicht existiert.
     */
    @Input
    @Optional
    public Object getSourcePath() {
        return sourcePath;
    }

    @Input
    @Optional
    public String getDbSchema() {
        return dbSchema;
    }

    @Input
    @Optional
    public String getDataset() {
        return dataset;
    }

    @Input
    @Optional
    public String getModelsToPublish() {
        return modelsToPublish;
    }

    @Input
    @Optional
    public String getRegion() {
        return region;
    }

    @Input
    @Optional
    public ListProperty<String> getRegions() {
        return regions;
    }

    // Falls die Publikation Regionen-weise erfolgt (region!=null): Liste der tatsaechlich publizierten Regionen
    @Internal
    public ListProperty<String> getPublishedRegions() {
        return publishedRegions;
    }

    @InputFile
    @Optional
    public Object getValidationConfig() {
        return validationConfig;
    }

    @Input
    @Optional
    public Boolean isUserFormats() {
        return userFormats;
    }

    @Input
    @Optional
    public Endpoint getKgdiService() {
        return kgdiService;
    }

    @Input
    @Optional
    public Endpoint getKgdiTokenService() {
        return kgdiTokenService;
    }

    @InputFile
    @Optional
    public Object getGrooming() {
        return grooming;
    }

    @Input
    @Optional
    public String getExportModels() {
        return exportModels;
    }

    @Input
    @Optional
    public String getModeldir() {
        return modeldir;
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

    private Path getSourceFile() {
        return sourcePath != null
                ? getProject().file(sourcePath).toPath()
                : null;
    }

    private Settings getSettings() {
        Settings settings = new Settings();

        if (modeldir != null) {
            settings.setValue(Validator.SETTING_ILIDIRS, modeldir);
        }
        if (proxy != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        }
        if (proxyPort != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
        }

        return settings;
    }

    private void validateOrThrow() throws IllegalArgumentException {
        if (sourcePath != null && getDbUri() != null) {
            throw new IllegalArgumentException("only sourcePath OR database can be set");
        }

        if (sourcePath == null && getDbUri() == null) {
            throw new IllegalArgumentException("one of sourcePath OR database must be set");
        }

        if (getDbUri() != null) {
            if (dbSchema == null) {
                throw new IllegalArgumentException("dbSchema must be set");
            }
            if (modelsToPublish == null && dataset == null && region == null && regions.get().isEmpty()) {
                throw new IllegalArgumentException("modelsToPublish OR dataset OR region OR regions must be set");
            } else if((modelsToPublish != null ? 1 : 0) + (dataset != null ? 1:0) + (region != null ? 1:0) + (!regions.get().isEmpty() ? 1:0) > 1) {
                throw new IllegalArgumentException("only one of modelsToPublish OR dataset OR region OR regions can be set");
            }
        }

        if (target == null) {
            throw new IllegalArgumentException("target must be set");
        }
    }

    private Path processSftp() {
        URI host;
        URI rawuri;
        String path;

        try {
            rawuri = new URI( target.getUrl());
            path = rawuri.getRawPath();

            if (rawuri.getPort() == -1) {
                host = new URI(rawuri.getScheme()+"://"+rawuri.getHost());
            } else {
                host = new URI(rawuri.getScheme()+"://"+rawuri.getHost()+":"+rawuri.getPort());
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        SFTPEnvironment environment = new SFTPEnvironment()
                .withUsername(target.getUser())
                .withPassword(target.getPassword().toCharArray())
                .withKnownHosts(new File(System.getProperty("user.home"),".ssh/known_hosts"));

        try (FileSystem fs = FileSystems.newFileSystem(host, environment, SFTPFileSystemProvider.class.getClassLoader())) {
            return fs.getPath(path);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Path getValidationFile() {
        return validationConfig != null
                ? getProject().file(validationConfig).toPath()
                : null;
    }

    private Path getGroomingFile() {
        return getProject().file(grooming).toPath();
    }

    private SimiSvcApi getSimiSvc() {
        SimiSvcApi result = null;

        if (kgdiService != null &&
                !kgdiService.getUrl().isEmpty() &&
                !kgdiService.getUser().isEmpty() &&
                !kgdiService.getPassword().isEmpty()
        ) {
            result = new SimiSvcClient();
            result.setup(kgdiService.getUrl(), kgdiService.getUser(), kgdiService.getPassword());

            if (kgdiTokenService != null) {
                result.setupTokenService(kgdiTokenService.getUrl(), kgdiTokenService.getUser(), kgdiTokenService.getPassword());
            }
        }

        return result;
    }
}