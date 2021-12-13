package ch.so.agi.gretl.tasks;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.PublisherStep;

public class Publisher extends DefaultTask {
    protected GretlLogger log;
    
    @Input
    public String publishTopics[]=null; //  = ["ch.so.agi.vermessung","ch.so.agi.vermessung.edit"]
    @InputFile
    public Object configFile; // = "publisherConfig.json"
    @OutputDirectory
    public Object targetFolder=null; //  = "out"
    @Input
    @Optional
    public boolean pushToFtp=true; //  = true //falls nicht angegeben à default true
    @Input
    @Optional
    public boolean groomFtp=true; //falls nicht angegeben à default true
    @Input
    @Optional
    public URL kgdiService=null; //  = "http://api.kgdi.ch/metadata"
    
    @TaskAction
    public void publishAll() {
        log = LogEnvironment.getLogger(Publisher.class);
        PublisherStep step=new PublisherStep();
        File config=getProject().file(configFile);
        File target=getProject().file(targetFolder);
        step.excepute(config,publishTopics,target,pushToFtp,groomFtp,kgdiService);
    }
}
