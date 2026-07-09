package ch.so.agi.gretl.steps.publisher.util.env;

public final class PupDateEnv {
    private final String connectionUrl;
    private final String dbSchema;
    private final String user;
    private final String password;

    public PupDateEnv(String connectionUrl, String dbSchema, String user, String password) {
        this.connectionUrl = connectionUrl;
        this.dbSchema = dbSchema;
        this.user = user;
        this.password = password;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
