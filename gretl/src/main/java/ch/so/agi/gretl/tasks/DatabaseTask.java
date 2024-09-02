package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.api.Connector;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

public abstract class DatabaseTask extends DefaultTask {
    private String dbUri;
    private String dbUser;
    private String dbPassword;

    @TaskAction
    public Connector createConnector() {
        if (dbUri == null || dbUser == null || dbPassword == null) {
            throw new IllegalArgumentException("dbUri, dbUser, and dbPassword must all be provided.");
        }

        return new Connector(dbUri, dbUser, dbPassword);
    }

    @Input
    @Optional
    public String getDbUri() {
        return dbUri;
    }

    public void setDbUri(String dbUri) {
        this.dbUri = dbUri;
    }

    @Input
    @Optional
    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    @Input
    @Optional
    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }
}
