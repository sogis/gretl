package ch.so.agi.gretl.steps.publisher.util.env;

public final class PubFolderEnv {
    private final String path;
    private final String user;
    private final String password;

    public PubFolderEnv(String path, String user, String password) {
        this.path = path;
        this.user = user;
        this.password = password;
    }

    public String getPath() {
        return path;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
