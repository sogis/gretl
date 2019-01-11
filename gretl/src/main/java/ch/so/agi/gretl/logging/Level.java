package ch.so.agi.gretl.logging;

/**
 * Utility class defining the logging levels when
 * using the gretl logging in a standalone environment.
 * The mapping to the standalone logging framework does
 * not include renaming the outputs to the gretl names.
 * A gretl info message will be logged as fine etc.
 */
public class Level {

    public static final Level ERROR = new Level(java.util.logging.Level.SEVERE);
    public static final Level LIFECYCLE = new Level(java.util.logging.Level.CONFIG);
    public static final Level INFO = new Level(java.util.logging.Level.FINE);
    public static final Level DEBUG = new Level(java.util.logging.Level.FINER);

    private java.util.logging.Level innerLevel;

    private Level(java.util.logging.Level innerLevel){
        if(innerLevel == null)
            throw new IllegalArgumentException("innerLevel must not be null");

        this.innerLevel = innerLevel;
    }

    java.util.logging.Level getInnerLevel(){
        return innerLevel;
    }
}

