package ch.so.agi.gretl.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import com.github.robtimus.filesystems.sftp.SFTPEnvironment;
import com.github.robtimus.filesystems.sftp.SFTPFileSystemProvider;

import ch.interlis.ili2c.Ili2cException;
import ch.interlis.iox.IoxException;
import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import ch.so.agi.gretl.steps.MetaPublisherStep;
import ch.so.agi.gretl.util.TaskUtil;
import freemarker.template.TemplateException;
import net.sf.saxon.s9api.SaxonApiException;

public class MetaPublisher extends DefaultTask {
    protected GretlLogger log;

    @Input
    public String dataIdent = null; // Identifikator der Daten z.B. "ch.so.agi.vermessung.edit"
    
    @Input
    public Endpoint target = null; // Zielverzeichnis

    @Input
    @Optional
    public ListProperty<String> regions = null; // Publizierte Regionen (aus Publisher-Task)

    @Input
    @Optional
    public Endpoint geocatTarget = null; // Geocat-Zielverzeichnis
    
    @TaskAction
    public void publishAll() {
        log = LogEnvironment.getLogger(MetaPublisher.class);

        if (dataIdent ==  null) {
            throw new IllegalArgumentException("dataIdent must not be null");
        }
        
        Path targetFile = null;
        if (target != null) {
            log.info("target " + target.toString());
            targetFile = getTargetFile(target);
        } else {
            throw new IllegalArgumentException("target must be set");
        }
        
        Path geocatTargetFile = null;
        if (geocatTarget != null) {
            log.info("geocat target " + geocatTarget.toString());
            geocatTargetFile = getTargetFile(geocatTarget);
        }
        
        // TODO: 
        // - dokumentieren 
        // - Sollte ok sein. Im ersten Unterordner von gretl (also im Gretl-Job-Ordner) muss jeweils ein settings file sein.
        // Dann wird es als Projekt-Root erkannt.
        File themeRootDirectory = getProject().getRootDir().getParentFile().getParentFile();

        MetaPublisherStep step = new MetaPublisherStep();
        try {
            step.execute(themeRootDirectory, dataIdent, targetFile, regions!=null?regions.get():null, geocatTargetFile);
        } catch (IOException | IoxException | Ili2cException | SaxonApiException | TemplateException e) {
            log.error("failed to run MetaPublisher", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
    
    // TODO: mit Publisher vereinen
    // S3 hinzufuegen (falls das Teil funktioniert)
    private Path getTargetFile(Endpoint target) {
        Path targetFile;
        if (target.getUrl().startsWith("sftp:")) {
            URI host = null;
            URI rawuri = null;
            String path = null;
            try {
                rawuri = new URI(target.getUrl());
                path = rawuri.getRawPath();
                if (rawuri.getPort() == -1) {
                    host = new URI(rawuri.getScheme() + "://" + rawuri.getHost());
                } else {
                    host = new URI(rawuri.getScheme() + "://" + rawuri.getHost() + ":" + rawuri.getPort());
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
            SFTPEnvironment environment = new SFTPEnvironment().withUsername(target.getUser())
                    .withPassword(target.getPassword().toCharArray())
                    .withKnownHosts(new File(System.getProperty("user.home"), ".ssh/known_hosts"));
            FileSystem fileSystem = null;
            try {
                fileSystem = FileSystems.newFileSystem(host, environment,
                        SFTPFileSystemProvider.class.getClassLoader());
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
            targetFile = fileSystem.getPath(path);
        } else {
            targetFile = getProject().file(target.getUrl()).toPath();
        }
        return targetFile;
  
    }

}
