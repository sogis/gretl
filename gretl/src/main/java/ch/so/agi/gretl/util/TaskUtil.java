package ch.so.agi.gretl.util;

import ch.so.agi.gretl.api.Connector;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.util.List;

/**
 * Utility Class with methods used in the Tasks.
 */
public class TaskUtil {

    /**
     * Used to Convert a thrown Exception into a GradleException. GradleException
     * must be thrown to halt the Execution of the gradle build.
     * GretlException intances are converted, all other Exceptions are wrapped
     * (nested)
     */
    public static GradleException toGradleException(Exception ex) {
        GradleException exception;

        String exClassName = ex.getClass().toString();
        String gretlClassName = GretlException.class.toString();

        if (exClassName.equals(gretlClassName)) { // can't use instanceof as must return false for GretlException
                                                  // subclasses.
            exception = new GradleException(ex.getMessage());
        } else {
            exception = new GradleException("Inner Exception Message: " + ex.getMessage(), ex);
        }
        return exception;
    }

    /**
     * Converts the given path relative to the gradle project to a absolute path and
     * returns the absolute path.
     */
    public static File createAbsolutePath(Object filePath, Project gradleProject) {
        return gradleProject.file(filePath);
    }

    public static Connector getDatabaseConnectorObject(List<String> databaseDetails) {
        if (databaseDetails == null || databaseDetails.isEmpty() || databaseDetails.size() > 3) {
            throw new IllegalArgumentException("At least the database URI is required.");
        }

        String databaseUri = databaseDetails.get(0);
        String databaseUser = databaseDetails.size() > 1 ? databaseDetails.get(1) : null;
        String databasePassword = databaseDetails.size() > 2 ? databaseDetails.get(2) : null;

        return new Connector(databaseUri, databaseUser, databasePassword);
    }
}
