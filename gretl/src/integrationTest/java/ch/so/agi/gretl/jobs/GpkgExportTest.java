package ch.so.agi.gretl.jobs;

import ch.ehi.ili2gpkg.Gpkg2iox;
import ch.interlis.iom.IomObject;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class GpkgExportTest {

    private final GretlLogger log;
    private final GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
    public GpkgExportTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void exportTableOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgExport");
        Path gpkgFilePath = Paths.get(projectDirectory + "/data.gpkg");
        String schemaName = "gpkgexport".toLowerCase();
        Files.deleteIfExists(gpkgFilePath);

        // Setup database
        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            stmt.execute(String.format("CREATE TABLE %s.exportdata(attr VARCHAR, the_geom GEOMETRY(POINT,2056))", schemaName));
            stmt.execute(String.format("INSERT INTO %s.exportdata(attr, the_geom) VALUES ('coord2d', '0101000020080800001CD4411DD441CDBF0E69626CDD33E23F')", schemaName));
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check results
        try (
                Connection gpkgConnection = DriverManager.getConnection("jdbc:sqlite:" + gpkgFilePath.toAbsolutePath());
                Statement stmt = gpkgConnection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT attr, the_geom FROM exportdata")
        ) {
            assertTrue(rs.next(), "No results returned from query");
            assertEquals("coord2d", rs.getString("attr"), "Attribute value mismatch");
            Gpkg2iox gpkg2iox = new Gpkg2iox();
            IomObject iomGeom = gpkg2iox.read(rs.getBytes("the_geom"));
            assertEquals("COORD {C1 -0.22857142857142854, C2 0.5688311688311687}", iomGeom.toString(), "Geometry mismatch");
            assertFalse(rs.next(), "More than one row returned from query");
        }
    }
    
    @Test
    public void exportTablesOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgExportTables");
        Files.deleteIfExists(Paths.get( projectDirectory + "/data.gpkg"));

        String schemaName = "gpkgexport".toLowerCase();

        try (
                Connection con = IntegrationTestUtilSql.connectPG(postgres);
                Statement stmt1 = con.createStatement();
                Statement stmt2 = con.createStatement();
        ) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            stmt1.execute("CREATE TABLE "+schemaName+".exportdata1(attr character varying,the_geom geometry(POINT,2056));");
            stmt1.execute("INSERT INTO "+schemaName+".exportdata1(attr,the_geom) VALUES ('coord2d','0101000020080800001CD4411DD441CDBF0E69626CDD33E23F')");
            stmt2.execute("CREATE TABLE "+schemaName+".exportdata2(attr character varying,the_geom geometry(POINT,2056));");
            stmt2.execute("INSERT INTO "+schemaName+".exportdata2(attr,the_geom) VALUES ('coord2d','0101000020080800001CD4411DD441CDBF0E69626CDD33E23F')");

            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
            IntegrationTestUtilSql.closeCon(con);
        }

        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check results
        for (int i = 1; i <= 2; i++) {
            log.info("cwd " + new File(".").getAbsolutePath());

            try (
                Connection gpkgConnection = DriverManager.getConnection("jdbc:sqlite:" + new File("src/integrationTest/jobs/GpkgExportTables/data.gpkg").getAbsolutePath());
                Statement stmt = gpkgConnection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT attr, the_geom FROM exportdata" + String.valueOf(i));
            ) {
                Gpkg2iox gpkg2iox = new Gpkg2iox();
                while (rs.next()) {
                    assertEquals("coord2d", rs.getString(1));
                    IomObject iomGeom = gpkg2iox.read(rs.getBytes(2));
                    assertEquals("COORD {C1 -0.22857142857142854, C2 0.5688311688311687}", iomGeom.toString());
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                fail();
            }
        }
    }
}
