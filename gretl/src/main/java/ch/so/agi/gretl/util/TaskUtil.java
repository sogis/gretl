package ch.so.agi.gretl.util;

import ch.so.agi.gretl.api.Connector;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;

import java.io.File;
import java.util.List;

/**
 * Utility Class with methods used in the Tasks.
 */
public class TaskUtil {

    /**
     * Used to Convert a thrown Exception into a GradleException. GradleException
     * must be thrown to halt the Execution of the gradle build.
     *
     * GretlException intances are converted, all other Exceptions are wrapped
     * (nested)
     */
    public static GradleException toGradleException(Exception ex) {
        GradleException res = null;

        String exClassName = ex.getClass().toString();
        String gretlClassName = GretlException.class.toString();

        if (exClassName.equals(gretlClassName)) { // can't use instanceof as must return false for GretlException
                                                  // subclasses.
            res = new GradleException(ex.getMessage());
        } else {
            res = new GradleException("Inner Exception Message: " + ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * Converts the given path relative to the gradle project to a absolute path and
     * returns the absolute path.
     */
    public static File createAbsolutePath(Object filePath, Project gradleProject) {
        File absolute = gradleProject.file(filePath);

        return absolute;
    }

    public static Connector getDatabaseConnectorObject(List<String> databaseDetails){
        if (databaseDetails.size() != 3) {
            throw new IllegalArgumentException("Values for db_uri, db_user, db_pass are required.");
        }
        String databaseUri = databaseDetails.get(0);
        String databaseUser = databaseDetails.get(1);
        String databasePassword = databaseDetails.get(2);
        return new Connector(databaseUri, databaseUser, databasePassword);
    }
}
