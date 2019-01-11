package ch.so.agi.gretl.logging;


import java.util.logging.Logger;

/**
 * Class behind the GretlLogger interface when using the
 * steps without gradle (in unittests, ...).
 */
public class CoreJavaLogAdaptor implements GretlLogger {

    private java.util.logging.Logger logger;

    CoreJavaLogAdaptor(Class logSource, Level logLevel){
        this.logger = java.util.logging.Logger.getLogger(logSource.getName());
        this.logger.setLevel(logLevel.getInnerLevel());
    }

    public void info(String msg){
        logger.fine(msg);
    }

    public void debug(String msg){
        logger.finer(msg);
    }

    public void error(String msg, Throwable thrown){
        logger.log(java.util.logging.Level.SEVERE, msg, thrown);
    }

    public void lifecycle(String msg){ logger.config(msg); }

    Logger getInnerLogger(){
        return logger;
    }
}

