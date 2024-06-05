package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.DbTest;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.FileStylingDefinition;
import ch.so.agi.gretl.util.GretlException;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the SqlExecutorStep
 */
public class SqlExecutorStepTest {

    private static final String POSTGIS_VERSION_SQL_PATH = "data/sql/postgisversion.sql";
    private static final String CREATE_TEST_DB_SQL_PATH = "data/sql/create_test_db.sql";

    @ClassRule
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                .withDatabaseName(TestUtil.PG_DB_NAME)
                .withUsername(TestUtil.PG_DDLUSR_USR)
                .withInitScript(TestUtil.PG_INIT_SCRIPT_PATH)
                .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private final Connector connector;
    private final Connector duckDbConnector;

    public SqlExecutorStepTest() {
        LogEnvironment.initStandalone();
        connector = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        duckDbConnector = new Connector("jdbc:duckdb::memory:", null, null);
    }

    @Before
    public void initialize() throws Exception {
        createTestDb();
    }

    @After
    public void finalise() throws Exception {
        clearTestDb();
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
        List<File> sqlList = createCorrectSqlFiles();

        try {
            step.execute(null, sqlList);
        } catch (GretlException e) {
            assertEquals(GretlException.TYPE_NO_DB, e.getType());
        }
    }

    @Test
    public void executeWithWrongFileExtensions_ThrowsGretlException() throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = createCorrectSqlFiles();
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
        List<File> sqlList = createCorrectSqlFiles();
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
                    String.format("GretlException must be of type: %s", GretlException.TYPE_FILE_NOT_READABLE),
                    e.getType(),
                    GretlException.TYPE_FILE_NOT_READABLE
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
        List<File> sqlListe = createCorrectSqlFiles();

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

    @Category(DbTest.class)
    @Test
    public void executePostgisVersionTest() throws Exception {
        File inputFile = TestUtil.getResourceFile(POSTGIS_VERSION_SQL_PATH);
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
        List<File> sqlList = createCorrectSqlFiles();

        try {
            new SqlExecutorStep().execute(connector, sqlList);
        } catch (Exception e) {
            fail("Exception occurred: " + e);
        }

        assertTrue(connector.isClosed());
    }

    private void clearTestDb() throws Exception {
        try (Connection connection = connector.connect(); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            statement.execute("DROP TABLE colors");
        }
    }

    private void createTestDb() throws Exception {
        try (Connection connection = connector.connect()) {
            connection.setAutoCommit(true);

            SqlExecutorStep step = new SqlExecutorStep();
            File inputFile = TestUtil.getResourceFile(CREATE_TEST_DB_SQL_PATH);
            FileStylingDefinition.checkForUtf8(inputFile);
            step.execute(connector, Collections.singletonList(inputFile));
        }
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

    private List<File> createCorrectSqlFiles() throws Exception {
        String content = "INSERT INTO colors\nVALUES (124,252,0,'LawnGreen');\n\nDELETE FROM colors WHERE gruen=0;";
        File sqlFile = TestUtil.createTempFile(folder, content, "query.sql");
        content = "UPDATE colors\nSET farbname='grün'\nWHERE farbname='LawnGreen';";
        File sqlFile2 = TestUtil.createTempFile(folder, content, "query1.sql");
        return new ArrayList<>(Arrays.asList(sqlFile, sqlFile2));
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