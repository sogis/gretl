package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.TransferSet;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.DbTest;
import ch.so.agi.gretl.util.DbConnector;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.GretlException;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static org.gradle.internal.impldep.org.testng.AssertJUnit.assertEquals;
import static org.junit.Assert.fail;

public class Db2DbStepTest {

    private static final String GEOM_WKT = "LINESTRING(2600000 1200000,2600001 1200001)";

    //Konstruktor//
    public Db2DbStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String e;
    private GretlLogger log;

    @After
    public void finalise() throws Exception {
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        clearTestDb(sourceDb);
    }

    @Test
    public void FaultFreeExecutionTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors; ", "query.sql");
            File sqlFile2 = TestUtil.createFile(folder, "SELECT * FROM colors; ", "query2.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
            ));
            mylist.add(new TransferSet(
                    sqlFile2.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);

            ResultSet rs = con.connect().createStatement().executeQuery("SELECT * FROM colors_copy WHERE farbname = 'blau'");
            while(rs.next()) {
                assertEquals(rs.getObject("rot"),0);
                assertEquals(rs.getObject("farbname"),"blau");
            }
        } finally {
            con.connect().close();
        }


    }
    @Test
    public void newlineAtEndOfFileTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = folder.newFile("query.sql");
            FileWriter sqlWriter=null;
            try {
                sqlWriter=new FileWriter(sqlFile);
                sqlWriter.write("SELECT * FROM colors;");
                sqlWriter.write(System.getProperty("line.separator"));
                //sqlWriter.write("SELECT * FROM colors;");
            }finally {
                if(sqlWriter!=null) {
                    sqlWriter.close();
                    sqlWriter=null;
                }
            }
            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", true
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
        } finally {
            con.connect().close();
        }


    }
    @Test
    public void fileWithMultipleStmtTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = folder.newFile("query.sql");
            FileWriter sqlWriter=null;
            try {
                sqlWriter=new FileWriter(sqlFile);
                sqlWriter.write("SELECT * FROM colors;");
                sqlWriter.write(System.getProperty("line.separator"));
                sqlWriter.write("SELECT * FROM colors;");
            }finally {
                if(sqlWriter!=null) {
                    sqlWriter.close();
                    sqlWriter=null;
                }
            }
            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", true
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            fail();
        }catch(IOException ex) {
            
        } finally {
            con.connect().close();
        }


    }

    @Test
    public void Db2DbEmptyFileTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = folder.newFile("query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

           Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
           Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("EmptyFileException müsste geworfen werden");
        } catch (EmptyFileException e) {

        } finally {
            con.connect().close();
        }

    }

    //todo was tested diese methode? Fehler bei leerer sql datei oder Fehler bei falschem sql?
    //--> Bitte aufräumen und methode besser benennen
    @Test
    public void SQLExceptionTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT BLABLABLA FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("EmptyFileException müsste geworfen werden");

        } catch (SQLException e) {
            log.debug("Got SQLException as expected");
        } finally{
            con.connect().close();
        }
    }

    @Test
    public void ColumnNumberTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        try {
            //Hier müssen die Tabellen manuell erstellt werden, da die Tabelle colors_copy
            //ja gerade mit nicht genug Spalten angelegt werden soll!
            Statement stmt = con.createStatement();
            stmt.execute("CREATE TABLE colors ( " +
                    "  rot integer, " +
                    "  gruen integer, " +
                    "  blau integer, " +
                    "  farbname VARCHAR(200))");
            stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
            stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
            stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");

            stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer)");

            File sqlFile = TestUtil.createFile(folder, "SELECT rot, gruen, blau, farbname FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");

        } catch (GretlException ge) {
            boolean isMismatchException = GretlException.TYPE_COLUMN_MISMATCH.equals(ge.getType());
            if(!isMismatchException){
                throw ge;
            }

        } finally {
            con.close();
        }
    }

    @Test
    public void IncompatibleDataTypeTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        try {
            Statement stmt = con.createStatement();

            stmt.execute("CREATE TABLE colors ( " +
                    "  rot integer, " +
                    "  gruen integer, " +
                    "  blau integer, " +
                    "  farbname VARCHAR(200))");
            stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
            stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
            stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");

            stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname integer)");

            File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");

        } catch (SQLException e) {
            log.debug("Got SQLException as expected");
        } finally {
            con.close();
        }
    }

    @Test
    public void CopyEmptyTableToOtherTableTest() throws Exception {

        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
        } finally {
            con.connect().close();
        }
    }

    @Test
    public void DeleteTest() throws Exception {
        //Datenbank für Test vorbereiten
        Connection con = null;
        try {
            Connector connector = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            con = connector.connect();
            createTestDb(connector);
            Statement stmt = con.createStatement();
            stmt.execute("INSERT INTO colors_copy  VALUES (255,0,0,'rot')");
            stmt.execute("INSERT INTO colors_copy  VALUES (251,0,0,'rot')");
            stmt.execute("INSERT INTO colors_copy  VALUES (0,0,255,'blau')");
            stmt.execute("INSERT INTO colors_copy  VALUES (251,0,0,'rot')");
            stmt.execute("INSERT INTO colors_copy  VALUES (67,2,255,'blauauaua')");
            stmt.execute("INSERT INTO colors_copy  VALUES (251,45,23,'rotototo')");
            stmt.execute("INSERT INTO colors_copy  VALUES (67,3,255,'blauwederenzian')");
            con.commit();
            //Vorbereitungsverbindung schliessen
        } finally {
            con.close();
        }

        File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors;", "query.sql");

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
        ));

        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB", "bjsvwsch", null);
        Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB", "bjsvwsch", null);


        Db2DbStep db2db = new Db2DbStep();
        //db2dbstep ausführen
        db2db.processAllTransferSets(sourceDb, targetDb, mylist);

        //Select auf db um korrektes ausführen zu verifizieren
        Connection con2 = null;
        try {
            Connector connector2 = new Connector("jdbc:derby:memory:myInMemDB", "bjsvwsch", null);
            con2 = connector2.connect();
            ResultSet rs = con2.createStatement().executeQuery("SELECT COUNT(*) FROM colors_copy");
            rs.next();
            int i = rs.getInt(1);
            con2.commit();
            assertEquals(i, 3);
        } finally {
            con2.close();
        }


    }

    @Test
    public void CloseConnectionsTest() throws Exception {

        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);

            Assert.assertTrue("SourceConnection is not closed", sourceDb.connect().isClosed());
            Assert.assertTrue("TargetConnection is not closed", targetDb.connect().isClosed());

        } finally {
            con.connect().close();
        }
    }


    @Test
    public void CloseConnectionsAfterFailedTest() throws Exception {

        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT güggeliblau FROM colors_copy", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            try {
                db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            } catch (SQLException e) {
                log.debug("Got SQLException as expected");
            } catch (Exception e) {
                log.debug("Got Exception as expected");
            }

            Assert.assertTrue("SourceConnection is not closed", sourceDb.connect().isClosed());
            Assert.assertTrue("TargetConnection is not closed", targetDb.connect().isClosed());
        } finally {
            con.connect().close();
        }
    }

    @Category(DbTest.class)
    @Test
    public void canWriteGeomFromWkbTest() throws Exception {
        String schemaName = "GeomFromWkbTest";

        Connection con = null;

        try{
            con =  connectToPreparedPgDb(schemaName);
            preparePgGeomSourceSinkTables(schemaName, con);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createFile(folder, 
                    String.format("select ST_AsBinary(geom) as geom from %s.source", schemaName),
                    "select.sql");

            Connector src = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
            Connector sink = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[]{"geom:wkb:2056"}
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);

            dropSchema(schemaName, con);
        }
        finally {
            if(con != null)
                con.close();
        }
    }

    @Category(DbTest.class)
    @Test
    public void canWriteGeomFromWktTest() throws Exception {
        String schemaName = "GeomFromWktTest";

        Connection con = null;

        try{
            con = connectToPreparedPgDb(schemaName);
            preparePgGeomSourceSinkTables(schemaName, con);


            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createFile(folder, 
                    String.format("select ST_AsText(geom) as geom from %s.source", schemaName),
                    "select.sql");

            Connector src = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
            Connector sink = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[]{"geom:wkt:2056"}
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);
        }
        finally {
            dropSchema(schemaName, con);

            if(con != null)
                con.close();
        }
    }

    @Category(DbTest.class)
    @Test
    public void canWriteGeomFromGeoJsonTest() throws Exception {
        String schemaName = "GeomFromGeoJsonTest";

        Connection con = null;

        try{
            con = connectToPreparedPgDb(schemaName);
            preparePgGeomSourceSinkTables(schemaName, con);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createFile(folder, 
                    String.format("select ST_AsGeoJSON(geom) as geom from %s.source", schemaName),
                    "select.sql");

            Connector src = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
            Connector sink = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[]{"geom:geojson:2056"}
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);
        }
        finally {
            dropSchema(schemaName, con);

            if(con != null)
                con.close();
        }
    }

    /**
     * Test's loading several hundred thousand rows from sqlite to postgis.
     * Loading 300'000 rows should take about 15 seconds
     */
    @Category(DbTest.class)
    @Test
    public void positiveBulkLoadPostgisTest() throws Exception {
        int numRows = 300000;
        String schemaName = "BULKLOAD2POSTGIS";

        Connection srcCon = null;
        Connection targetCon = null;

        try{
            File sqliteDb = createTmpDb(schemaName);
            srcCon = connectSqlite(sqliteDb);
            createSqliteSrcTable(numRows, srcCon);

            targetCon = connectToPreparedPgDb(schemaName);
            prepareSinkTable(schemaName, targetCon);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createFile(folder, "select myint, myfloat, mytext, mywkt as mygeom from dtypes","select.sql");

            Connector src = new Connector("jdbc:sqlite:" + sqliteDb.getAbsolutePath());
            Connector sink = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".DTYPES",
                    true,
                    new String[]{"mygeom:wkt:2056"}
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            //Test considered OK if all values are transferred and Geom OK
            String checkSQL = String.format("SELECT COUNT(*) FROM %s.DTYPES", schemaName);

            Statement checkStmt = targetCon.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkSQL);
            rs.next();

            Assert.assertEquals("Check Statement must return exactly " + numRows, numRows, rs.getInt(1));
        }
        finally {
            dropSchema(schemaName, targetCon);

            if(srcCon != null){ srcCon.close(); }
            if(targetCon != null){ targetCon.close(); }
        }
    }

    /**
     * Tests if the sqlite datatypes and geometry as wkt are transferred
     * faultfree from sqlite to postgis
     */
    @Category(DbTest.class)
    @Test
    public void positiveSqlite2PostgisTest() throws Exception {
        String schemaName = "SQLITE2POSTGIS";

        Connection srcCon = null;
        Connection targetCon = null;

        try{
            File sqliteDb = createTmpDb(schemaName);
            srcCon = connectSqlite(sqliteDb);
            createSqliteSrcTable(1, srcCon);

            targetCon = connectToPreparedPgDb(schemaName);
            prepareSinkTable(schemaName, targetCon);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createFile(folder, "select myint, myfloat, mytext, mywkt as mygeom from dtypes","select.sql");

            Connector src = new Connector("jdbc:sqlite:" + sqliteDb.getAbsolutePath());
            Connector sink = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".DTYPES",
                    true,
                    new String[]{"mygeom:wkt:2056"}
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            //Test considered OK if all values are transferred and Geom OK
            String checkSQLRaw = "SELECT COUNT(*) FROM %s.DTYPES " +
                    "WHERE MYINT IS NOT NULL AND MYFLOAT IS NOT NULL AND MYTEXT IS NOT NULL AND " +
                    "ST_Equals(MYGEOM, ST_GeomFromText('%s', 2056)) = True";
            String checkSql = String.format(checkSQLRaw, schemaName, GEOM_WKT);

            Statement checkStmt = targetCon.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkSql);
            rs.next();

            Assert.assertEquals("Check Statement must return exactly one row", 1, rs.getInt(1));
        }
        finally {
            dropSchema(schemaName, targetCon);

            if(srcCon != null){ srcCon.close(); }
            if(targetCon != null){ targetCon.close(); }
        }
    }

    private static void prepareSinkTable(String schemaName, Connection con) throws SQLException{
        String create = String.format(
                "CREATE TABLE %s.DTYPES(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYGEOM GEOMETRY(LINESTRING,2056))",
                schemaName);

        Statement s = con.createStatement();
        s.addBatch(create);

        String grant = String.format("GRANT SELECT, INSERT, DELETE ON ALL TABLES IN SCHEMA %s TO %s", schemaName, TestUtil.PG_DMLUSR_USR);
        s.addBatch(grant);
        s.executeBatch();

        con.commit();
    }

    private static void createSqliteSrcTable(int numRows, Connection con) throws SQLException{
        String create = "CREATE TABLE DTYPES(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYWKT TEXT)";
        Statement sCreate = con.createStatement();
        sCreate.execute(create);

        Random random = new Random();

        PreparedStatement ps = con.prepareStatement("INSERT INTO DTYPES VALUES(?, ?, ?, ?)");
        for(int i=0; i<numRows; i++){
            ps.setInt(1, random.nextInt());
            ps.setDouble(2,random.nextDouble());
            ps.setString(3, UUID.randomUUID().toString());
            ps.setString(4, GEOM_WKT);
            ps.addBatch();
        }

        ps.executeBatch();
        ps.close();

        con.commit();
    }

    private static void createSqliteTargetTable(Connection con) throws SQLException{
        String create = "CREATE TABLE DTYPES(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYDATE TEXT, MYTIME TEXT, " +
                "MYUUID TEXT, MYGEOM_WKT TEXT)";

        Statement sCreate = con.createStatement();
        sCreate.execute(create);

        con.commit();
    }

    private static File createTmpDb(String name) throws  IOException{
        Path dir = Files.createTempDirectory(null);
        Path file = Paths.get(name + ".sqlite");

        Path dbPath = dir.resolve(file);
        return dbPath.toFile();
    }

    private static Connection connectSqlite(File dbLocation) throws SQLException{
        String url = "jdbc:sqlite:" + dbLocation.getAbsolutePath();

        Connection con = DriverManager.getConnection(url);
        con.setAutoCommit(false);

        return con;
    }

    /**
     * Tests if the "special" datatypes (Date, Time, GUID, Geometry, ..) are transferred
     * faultfree from Postgis to sqlite
     */
    @Category(DbTest.class)
    @Test
    public void positivePostgis2SqliteTest() throws Exception {
        String schemaName = "POSTGIS2SQLITE";

        Connection srcCon = null;
        Connection targetCon = null;

        try{
            srcCon = connectToPreparedPgDb(schemaName);
            prepareSrcTable(schemaName, srcCon);

            File sqliteDb = createTmpDb(schemaName);
            targetCon = connectSqlite(sqliteDb);
            createSqliteTargetTable(targetCon);

            Db2DbStep step = new Db2DbStep();
            String select = String.format(
                    "select myint, myfloat, mytext, mydate, mytime, myuuid, ST_AsText(mygeom) as mygeom_wkt from %s.dtypes",
                    schemaName
            );
            File queryFile = TestUtil.createFile(folder, select,"select.sql");


            Connector src = new Connector(TestUtil.PG_CONNECTION_URI, TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
            Connector sink = new Connector("jdbc:sqlite:" + sqliteDb.getAbsolutePath());
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    "dtypes",
                    true
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            //Test considered OK if all values are transferred and Geom OK
            String checkSQL = String.format("SELECT COUNT(*) FROM DTYPES WHERE " +
                    "MYINT IS NOT NULL AND MYFLOAT IS NOT NULL AND MYTEXT IS NOT NULL AND MYDATE IS NOT NULL AND MYTIME IS NOT NULL AND MYUUID IS NOT NULL AND " +
                    "MYGEOM_WKT = '%s'",
                    GEOM_WKT);

            Statement checkStmt = targetCon.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkSQL);
            rs.next();

            Assert.assertEquals("Check Statement must return exactly one row", 1, rs.getInt(1));
        }
        finally {
            dropSchema(schemaName, srcCon);

            if(srcCon != null){ srcCon.close(); }
            if(targetCon != null){ targetCon.close(); }
        }
    }


    //HILFSFUNKTIONEN FÜR DIE TESTS! ////

    private static void prepareSrcTable(String schemaName, Connection con) throws SQLException
    {
        Statement s = con.createStatement();

        String rawCreate = "CREATE TABLE %s.DTYPES(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYDATE DATE, MYTIME TIME, " +
                "MYUUID UUID, MYGEOM GEOMETRY(LINESTRING,2056))";
        String create = String.format(rawCreate, schemaName);
        s.addBatch(create);

        String insert = String.format(
                "INSERT INTO %s.DTYPES VALUES(15, 9.99, 'Hello Db2Db', CURRENT_DATE, CURRENT_TIME, '%s', ST_GeomFromText('%s', 2056))",
                schemaName,
                UUID.randomUUID(),
                GEOM_WKT);
        s.addBatch(insert);

        String grant = String.format("GRANT SELECT ON ALL TABLES IN SCHEMA %s TO %s", schemaName, TestUtil.PG_READERUSR_USR);
        s.addBatch(grant);
        s.executeBatch();

        con.commit();
    }

    private static void assertEqualGeomInSourceAndSink(Connection con, String schemaName) throws SQLException {
        Statement check = con.createStatement();
        ResultSet rs = check.executeQuery(
                String.format("select ST_AsText(geom) as geom_text from %s.sink", schemaName));
        rs.next();
        String geomRes = rs.getString(1).trim().toUpperCase();

        Assert.assertEquals("The transferred geometry is not equal to the geometry in the source table", GEOM_WKT, geomRes);
    }

    private static void preparePgGeomSourceSinkTables(String schemaName, Connection con) throws SQLException {
        Statement prep = con.createStatement();
        prep.addBatch(String.format("CREATE TABLE %s.SOURCE (geom geometry(LINESTRING,2056) );",schemaName));
        prep.addBatch(String.format("CREATE TABLE %s.SINK (geom geometry(LINESTRING,2056) );",schemaName));
        prep.addBatch(String.format("INSERT INTO %s.SOURCE VALUES ( ST_GeomFromText('%s', 2056) )", schemaName, GEOM_WKT));

        prep.addBatch(String.format("GRANT SELECT ON ALL TABLES IN SCHEMA %s TO %s", schemaName, TestUtil.PG_READERUSR_USR));
        prep.addBatch(String.format("GRANT SELECT, INSERT, DELETE ON ALL TABLES IN SCHEMA %s TO %s", schemaName, TestUtil.PG_DMLUSR_USR));

        prep.executeBatch();
        con.commit();
    }

    private void dropSchema(String schemaName, Connection con) throws SQLException{
        if(con == null){ return; }

        Statement s = con.createStatement();
        s.execute(String.format("drop schema %s cascade", schemaName));
    }

    private static Connection connectToPreparedPgDb(String schemaName) throws Exception {
        Driver pgDriver = (Driver)Class.forName("org.postgresql.Driver").newInstance();
        DriverManager.registerDriver(pgDriver);

        Connection con = DriverManager.getConnection(
            TestUtil.PG_CONNECTION_URI,
            TestUtil.PG_DDLUSR_USR,
            TestUtil.PG_DDLUSR_PWD);

        con.setAutoCommit(false);

        Statement s = con.createStatement();
        s.addBatch(String.format("drop schema if exists %s cascade", schemaName));
        s.addBatch("create schema " + schemaName);
        s.addBatch(String.format("grant usage on schema %s to %s", schemaName, TestUtil.PG_DMLUSR_USR));
        s.addBatch(String.format("grant usage on schema %s to %s", schemaName, TestUtil.PG_READERUSR_USR));
        s.executeBatch();
        con.commit();

        return con;
    }

    private void clearTestDb(Connector sourceDb) throws Exception {
        Connection con = sourceDb.connect();
        con.setAutoCommit(true);
        try {
            Statement stmt = con.createStatement();
            try {
                stmt.execute("DROP TABLE colors");
            } catch (SQLException e) {};
            try {
                stmt.execute("DROP TABLE colors_copy");
            } catch (SQLException e) {};
        } finally {
            con.close();
        }

    }

    private void createTestDb(Connector sourceDb )
            throws Exception{
        Connection con = sourceDb.connect();
        createTableInTestDb(con);
        writeExampleDataInTestDB(con);

    }

    private void createTableInTestDb(Connection con) throws Exception {
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");
    }

    private void writeExampleDataInTestDB(Connection con) throws Exception{
        Statement stmt = con.createStatement();
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
    }

}