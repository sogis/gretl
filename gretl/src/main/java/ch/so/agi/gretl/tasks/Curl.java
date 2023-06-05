package ch.so.agi.gretl.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

// README.md eventuell
// abstract class geht erst mit 5.6 oder so. Nicht mit 5.1.1
// Dann kommen aber viele Warnungen von anderen Tasks wegen fehlendem Getter o.ä.

public class Curl extends DefaultTask {
    protected GretlLogger log;

    @Internal
    public String serverUrl;
    
    @Internal
    public MethodType method;
    
    @TaskAction
    public void request() {
        log = LogEnvironment.getLogger(Curl.class);

        System.out.println("*********"+serverUrl+"***********");
        System.out.println("*********"+method+"***********");

    }
    
    
    private static enum MethodType {
        GET, POST
    }

    
    
    
}
