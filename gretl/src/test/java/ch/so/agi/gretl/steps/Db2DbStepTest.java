package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.TransferSet;
import ch.so.agi.gretl.testutil.DbTest;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.GretlException;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static org.gradle.internal.impldep.org.testng.AssertJUnit.assertEquals;
import static org.junit.Assert.fail;

public class Db2DbStepTest {

    private static final String GEOM_WKT = "LINESTRING(2600000 1200000,2600001 1200001)";

    @ClassRule
    public static PostgreSQLContainer<?> postgres =
            (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                    .withDatabaseName(TestUtil.PG_DB_NAME)
                    .withUsername(TestUtil.PG_DDLUSR_USR)
                    .withInitScript(TestUtil.PG_INIT_SCRIPT_PATH)
                    .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    private Connector connector;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        this.connector = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(this.connector);
    }

    @After
    public void after() throws Exception {
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        clearTestDb(sourceDb);
        if (!this.connector.isClosed()) {
            this.connector.close();
        }
    }

    @Test
    public void faultFreeExecutionTest() throws Exception {
        File sqlFile = TestUtil.createTempFile(folder, "SELECT * FROM colors; ", "query.sql");
        File sqlFile2 = TestUtil.createTempFile(folder, "SELECT * FROM colors; ", "query2.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(Arrays.asList(
                new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.TRUE),
                new TransferSet(sqlFile2.getAbsolutePath(), "colors_copy", Boolean.TRUE)
        ));
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        Db2DbStep db2db = new Db2DbStep();

        db2db.processAllTransferSets(sourceDb, targetDb, transferSets);

        try (Connection connection = this.connector.connect()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM colors_copy WHERE farbname = 'blau'");
            while (rs.next()) {
                assertEquals(rs.getObject("rot"), 0);
                assertEquals(rs.getObject("farbname"), "blau");
            }
        }
    }

    @Test
    public void newlineAtEndOfFileTest_Ok() throws Exception {
        File sqlFile = TestUtil.createTempFile(folder, "SELECT * FROM colors;" + System.lineSeparator(), "query.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(Collections.singletonList(
                new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", true)
        ));

        Db2DbStep db2db = new Db2DbStep();
        db2db.processAllTransferSets(this.connector, this.connector, transferSets);
     }

    @Test(expected = IOException.class)
    public void fileWithMultipleStmtTest_throwsIOException() throws Exception {
        String content = "SELECT * FROM colors;" + System.lineSeparator() + "SELECT * FROM colors;";
        File sqlFile = TestUtil.createTempFile(folder, content, "query.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(Collections.singletonList(
                new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", true)
        ));

        Db2DbStep db2db = new Db2DbStep();
        db2db.processAllTransferSets(this.connector, this.connector, transferSets);
        Assert.fail();
    }

    @Test(expected = EmptyFileException.class)
    public void db2dbEmptyFile_throwsEmptyFileException() throws Exception {
        File sqlFile = TestUtil.createTempFile(folder, "", "query.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(Collections.singletonList(
                new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.FALSE)
        ));

        Db2DbStep db2db = new Db2DbStep();
        db2db.processAllTransferSets(this.connector, this.connector, transferSets);
        Assert.fail("EmptyFileException müsste geworfen werden");
    }

    @Test(expected = SQLException.class)
    public void invalidSql_throwsSqlException() throws Exception {
        File sqlFile = TestUtil.createTempFile(folder, "SELECT somethingInvalid FROM colors", "query.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(Collections.singletonList(
                new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.FALSE)
        ));

        Db2DbStep db2db = new Db2DbStep();
        db2db.processAllTransferSets(this.connector, this.connector, transferSets);
        Assert.fail("EmptyFileException müsste geworfen werden");
    }

    @Test
    public void columnNumberTest() throws Exception {
        try (Connection connection = this.connector.connect(); Statement stmt = connection.createStatement()) {
            // Hier muss die Spalte farbname entfernt werden, da die Tabelle colors_copy
            // ja gerade mit nicht genug Spalten angelegt werden soll!
            stmt.execute("ALTER TABLE colors_copy DROP COLUMN farbname");

            File sqlFile = TestUtil.createTempFile(folder, "SELECT rot, gruen, blau, farbname FROM colors", "query.sql");
            ArrayList<TransferSet> transferSets = new ArrayList<>(Collections.singletonList(
                    new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.FALSE)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, transferSets);

            Assert.fail("Eine Exception müsste geworfen werden. ");
        } catch (GretlException ge) {
            boolean isMismatchException = GretlException.TYPE_COLUMN_MISMATCH.equals(ge.getType());
            if (!isMismatchException) {
                throw ge;
            }
        }
    }

    @Test(expected = SQLException.class)
    public void incompatibleDataType_throwsSqlException() throws Exception {
        try (Connection connection = this.connector.connect(); Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE colors_copy; CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname integer)");

            File sqlFile = TestUtil.createTempFile(folder, "SELECT * FROM colors", "query.sql");
            ArrayList<TransferSet> transferSets = new ArrayList<>(Collections.singletonList(
                    new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.FALSE)
            ));
            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, transferSets);

            Assert.fail("Eine Exception müsste geworfen werden. ");
        }
    }

    @Test
    public void copyEmptyTableToOtherTable_Ok() throws Exception {
        File sqlFile = TestUtil.createTempFile(folder, "SELECT * FROM colors", "query.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(Collections.singletonList(
                new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.FALSE)
        ));
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        Db2DbStep db2db = new Db2DbStep();

        db2db.processAllTransferSets(sourceDb, targetDb, transferSets);
    }

    @Test
    public void deleteTest_returnsCorrectCount() throws Exception {
        Connector connector = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TestUtil.execute(connector, TestUtil.getResourceFile(TestUtil.INSERT_COLORS_COPY_DATA_SQL_PATH));
        File sqlFile = TestUtil.createTempFile(folder, "SELECT * FROM colors;", "query.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(
                Collections.singletonList(
                        new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.TRUE)
                )
        );
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB", "bjsvwsch", null);
        Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB", "bjsvwsch", null);
        Db2DbStep db2db = new Db2DbStep();
        db2db.processAllTransferSets(sourceDb, targetDb, transferSets);

        // Select auf db um korrektes ausführen zu verifizieren
        try (Connection connection = connector.connect()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM colors_copy");
            rs.next();
            int i = rs.getInt(1);
            connection.commit();

            assertEquals(3, i);
        }
    }

    @Test
    public void closeConnectionsTest_connectionsClosed() throws Exception {
        File sqlFile = TestUtil.createTempFile(folder, "SELECT * FROM colors", "query.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(
                Collections.singletonList(
                        new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.TRUE)
                )
        );
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();
        db2db.processAllTransferSets(sourceDb, targetDb, transferSets);

        Assert.assertTrue("SourceConnection is not closed", sourceDb.isClosed());
        Assert.assertTrue("TargetConnection is not closed", targetDb.isClosed());
    }

    @Test
    public void closeConnectionsAfterFailedTest_connectionsClosed() throws Exception {
        File sqlFile = TestUtil.createTempFile(folder, "SELECT güggeliblau FROM colors_copy", "query.sql");
        ArrayList<TransferSet> transferSets = new ArrayList<>(
                Collections.singletonList(
                        new TransferSet(sqlFile.getAbsolutePath(), "colors_copy", Boolean.TRUE)
                )
        );
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        Db2DbStep db2db = new Db2DbStep();

        Assert.assertThrows(SQLException.class, () -> db2db.processAllTransferSets(sourceDb, targetDb, transferSets));
        boolean isclosed = sourceDb.isClosed();
        Assert.assertTrue("SourceConnection is not closed", sourceDb.isClosed());
        Assert.assertTrue("TargetConnection is not closed", targetDb.isClosed());
    }

    @Category(DbTest.class)
    @Test
    public void canWriteGeomFromWkbTest_Ok() throws Exception {
        String schemaName = "GeomFromWkbTest";

        try (Connection con = connectToPreparedPgDb(schemaName)) {
            preparePgGeomSourceSinkTables(schemaName, con);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createTempFile(
                    folder,
                    String.format("select ST_AsBinary(geom) as geom from %s.source", schemaName),
                    "select.sql"
            );

            Connector src = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
            Connector sink = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[] { "geom:wkb:2056" }
            );

            step.processAllTransferSets(src, sink, Collections.singletonList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);
            dropSchema(schemaName, con);
        }
    }

    @Category(DbTest.class)
    @Test
    public void canWriteGeomFromWktTest_Ok() throws Exception {
        String schemaName = "GeomFromWktTest";

        try (Connection con = connectToPreparedPgDb(schemaName)) {
            preparePgGeomSourceSinkTables(schemaName, con);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createTempFile(
                    folder,
                    String.format("select ST_AsText(geom) as geom from %s.source", schemaName),
                    "select.sql"
            );

            Connector src = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
            Connector sink = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[] { "geom:wkt:2056" }
            );

            step.processAllTransferSets(src, sink, Collections.singletonList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);
            dropSchema(schemaName, con);
        }
    }

    @Category(DbTest.class)
    @Test
    public void canWriteGeomFromGeoJsonTest_Ok() throws Exception {
        String schemaName = "GeomFromGeoJsonTest";

        try (Connection con = connectToPreparedPgDb(schemaName)) {
            preparePgGeomSourceSinkTables(schemaName, con);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createTempFile(folder,
                    String.format("select ST_AsGeoJSON(geom) as geom from %s.source", schemaName),
                    "select.sql"
            );

            Connector src = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
            Connector sink = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[] { "geom:geojson:2056" }
            );

            step.processAllTransferSets(src, sink, Collections.singletonList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);
            dropSchema(schemaName, con);
        }
    }

    /**
     * Test's loading several hundred thousand rows from sqlite to postgis. Loading
     * 300'000 rows should take about 15 seconds
     */
    @Category(DbTest.class)
    @Test
    public void positiveBulkLoadPostgisTest_Ok() throws Exception {
        int numRows = 300000;
        String schemaName = "BULKLOAD2POSTGIS";
        File sqliteDb = createTmpDb(schemaName);

        try (
                Connection srcCon = connectSqlite(sqliteDb);
                Connection targetCon = connectToPreparedPgDb(schemaName);
                Statement checkStmt = targetCon.createStatement()
        ) {
            createSqliteSrcTable(numRows, srcCon);
            prepareSinkTable(schemaName, targetCon);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createTempFile(
                    folder,
                    "select myint, myfloat, mytext, mywkt as mygeom from dtypes",
                    "select.sql"
            );

            Connector src = new Connector("jdbc:sqlite:" + sqliteDb.getAbsolutePath());
            Connector sink = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".DTYPES",
                    true,
                    new String[] { "mygeom:wkt:2056" }
            );

            step.processAllTransferSets(src, sink, Collections.singletonList(tSet));

            // Test considered OK if all values are transferred and Geom OK
            String checkSQL = String.format("SELECT COUNT(*) FROM %s.DTYPES", schemaName);
            ResultSet rs = checkStmt.executeQuery(checkSQL);
            rs.next();

            Assert.assertEquals("Check Statement must return exactly " + numRows, numRows, rs.getInt(1));
            dropSchema(schemaName, targetCon);
        }
    }

    /**
     * Tests if the sqlite datatypes and geometry as wkt are transferred faultfree
     * from sqlite to postgis
     */
    @Category(DbTest.class)
    @Test
    public void positiveSqlite2PostgisTest_Ok() throws Exception {
        String schemaName = "SQLITE2POSTGIS";
        File sqliteDb = createTmpDb(schemaName);

        try (
                Connection srcCon = connectSqlite(sqliteDb);
                Connection targetCon = connectToPreparedPgDb(schemaName);
                Statement checkStmt = targetCon.createStatement()
        ) {
            createSqliteSrcTable(1, srcCon);
            prepareSinkTable(schemaName, targetCon);

            Db2DbStep step = new Db2DbStep();
            File queryFile = TestUtil.createTempFile(
                    folder,
                    "select myint, myfloat, mytext, mywkt as mygeom from dtypes",
                    "select.sql"
            );

            Connector src = new Connector("jdbc:sqlite:" + sqliteDb.getAbsolutePath());
            Connector sink = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DMLUSR_USR, TestUtil.PG_DMLUSR_PWD);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".DTYPES",
                    true,
                    new String[] { "mygeom:wkt:2056" }
            );

            step.processAllTransferSets(src, sink, Collections.singletonList(tSet));

            // Test considered OK if all values are transferred and Geom OK
            String checkSQLRaw = "SELECT COUNT(*) FROM %s.DTYPES "
                    + "WHERE MYINT IS NOT NULL AND MYFLOAT IS NOT NULL AND MYTEXT IS NOT NULL AND "
                    + "ST_Equals(MYGEOM, ST_GeomFromText('%s', 2056)) = True";
            String checkSql = String.format(checkSQLRaw, schemaName, GEOM_WKT);
            ResultSet rs = checkStmt.executeQuery(checkSql);
            rs.next();

            Assert.assertEquals("Check Statement must return exactly one row", 1, rs.getInt(1));
        }
    }

    /**
     * Tests if the "special" datatypes (Date, Time, GUID, Geometry, ..) are
     * transferred faultfree from Postgis to sqlite
     */
    @Category(DbTest.class)
    @Test
    public void positivePostgis2SqliteTest_Ok() throws Exception {
        String schemaName = "POSTGIS2SQLITE";
        File sqliteDb = createTmpDb(schemaName);

        try (
                Connection srcCon = connectToPreparedPgDb(schemaName);
                Connection targetCon = connectSqlite(sqliteDb)
        ) {
            prepareSrcTable(schemaName, srcCon);
            createSqliteTargetTable(targetCon);

            Db2DbStep step = new Db2DbStep();
            String select = String.format(
                    "select myint, myfloat, mytext, mydate, mytime, myuuid, ST_AsText(mygeom) as mygeom_wkt from %s.dtypes",
                    schemaName);
            File queryFile = TestUtil.createTempFile(folder, select, "select.sql");

            Connector src = new Connector(postgres.getJdbcUrl(), TestUtil.PG_READERUSR_USR, TestUtil.PG_READERUSR_PWD);
            Connector sink = new Connector("jdbc:sqlite:" + sqliteDb.getAbsolutePath());
            TransferSet tSet = new TransferSet(queryFile.getAbsolutePath(), "dtypes", true);

            step.processAllTransferSets(src, sink, Collections.singletonList(tSet));

            // Test considered OK if all values are transferred and Geom OK
            String checkSQL = String.format("SELECT COUNT(*) FROM DTYPES WHERE "
                    + "MYINT IS NOT NULL AND MYFLOAT IS NOT NULL AND MYTEXT IS NOT NULL AND MYDATE IS NOT NULL AND MYTIME IS NOT NULL AND MYUUID IS NOT NULL AND "
                    + "MYGEOM_WKT = '%s'", GEOM_WKT);

            Statement checkStmt = targetCon.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkSQL);
            rs.next();

            Assert.assertEquals("Check Statement must return exactly one row", 1, rs.getInt(1));
            dropSchema(schemaName, srcCon);
        }
    }

    // HILFSFUNKTIONEN FÜR DIE TESTS! ////
    private static void prepareSinkTable(String schemaName, Connection con) throws SQLException {
        String create = String.format(
                "CREATE TABLE %s.DTYPES(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYGEOM GEOMETRY(LINESTRING,2056))",
                schemaName);
        String grant = String.format(
                "GRANT SELECT, INSERT, DELETE ON ALL TABLES IN SCHEMA %s TO %s",
                schemaName,
                TestUtil.PG_DMLUSR_USR
        );

        try (Statement statement = con.createStatement()) {
            statement.addBatch(create);
            statement.addBatch(grant);
            statement.executeBatch();
            con.commit();
        }
    }

    private static void createSqliteSrcTable(int numRows, Connection con) throws SQLException {
        String createQuery = "CREATE TABLE DTYPES(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYWKT TEXT)";
        String insertQuery = "INSERT INTO DTYPES VALUES(?, ?, ?, ?)";
        Random random = new Random();

        try (Statement statement = con.createStatement()) {
            statement.execute(createQuery);
        }

        try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
            for (int i = 0; i < numRows; i++) {
                ps.setInt(1, random.nextInt());
                ps.setDouble(2, random.nextDouble());
                ps.setString(3, UUID.randomUUID().toString());
                ps.setString(4, GEOM_WKT);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        con.commit();
    }

    private static void createSqliteTargetTable(Connection con) throws SQLException {
        String createQuery = "CREATE TABLE DTYPES(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYDATE TEXT, MYTIME TEXT, "
                + "MYUUID TEXT, MYGEOM_WKT TEXT)";

        try (Statement statement = con.createStatement()) {
            statement.execute(createQuery);
            con.commit();
        }
    }

    private static File createTmpDb(String name) throws IOException {
        Path dir = Files.createTempDirectory(null);
        Path file = Paths.get(name + ".sqlite");
        Path dbPath = dir.resolve(file);
        return dbPath.toFile();
    }

    private static Connection connectSqlite(File dbLocation) throws SQLException {
        String url = "jdbc:sqlite:" + dbLocation.getAbsolutePath();
        Connection con = DriverManager.getConnection(url);
        con.setAutoCommit(false);
        return con;
    }

    private static void prepareSrcTable(String schemaName, Connection con) throws SQLException {
        String createQuery = String.format(
                "CREATE TABLE %s.DTYPES(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYDATE DATE, MYTIME TIME, "
                + "MYUUID UUID, MYGEOM GEOMETRY(LINESTRING,2056))",
                schemaName
        );
        String insertQuery = String.format(
                "INSERT INTO %s.DTYPES VALUES(15, 9.99, 'Hello Db2Db', CURRENT_DATE, CURRENT_TIME, '%s', ST_GeomFromText('%s', 2056))",
                schemaName,
                UUID.randomUUID(),
                GEOM_WKT
        );
        String grantQuery = String.format("GRANT SELECT ON ALL TABLES IN SCHEMA %s TO %s", schemaName, TestUtil.PG_READERUSR_USR);

        try (Statement statement = con.createStatement()) {
            statement.addBatch(createQuery);
            statement.addBatch(insertQuery);
            statement.addBatch(grantQuery);
            statement.executeBatch();
            con.commit();
        }
    }

    private static void assertEqualGeomInSourceAndSink(Connection con, String schemaName) throws SQLException {
        String selectQuery = String.format("select ST_AsText(geom) as geom_text from %s.sink", schemaName);
        String expectedMessage = "The transferred geometry is not equal to the geometry in the source table";

        try (Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(selectQuery);
            rs.next();

            String geomRes = rs.getString(1).trim().toUpperCase();
            Assert.assertEquals(expectedMessage, GEOM_WKT, geomRes);
        }
    }

    private static void preparePgGeomSourceSinkTables(String schemaName, Connection con) throws SQLException {
        try (Statement statement = con.createStatement()) {
            statement.addBatch(String.format("CREATE TABLE %s.SOURCE (geom geometry(LINESTRING,2056) );", schemaName));
            statement.addBatch(String.format("CREATE TABLE %s.SINK (geom geometry(LINESTRING,2056) );", schemaName));
            statement.addBatch(String.format("INSERT INTO %s.SOURCE VALUES ( ST_GeomFromText('%s', 2056) )", schemaName, GEOM_WKT));
            statement.addBatch(String.format("GRANT SELECT ON ALL TABLES IN SCHEMA %s TO %s", schemaName, TestUtil.PG_READERUSR_USR));
            statement.addBatch(String.format("GRANT SELECT, INSERT, DELETE ON ALL TABLES IN SCHEMA %s TO %s", schemaName, TestUtil.PG_DMLUSR_USR));

            statement.executeBatch();
            con.commit();
        }
    }

    private void dropSchema(String schemaName, Connection con) throws SQLException {
        if (con == null) {
            return;
        }

        try (Statement statement = con.createStatement()) {
            statement.execute(String.format("drop schema %s cascade", schemaName));
        }
    }

    private static Connection connectToPreparedPgDb(String schemaName) throws Exception {
        String url = postgres.getJdbcUrl();
        String user = postgres.getUsername();
        String password = postgres.getPassword();

        Connection con = DriverManager.getConnection(url, user, password);
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

    private void clearTestDb(Connector connector) throws Exception {
        try (Connection connection = connector.connect(); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);

            try {
                statement.execute("DROP TABLE colors");
            } catch (SQLException ignored) {}

            try {
                statement.execute("DROP TABLE colors_copy");
            } catch (SQLException ignored) {}
        }
    }

    private void createTestDb(Connector sourceDb) throws Exception {
        Connection con = sourceDb.connect();
        createTableInTestDb(con);
        writeExampleDataInTestDB(con);
    }

    private void createTableInTestDb(Connection connection) throws Exception {
        connection.setAutoCommit(true);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE colors (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");
            statement.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");
        }
    }

    private void writeExampleDataInTestDB(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
            statement.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
            statement.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
        }
    }
}