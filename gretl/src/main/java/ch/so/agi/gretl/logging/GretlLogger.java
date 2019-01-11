package ch.so.agi.gretl.logging;

/**
 * Logger used in all GRETL classes. As Gretl is based on a build tool, it
 * uses the special lifecycle log level which informs gretl users on start and
 * finish of the processing of one step.
 * Consequently, each step must have exactly two lifecycle log outputs.
 * One when starting the execution of the Step (after inputvalidation).
 * One after finishing the execution of the Step (after cleanup).
 * Info is used for more detailed feedback to the user.
 * Debug is used for very detailed output that should help in debugging a problem.
 * Error is used to log Exception messages.
 * Priority of the logOutput: error > lifecycle > info > debug.
 * Example: Setting the loglevel to lifecycle means that lifecycle and error logs will be output
 */
public interface GretlLogger {

    public void info(String msg);

    public void debug(String msg);

    public void error(String msg, Throwable thrown);

    public void lifecycle(String msg);
}
