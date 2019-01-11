package ch.so.agi.gretl.logging;

/**
 * Returns a Logger instance
 */
public interface LogFactory {
    public GretlLogger getLogger(Class logSource);
}
