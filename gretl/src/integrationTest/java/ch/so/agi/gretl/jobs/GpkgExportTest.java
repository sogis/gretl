package ch.so.agi.gretl.jobs;

import ch.ehi.ili2gpkg.Gpkg2iox;
import ch.interlis.iom.IomObject;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.ClassRule;

public class GpkgExportTest {
    private final GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
    private Connection connection = null;

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Before
    public void setup() {
        connection = IntegrationTestUtilSql.connectPG(postgres);
    }

    @After
    public void tearDown() {
        IntegrationTestUtilSql.closeCon(connection);
    }

    @Test
    public void exportTableOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgExport");

        Files.deleteIfExists(Paths.get(projectDirectory+ "/data.gpkg"));

        String schemaName = "gpkgexport".toLowerCase();

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        Statement s1 = connection.createStatement();
        s1.execute("CREATE TABLE "+schemaName+".exportdata(attr character varying,the_geom geometry(POINT,2056));");
        s1.execute("INSERT INTO "+schemaName+".exportdata(attr,the_geom) VALUES ('coord2d','0101000020080800001CD4411DD441CDBF0E69626CDD33E23F')");
        s1.close();
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        IntegrationTestUtil.executeTestRunner(projectDirectory, "gpkgexport", gradleVariables);

        //check results
        {
            System.out.println("cwd "+new File(".").getAbsolutePath());
            Statement stmt = null;
            ResultSet rs = null;
            Connection gpkgConnection = null;
            try {
                Gpkg2iox gpkg2iox = new Gpkg2iox();
                gpkgConnection = DriverManager.getConnection("jdbc:sqlite:" + new File("src/integrationTest/jobs/GpkgExport/data.gpkg").getAbsolutePath());
                stmt = gpkgConnection.createStatement();
                rs = stmt.executeQuery("SELECT attr, the_geom FROM exportdata");

                while (rs.next()) {
                    assertEquals("coord2d", rs.getString(1));
                    IomObject iomGeom = gpkg2iox.read(rs.getBytes(2));
                    assertEquals("COORD {C1 -0.22857142857142854, C2 0.5688311688311687}", iomGeom.toString());
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
        }
    }
    
    @Test
    public void exportTablesOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgExportTables");
        Files.deleteIfExists(Paths.get(projectDirectory + "/data.gpkg"));

        String schemaName = "gpkgexport".toLowerCase();

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        Statement s1 = connection.createStatement();
        s1.execute("CREATE TABLE "+schemaName+".exportdata1(attr character varying,the_geom geometry(POINT,2056));");
        s1.execute("INSERT INTO "+schemaName+".exportdata1(attr,the_geom) VALUES ('coord2d','0101000020080800001CD4411DD441CDBF0E69626CDD33E23F')");
        s1.close();

        Statement s2 = connection.createStatement();
        s2.execute("CREATE TABLE "+schemaName+".exportdata2(attr character varying,the_geom geometry(POINT,2056));");
        s2.execute("INSERT INTO "+schemaName+".exportdata2(attr,the_geom) VALUES ('coord2d','0101000020080800001CD4411DD441CDBF0E69626CDD33E23F')");
        s2.close();


        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        IntegrationTestUtil.executeTestRunner(projectDirectory, "gpkgexport", gradleVariables);

        //check results
        for (int i=1; i<=2; i++) {
            System.out.println("cwd "+new File(".").getAbsolutePath());
            Statement stmt = null;
            ResultSet rs = null;
            Connection gpkgConnection = null;
            try {
                Gpkg2iox gpkg2iox = new Gpkg2iox();
                gpkgConnection = DriverManager.getConnection("jdbc:sqlite:" + new File("src/integrationTest/jobs/GpkgExportTables/data.gpkg").getAbsolutePath());
                stmt = gpkgConnection.createStatement();
                rs = stmt.executeQuery("SELECT attr, the_geom FROM exportdata" + i);

                while (rs.next()) {
                    assertEquals("coord2d", rs.getString(1));
                    IomObject iomGeom = gpkg2iox.read(rs.getBytes(2));
                    assertEquals("COORD {C1 -0.22857142857142854, C2 0.5688311688311687}", iomGeom.toString());
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }

        }
    }
}
