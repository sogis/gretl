package ch.so.agi.gretl.testutil;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.steps.SqlExecutorStep;
import ch.so.agi.gretl.util.FileStylingDefinition;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
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
    public static final String CREATE_TEST_DB_SQL_PATH = "data/sql/create_test_db.sql";
    public static final String CLEAR_TEST_DB_SQL_PATH = "data/sql/clear_test_db.sql";
    public static final String INSERT_COLORS_COPY_DATA_SQL_PATH = "data/sql/insert_colors_copy_data.sql";

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

    public static void createTestDb(Connector connector, String resourceFile) throws Exception {
        try (Connection connection = connector.connect()) {
            connection.setAutoCommit(true);
            File inputFile = getResourceFile(resourceFile);
            FileStylingDefinition.checkForUtf8(inputFile);
            execute(connector, inputFile);
        }
    }

    public static void clearTestDb(Connector connector) throws Exception {
        try (Connection connection = connector.connect()) {
            connection.setAutoCommit(true);
            File inputFile = getResourceFile(CLEAR_TEST_DB_SQL_PATH);
            FileStylingDefinition.checkForUtf8(inputFile);
            execute(connector, inputFile);
        }
    }

    public static void execute(Connector connector, File inputFile) throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        step.execute(connector, Collections.singletonList(inputFile));
    }
}