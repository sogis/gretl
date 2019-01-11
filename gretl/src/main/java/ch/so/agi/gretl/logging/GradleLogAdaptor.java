package ch.so.agi.gretl.logging;

import org.gradle.api.logging.*;

/**
 * Class taking care of the logging when using the Steps
 * integrated in Gradle (When running the corresponding Tasks in Gradle).
 */
public class GradleLogAdaptor implements GretlLogger {

    private org.gradle.api.logging.Logger logger;

    GradleLogAdaptor(Class logSource){
        this.logger = Logging.getLogger(logSource);
    }

    public void info(String msg){
        logger.info(msg);
    }

    public void debug(String msg){
        logger.debug(msg);
    }

    public void lifecycle(String msg){
        logger.lifecycle(msg);
    }

    public void error(String msg, Throwable thrown){
        logger.error(msg, thrown);
    }
}
