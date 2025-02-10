package ch.so.agi.gretl.api;

import ch.so.agi.gretl.util.DbConnector;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Class which is used get a connection to the database
 */
public class Connector implements Serializable {

    private String dbUri;
    private String dbUser;
    private String dbPassword;
    private transient GretlLogger log;
    private transient Connection dbConnection = null;

    public Connector(String dbUri) {
        this(dbUri, null, null);
    }

    public Connector(String dbUri, String dbUser) {
        this(dbUri, dbUser, null);
    }

    public Connector(String dbUri, String dbUser, String dbPassword) {
        this.dbUri = dbUri;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    public Connection connect() throws SQLException {
        if (dbConnection == null) {
            dbConnection = DbConnector.connect(dbUri, dbUser, dbPassword);
            dbConnection.setAutoCommit(false);
        }
        return dbConnection;
    }
    public void close() throws SQLException {
        if (dbConnection != null) {
            Connection con=dbConnection;
            dbConnection=null;
            con.close();
        }
        return;
    }
    public boolean isClosed() {
        return (dbConnection == null);
    }

    public String getDbUri() {
        return dbUri;
    }

    public void setDbUri(String dbUri) {
        this.dbUri = dbUri;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String toString() {
        int passLength = 0;
        if (dbPassword != null)
            passLength = dbPassword.length();

        char[] starArray = new char[passLength];
        Arrays.fill(starArray, "*".toCharArray()[0]);

        String res = String.format("Connection( DbUri: %s, DbUser: %s DbPass: %s)", dbUri, dbUser,
                new String(starArray));
        return res;
    }
    
    
}
