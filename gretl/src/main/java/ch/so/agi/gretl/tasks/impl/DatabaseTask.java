package ch.so.agi.gretl.tasks.impl;

import ch.so.agi.gretl.api.Connector;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;

import java.util.List;

public abstract class DatabaseTask extends DefaultTask {
    private Connector database;

    @Input
    public Connector getDatabase() {
        return database;
    }

    public void setDatabase(List<String> databaseDetails) {
        if (databaseDetails.size() != 3) {
            throw new IllegalArgumentException("Values for db_uri, db_user, db_pass are required.");
        }

        String databaseUri = databaseDetails.get(0);
        String databaseUser = databaseDetails.get(1);
        String databasePassword = databaseDetails.get(2);

        this.database = new Connector(databaseUri, databaseUser, databasePassword);
    }
}
