package ch.so.agi.gretl.testutil;

import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class TestUtil {
    public static final String PG_CONNECTION_URI = System.getProperty("gretltest_dburi_pg");
    public static final String PG_DB_NAME = "gretl";
    public static final String PG_DDLUSR_USR = "ddluser";
    public static final String PG_DDLUSR_PWD = "ddluser";
    public static final String PG_DMLUSR_USR = "dmluser";
    public static final String PG_DMLUSR_PWD = "dmluser";
    public static final String PG_READERUSR_USR = "readeruser";
    public static final String PG_READERUSR_PWD = "readeruser";
    public static final String PG_INIT_SCRIPT_PATH = "data/sql/init_postgresql.sql";
    public static final String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";

    public static File createTempFile(TemporaryFolder folder, String stm, String fileName) throws IOException {
        File sqlFile = folder.newFile(fileName);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(sqlFile))) {
            bw.write(stm);
            return sqlFile;
        }
    }

    public static File getResourceFile(String resourcePath) throws Exception {
        Objects.requireNonNull(resourcePath);
        URL resourceUrl = TestUtil.class.getClassLoader().getResource(resourcePath);

        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found " + resourcePath);
        }

        return new File(resourceUrl.toURI());
    }
}