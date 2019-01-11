package ch.so.agi.gretl.logging;

/**
 * Holds the global logging factory used by the Step
 * and helper classes to get a logger instance.
 * Holds either a logging factory for standalone use
 * of the steps classes as in unit tests or the gradle *
 * logging environment for integrated use of the steps
 * in gradle.
 */
public class LogEnvironment {

    private static LogFactory currentLogFactory=null;

    public static void setLogFactory(LogFactory factory) {
        currentLogFactory=factory;
    }
    public static void initGradleIntegrated() {
        if(currentLogFactory == null) {
            setLogFactory( new GradleLogFactory());
        }
    }

    public static void initStandalone(){
        initStandalone(Level.DEBUG);
    }

    public static void initStandalone(Level logLevel) {
        if(currentLogFactory == null) {
            setLogFactory( new CoreJavaLogFactory(logLevel));
        }
    }

    public static GretlLogger getLogger(Class logSource) {
        if(currentLogFactory == null) {
            try {
                if(Class.forName("org.gradle.api.logging.Logger")!=null) {
                    
                }
                setLogFactory( new GradleLogFactory());
            } catch (ClassNotFoundException e) {
                // use java logging if no gradle in classpath
                setLogFactory( new CoreJavaLogFactory(Level.DEBUG));
            }
        }
        if(logSource == null)
            throw new IllegalArgumentException("The logSource must not be null");

        return currentLogFactory.getLogger(logSource);
    }
}

