package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.FileStylingDefinition;
import ch.so.agi.gretl.util.GretlException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for the SqlExecutorStep
 */
@Testcontainers
public class SqlExecutorStepTest {

    @Container
    public PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                .withDatabaseName(TestUtil.PG_DB_NAME)
                .withUsername(TestUtil.PG_DDLUSR_USR)
                .withInitScript(TestUtil.PG_INIT_SCRIPT_PATH)
                .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @TempDir
    public Path folder;
    private final Connector connector;
    private final Connector duckDbConnector;

    public SqlExecutorStepTest() {
        LogEnvironment.initStandalone();
        connector = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        duckDbConnector = new Connector("jdbc:duckdb::memory:", null, null);
    }

    @BeforeEach
    public void before() throws Exception {
        TestUtil.createTestDb(this.connector, TestUtil.CREATE_TEST_DB_SQL_PATH);
    }

    @AfterEach
    public void after() throws Exception {
        TestUtil.clearTestDb(connector);
        if (!connector.isClosed()) {
            connector.close();
        }

        if (!duckDbConnector.isClosed()) {
            duckDbConnector.close();
        }
    }

    @Test
    public void executeWithoutFiles_ThrowsGretlException() throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = new ArrayList<>();

        try {
            step.execute(connector, sqlList);
        } catch (GretlException e) {
            assertEquals(GretlException.TYPE_NO_FILE, e.getType());
        }
    }

    @Test
    public void executeWithoutDb_ThrowsGretlException() throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = getCorrectSqlFiles();

        try {
            step.execute(null, sqlList);
        } catch (GretlException e) {
            assertEquals(GretlException.TYPE_NO_DB, e.getType());
        }
    }

    @Test
    public void executeWithWrongFileExtensions_ThrowsGretlException() throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = getCorrectSqlFiles();
        sqlList.add(createSqlFileWithWrongExtension());

        try {
            step.execute(connector, sqlList);
        } catch (GretlException e) {
            assertEquals(GretlException.TYPE_WRONG_EXTENSION, e.getType());
        }
    }

    @Test
    public void executeEmptyFile_ThrowsEmptyFileException() throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = getCorrectSqlFiles();
        sqlList.add(createEmptySqlFile());

        try {
            step.execute(connector, sqlList);
        } catch (EmptyFileException e) {
            assertTrue(e.getMessage().contains("File must not be empty"));
        }
    }

    @Test
    public void executeWithInexistentFilePath_ThrowsGretlException() throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = Collections.singletonList(new File("/nonexistent/path/to/file.sql"));

        try {
            step.execute(connector, sqlList);
        } catch (GretlException e) {
            assertEquals(
                    GretlException.TYPE_FILE_NOT_READABLE,
                    e.getType(),
                    String.format("GretlException must be of type: %s", GretlException.TYPE_FILE_NOT_READABLE)
            );
        }
    }

    @Test
    public void executeWrongQuery_ThrowsSQLException() throws Exception {
        List<File> sqlList = createWrongSqlFile();

        try {
            new SqlExecutorStep().execute(connector, sqlList);
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("Error while executing the sqlstatement."));
        }
    }

    @Test
    public void executeSQLFileWithoutStatement_ThrowsGretlException() throws Exception {
        List<File> sqlListe = createSqlFileWithoutStatement();

        try {
            new SqlExecutorStep().execute(connector, sqlListe);
        } catch (GretlException e) {
            assertEquals("no statement in sql-file", e.getType());
        }
    }

    @Test
    public void executePositiveTest_Ok() throws Exception {
        List<File> sqlListe = getCorrectSqlFiles();

        try {
            new SqlExecutorStep().execute(connector, sqlListe);
        } catch (Exception e) {
            fail("Exception occurred: " + e);
        }
    }
    
    @Test
    public void executeDuckDB_Ok() throws Exception {
        List<File> sqlList = createSelectSqlFile();

        try {
            new SqlExecutorStep().execute(duckDbConnector, sqlList);
        } catch (Exception e) {
            fail("Exception occurred: " + e);
        }
    }
    
    @Test
    public void executeDuckDB_Fail() throws Exception {
        List<File> sqlList = createFailingSelectSqlFile();

        try {
            new SqlExecutorStep().execute(duckDbConnector, sqlList);
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("Referenced column \"asdf\" not found in FROM clause"));
        }
    }

    @Test
    @Tag(TestTags.DB_TEST)
    public void executePostgisVersionTest() throws Exception {
        File inputFile = TestUtil.getResourceFile(TestUtil.POSTGIS_VERSION_SQL_PATH);
        FileStylingDefinition.checkForUtf8(inputFile);
        List<File> sqlList = Collections.singletonList(inputFile);

        try {
            new SqlExecutorStep().execute(
                    new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD),
                    sqlList
            );
        } catch (Exception e) {
            fail("Exception occurred: " + e);
        }
    }

    @Test
    public void checkIfConnectionIsClosed() throws Exception {
        List<File> sqlList = getCorrectSqlFiles();

        try {
            new SqlExecutorStep().execute(connector, sqlList);
        } catch (Exception e) {
            fail("Exception occurred: " + e);
        }

        assertTrue(connector.isClosed());
    }

    private List<File> getCorrectSqlFiles() throws Exception {
        return new ArrayList<>(Arrays.asList(
                TestUtil.getResourceFile(TestUtil.COLORS_INSERT_DELETE_SQL_PATH),
                TestUtil.getResourceFile(TestUtil.COLORS_UPDATE_FARBNAME_SQL_PATH)
            )
        );
    }

    private File createSqlFileWithWrongExtension() throws Exception {
        String content = "INSERT INTO colors\nVALUES (124,252,0,'LawnGreen');";
        return TestUtil.createTempFile(folder, content, "query.txt");
    }

    private File createEmptySqlFile() throws Exception {
        return TestUtil.createTempFile(folder, "", "query2.sql");
    }

    private List<File> createWrongSqlFile() throws Exception {
        String content = "INSERT INTO colors1\nVALUES (124,252,0,'LawnGreen');";
        File sqlFile = TestUtil.createTempFile(folder, content, "query.sql");
        return new ArrayList<>(Collections.singletonList(sqlFile));
    }

    private List<File> createSqlFileWithoutStatement() throws Exception {
        String content = " ;;;;; ; ; ";
        File sqlFile = TestUtil.createTempFile(folder, content, "query.sql");
        return new ArrayList<>(Collections.singletonList(sqlFile));
    }

    private List<File> createSelectSqlFile() throws Exception {
        String content = "SELECT 1;";
        File sqlFile = TestUtil.createTempFile(folder, content, "query.sql");
        return new ArrayList<>(Collections.singletonList(sqlFile));
    }
    
    private List<File> createFailingSelectSqlFile() throws Exception {
        String content = "SELECT asdf;";
        File sqlFile = TestUtil.createTempFile(folder, content, "query.sql");
        return new ArrayList<>(Collections.singletonList(sqlFile));
    }
}