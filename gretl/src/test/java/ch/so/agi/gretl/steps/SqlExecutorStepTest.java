package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.DbTest;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.FileStylingDefinition;
import ch.so.agi.gretl.util.FileStylingDefinitionTest;
import ch.so.agi.gretl.util.GretlException;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the SqlExecutorStep
 */
public class SqlExecutorStepTest {

    @ClassRule
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                .withDatabaseName(TestUtil.PG_DB_NAME)
                .withUsername(TestUtil.PG_DDLUSR_USR)
                .withInitScript("init_postgresql.sql")
                .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    // Create temporary folder for saving sqlfiles
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private GretlLogger log;

    public SqlExecutorStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Before
    public void initialize() throws Exception {
        createTestDb(initializeConnector());
    }

    @After
    public void finalise() throws Exception {
        clearTestDb(initializeConnector());
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!postgres.isRunning()) {
            postgres.start();
        }
    }

    @AfterClass
    public static void tearDown() {
        if (postgres.isRunning()) {
            postgres.stop();
        }
    }

    private Connector initializeConnector() {
        return new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
    }

    @Test
    public void executeWithoutFilesThrowsGretlException() throws Exception {
        Connector connector = initializeConnector();
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = new ArrayList<>();

        try {
            step.execute(connector, sqlList);
        } catch (GretlException e) {
            Assert.assertEquals("no file", e.getType());
        }
    }

    @Test
    public void executeWithoutDbThrowsGretlException() throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = createCorrectSqlFiles();

        try {
            step.execute(null, sqlList);
        } catch (GretlException e) {
            Assert.assertEquals("no database", e.getType());
        }
    }

    @Test
    public void executeWithWrongFileExtensionsThrowsGretlException() throws Exception {
        Connector connector = initializeConnector();
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = createCorrectSqlFiles();
        sqlList.add(createSqlFileWithWrongExtension());

        try {
            step.execute(connector, sqlList);
        } catch (GretlException e) {
            Assert.assertEquals("no .sql-Extension", e.getType());
        }
    }

    @Test
    public void executeEmptyFileThrowsEmptyFileException() throws Exception {
        Connector connector = initializeConnector();
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlListe = createCorrectSqlFiles();
        sqlListe.add(createEmptySqlFile());

        try {
            step.execute(connector, sqlListe);
        } catch (EmptyFileException e) {
            Assert.assertTrue(e.getMessage().contains("File must not be empty"));
        }
    }

    @Test
    public void executeWithInexistentFilePathThrowsGretlException() throws Exception {
        Connector connector = initializeConnector();
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = new ArrayList<>();
        sqlList.add(new File("/inexistent/path/to/file.sql"));

        try {
            step.execute(connector, sqlList);
        } catch (GretlException e) {
            Assert.assertEquals(
                    String.format("GretlException must be of type: %s", GretlException.TYPE_FILE_NOT_READABLE),
                    e.getType(), GretlException.TYPE_FILE_NOT_READABLE
            );
        }
    }

    @Test
    public void executeWrongQueryThrowsSQLException() throws Exception {
        Connector connector = initializeConnector();
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = createWrongSqlFile();

        try {
            step.execute(connector, sqlList);
        } catch (SQLException e) {
            Assert.assertTrue(e.getMessage().contains("Error while executing the sqlstatement."));
        }
    }

    @Test
    public void executeSQLFileWithoutStatementThrowsGretlException() throws Exception {
        Connector connector = initializeConnector();
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlListe = createSqlFileWithoutStatement();

        try {
            step.execute(connector, sqlListe);
        } catch (GretlException e) {
            Assert.assertEquals("no statement in sql-file", e.getType());
        }
    }

    @Test
    public void executePositiveTest() throws Exception {
        Connector connector = initializeConnector();
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlListe = createCorrectSqlFiles();
        step.execute(connector, sqlListe);
    }
    
    @Test
    public void executeDuckDB_Ok() throws Exception {
        Connector connector = new Connector("jdbc:duckdb::memory:", null, null);
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = createSelectSqlFile();
        step.execute(connector, sqlList);
    }

    
    @Test
    public void executeDuckDB_Fail() throws Exception {
        Connector connector = new Connector("jdbc:duckdb::memory:", null, null);
        SqlExecutorStep step = new SqlExecutorStep();
        List<File> sqlList = createFailingSelectSqlFile();

        try {
            step.execute(connector, sqlList);
        } catch (SQLException e) {
            Assert.assertTrue(e.getMessage().contains("Referenced column \"asdf\" not found in FROM clause"));
        }
    }

    @Category(DbTest.class)
    @Test
    public void executePostgisVersionTest() throws Exception {
        Connector connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
        SqlExecutorStep step = new SqlExecutorStep();

        URL resourceUrl = getClass().getResource("data/sql/postgisversion.sql");
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found");
        }

        File inputFile = new File(resourceUrl.toURI());
        FileStylingDefinition.checkForUtf8(inputFile);
        List<File> sqlList = new ArrayList<>();
        sqlList.add(inputFile);
        step.execute(connector, sqlList);
    }

    @Test
    public void checkIfConnectionIsClosed() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createCorrectSqlFiles();

        x.execute(sourceDb, sqlListe);
        Assert.assertTrue(sourceDb.isClosed());
    }

    private void clearTestDb(Connector sourceDb) throws Exception {
        Connection con = sourceDb.connect();
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("DROP TABLE colors");
        con.close();
    }

    private void createTestDb(Connector sourceDb) throws Exception {
        Connection con = sourceDb.connect();
        con.setAutoCommit(true);
        createTableInTestDb(con);
        con.close();
    }

    private void createTableInTestDb(Connection con) throws Exception {
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " + "  rot integer, " + "  gruen integer, " + "  blau integer, "
                + "  farbname VARCHAR(200))");
        writeExampleDataInTestDB(con);
    }

    private void writeExampleDataInTestDB(Connection con) throws Exception {
        Statement stmt = con.createStatement();
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
    }

    private File createSqlFileWithWrongExtension() throws Exception {
        File sqlFile = folder.newFile("query.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" INSERT INTO colors\n" + "VALUES (124,252,0,'LawnGreen')");
        writer.close();
        return sqlFile;
    }

    private File createEmptySqlFile() throws Exception {
        File sqlFile1 = folder.newFile("query2.sql");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write("");
        writer1.close();
        return sqlFile1;
    }

    private List<File> createWrongSqlFile() throws Exception {
        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" INSERT INTO colors1\n" + "VALUES (124,252,0,'LawnGreen')");
        writer.close();
        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        return sqlListe;
    }

    private List<File> createSqlFileWithoutStatement() throws Exception {
        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" ;;;;; ; ; ");
        writer.close();

        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        return sqlListe;
    }

    private List<File> createCorrectSqlFiles() throws Exception {
        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(
                " INSERT INTO colors\n" + "VALUES (124,252,0,'LawnGreen')" + " ;    DELETE FROM colors WHERE gruen=0 ");
        writer.close();

        File sqlFile1 = folder.newFile("query1.sql");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write("UPDATE colors\n" + "set farbname='grün'\n" + "WHERE farbname='LawnGreen'\n");
        writer1.close();
        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        sqlListe.add(sqlFile1);
        return sqlListe;
    }
    
    private List<File> createSelectSqlFile() throws Exception {
        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT 1; ");
        writer.close();

        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        return sqlListe;
    }
    
    private List<File> createFailingSelectSqlFile() throws Exception {
        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT asdf; ");
        writer.close();

        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        return sqlListe;
    }

}