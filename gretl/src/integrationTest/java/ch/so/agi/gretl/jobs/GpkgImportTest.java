package ch.so.agi.gretl.jobs;

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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.junit.ClassRule;

public class GpkgImportTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    private Connection connection = null;

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Before
    public void setup() {
        connection = IntegrationTestUtilSql.connectPG(postgres);
    }

    @After
    public void tearDown() {
        IntegrationTestUtilSql.closeCon(connection);
    }
    
    @Test
    public void importOk() throws Exception {
        String schemaName = "gpkgimport".toLowerCase();

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        Statement s1 = connection.createStatement();
        s1.execute("CREATE TABLE "+schemaName+".importdata(fid integer, idname character varying, geom geometry(POINT,2056))");
        s1.close();
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/GpkgImport");
        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        IntegrationTestUtil.getGradleRunner(projectDirectory, "gpkgimport", variables).build();

        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);

        Statement s2 = connection.createStatement();
        ResultSet rowCount = s2.executeQuery("SELECT COUNT(*) AS rowcount FROM "+schemaName+".importdata;");
        while(rowCount.next()) {
            assertEquals(1, rowCount.getInt(1));
        }
        ResultSet rs = s2.executeQuery("SELECT fid,idname,st_asewkt(geom) FROM "+schemaName+".importdata;");
        ResultSetMetaData rsmd=rs.getMetaData();
        assertEquals(3, rsmd.getColumnCount());
        while(rs.next()){
            assertEquals(1, rs.getObject(1));
            assertEquals("12", rs.getObject(2));
            assertEquals("SRID=2056;POINT(-0.228571428571429 0.568831168831169)", rs.getObject(3));
        }
        rs.close();
        s1.close();
    }
}
