package ch.so.agi.gretl.jobs;

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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class GpkgImportTest {
    
    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));
    
    @Test
    public void importOk() throws Exception {
        String schemaName = "gpkgimport".toLowerCase();

        // Setup db schema
        try (
                Connection con = IntegrationTestUtilSql.connectPG(postgres);
                Statement stmt = con.createStatement()
        ) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            stmt.execute("CREATE TABLE "+schemaName+".importdata(fid integer, idname character varying, geom geometry(POINT,2056))");

            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgImport");
        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        IntegrationTestUtil.executeTestRunner(projectDirectory, variables);

        // Reconnect to check results
        try (Connection con = IntegrationTestUtilSql.connectPG(postgres);
             Statement stmt = con.createStatement()) {

            // Query row count
            try (ResultSet rowCount = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM " + schemaName + ".importdata;")) {
                assertTrue(rowCount.next(), "No row count result returned");
                assertEquals(1, rowCount.getInt("rowcount"), "Row count mismatch");
            }

            // Query actual data
            try (ResultSet rs = stmt.executeQuery("SELECT fid, idname, ST_AsEWKT(geom) FROM " + schemaName + ".importdata;")) {
                ResultSetMetaData rsmd = rs.getMetaData();
                assertEquals(3, rsmd.getColumnCount(), "Column count mismatch");

                assertTrue(rs.next(), "No result rows returned");
                assertEquals(1, rs.getInt("fid"), "FID mismatch");
                assertEquals("12", rs.getString("idname"), "ID name mismatch");
                assertEquals("SRID=2056;POINT(-0.228571428571429 0.568831168831169)", rs.getString("st_asewkt"), "Geometry mismatch");
            }
        }
    }
}
