package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

// README.md eventuell
// abstract class geht erst mit 5.6 oder so. Nicht mit 5.1.1
// Dann kommen aber viele Warnungen von anderen Tasks wegen fehlendem Getter o.ae.
// Publisher-Ansatz geht nicht, weil dann wird wirklich ein Objekt vom Typ Property erwartet.
// Das ist fuer Anwender doof.

public class Curl extends DefaultTask {
    protected GretlLogger log;

    @Internal
    public String serverUrl;
    
    @Internal
    public MethodType method;
    
    @Internal
    @Optional
    public Map<String,Object> formData; // curl [URL] -F key1=value1 -F file1=@my_file.xtf 
    
    @Internal
    @Optional
    public String data; // curl [URL] -d "key1=value1&key2=value2"
    
    @Internal
    @Optional
    public File outputFile; // curl [URL] -o
    
    @Internal
    @Optional
    public File dataBinary; // curl [URL] --data-binary
    
    @Internal
    @Optional
    public Map<String,String> header; // curl [URL] -H ... -H ...
    
    @Internal
    @Optional
    public String user;
    
    @Internal
    @Optional
    public String password;
    
    @TaskAction
    public void request() {
        log = LogEnvironment.getLogger(Curl.class);

        System.out.println("*********"+serverUrl+"***********");
        System.out.println("*********"+method+"***********");
        System.out.println("*********"+formData+"***********");

    }
    
    
    private static enum MethodType {
        GET, POST
    }

    
    
    
}
