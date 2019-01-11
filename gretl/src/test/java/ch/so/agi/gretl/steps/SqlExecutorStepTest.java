package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.DbTest;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.GretlException;

import static org.hamcrest.CoreMatchers.containsString;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 * Tests for the SqlExecutorStep
 */
public class SqlExecutorStepTest {
    private GretlLogger log;

    public SqlExecutorStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    //Create temporary folder for saving sqlfiles
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void initialize() throws Exception {
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        createTestDb(sourceDb);
    }

    @After
    public void finalise() throws Exception {
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        clearTestDb(sourceDb);
    }

    @Test
    public void executeWithoutFilesThrowsGretlException() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = new ArrayList<>();

        try {
            x.execute(sourceDb,sqlListe);
        } catch (GretlException e) {
            Assert.assertEquals("no file", e.getType());
        }
    }

    @Test
    public void executeWithoutDbThrowsGretlException() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = null;
        List<File> sqlListe = createCorrectSqlFiles();

        try {
            x.execute(sourceDb,sqlListe);
        } catch (GretlException e) {
            Assert.assertEquals("no database", e.getType());
        }
    }


    @Test
    public void executeWithWrongFileExtensionsThrowsGretlException() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();

        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createCorrectSqlFiles();
        sqlListe.add(createSqlFileWithWrongExtension());

        try {
            x.execute(sourceDb,sqlListe);
        } catch (GretlException e) {
            Assert.assertEquals("no .sql-Extension", e.getType());
        }
    }


    @Test
    public void executeEmptyFileThrowsEmptyFileException() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File>sqlListe = createCorrectSqlFiles();
        sqlListe.add(createEmptySqlFile());

        try {
            x.execute(sourceDb, sqlListe);
        } catch (EmptyFileException e) {
            Assert.assertThat(e.getMessage(), containsString("File must not be empty"));

        }
    }

    @Test
    public void executeWithInexistentFilePathThrowsGretlException() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File>sqlListe = new ArrayList<>();
        sqlListe.add(new File("/inexistent/path/to/file.sql"));

        try {
            x.execute(sourceDb, sqlListe);
        } catch (GretlException e) {
            Assert.assertEquals(
                    "GretlException must be of type: " + GretlException.TYPE_FILE_NOT_READABLE,
                    e.getType(),
                    GretlException.TYPE_FILE_NOT_READABLE);
        }
    }


    @Test
    public void executeWrongQueryThrowsSQLException() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createWrongSqlFile();

        try {
            x.execute(sourceDb, sqlListe);
        } catch (SQLException e) {
            Assert.assertThat(e.getMessage(), containsString("Error while executing the sqlstatement."));
        }

    }

    @Test
    public void executeSQLFileWithoutStatementThrowsGretlException() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createSqlFileWithoutStatement();

        try {
            x.execute(sourceDb, sqlListe);
        } catch (GretlException e) {
            Assert.assertEquals("no statement in sql-file", e.getType());
        }

    }


    @Test
    public void executePositiveTest() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createCorrectSqlFiles();

        x.execute(sourceDb,sqlListe);
    }

    @Category(DbTest.class)
    @Test
    public void executePostgisVersionTest() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        System.err.println(TestUtil.PG_CONNECTION_URI);
        Connector sourceDb = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);

        File sqlFile = folder.newFile("postgisversion.sql");
        FileWriter sqlWriter=null;
        try {
            sqlWriter=new FileWriter(sqlFile);
            sqlWriter.write("SELECT PostGIS_Full_Version();");
        }finally {
            if(sqlWriter!=null) {
                sqlWriter.close();
                sqlWriter=null;
            }
        }
        List<File> sqlListe = new ArrayList<File>();
        sqlListe.add(sqlFile);
        
        x.execute(sourceDb,sqlListe);
    }

    @Test
    public void checkIfConnectionIsClosed() throws Exception{
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createCorrectSqlFiles();

        x.execute(sourceDb,sqlListe);
        Assert.assertTrue(sourceDb.connect().isClosed());
    }

    private void clearTestDb(Connector sourceDb) throws Exception {
        Connection con = sourceDb.connect();
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("DROP TABLE colors");
        con.close();
    }

    private void createTestDb(Connector sourceDb )
            throws Exception{
        Connection con = sourceDb.connect();
        con.setAutoCommit(true);
        createTableInTestDb(con);
        con.close();
    }

    private void createTableInTestDb(Connection con) throws Exception {
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        writeExampleDataInTestDB(con);
    }

    private void writeExampleDataInTestDB(Connection con) throws Exception{
        Statement stmt = con.createStatement();
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
    }

    private File createSqlFileWithWrongExtension() throws Exception{
        File sqlFile =  folder.newFile("query.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" INSERT INTO colors\n" +
                "VALUES (124,252,0,'LawnGreen')");
        writer.close();
        return sqlFile;
    }

    private File createEmptySqlFile() throws Exception {
        File sqlFile1 =  folder.newFile("query2.sql");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write("");
        writer1.close();
        return sqlFile1;
    }

    private List<File> createWrongSqlFile() throws Exception {
        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" INSERT INTO colors1\n" +
                "VALUES (124,252,0,'LawnGreen')");
        writer.close();
        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        return sqlListe;
    }

    private List<File> createSqlFileWithoutStatement() throws Exception {
        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" ;;;;; ; ; ");
        writer.close();

        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        return sqlListe;
    }

    private List<File> createCorrectSqlFiles() throws Exception {
        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" INSERT INTO colors\n" +
                "VALUES (124,252,0,'LawnGreen')"+
                " ;    DELETE FROM colors WHERE gruen=0 ");
        writer.close();

        File sqlFile1 =  folder.newFile("query1.sql");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write("UPDATE colors\n" +
                "set farbname='grün'\n" +
                "WHERE farbname='LawnGreen'\n");
        writer1.close();
        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        sqlListe.add(sqlFile1);
        return sqlListe;
    }
}