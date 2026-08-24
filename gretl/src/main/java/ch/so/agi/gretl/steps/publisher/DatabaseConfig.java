package ch.so.agi.gretl.steps.publisher;

import java.util.Objects;

import ch.so.agi.gretl.api.Connector;

/** Immutable database connection configuration for a publication. */
public final class DatabaseConfig {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseConfig(String url, String user, String password) {
        this.url = normalize(url);
        this.user = normalize(user);
        this.password = password;
    }

    public static DatabaseConfig from(Connector connector) {
        Objects.requireNonNull(connector, "connector must not be null");
        return new DatabaseConfig(connector.getDbUri(), connector.getDbUser(), connector.getDbPassword());
    }

    public Connector createConnector() {
        return new Connector(url, user, password);
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
