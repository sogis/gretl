package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import org.junit.*;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ShpImportTest {
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
    public void importOk() throws Exception {
        String schemaName = "shpimport".toLowerCase();

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        Statement s1 = connection.createStatement();
        s1.execute("CREATE TABLE "+schemaName+".importdata_batchsize(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, geometrie geometry(POINT,2056), aextra varchar(40))");
        s1.close();
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/ShpImportBatchSize");

        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        IntegrationTestUtil.executeTestRunner(projectDirectory, "shpimport", variables);

        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);

        Statement s2 = connection.createStatement();
        ResultSet rs=s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum,adate, ST_X(geometrie), ST_Y(geometrie), aextra FROM "+schemaName+".importdata_batchsize WHERE t_id=1");
        if(!rs.next()) {
            fail();
        }
        assertEquals(2,rs.getInt(1));
        assertEquals(new BigDecimal("3.4"),rs.getBigDecimal(2));
        assertEquals("abc",rs.getString(3));
        assertEquals("rot",rs.getString(4));
        assertEquals(new java.sql.Date(2013-1900,10-1,21),rs.getDate(5));
        assertEquals(2638000.0,rs.getFloat(6),0.000001);
        assertEquals(1175250.0,rs.getFloat(7),0.000001);
        if(rs.next()) {
            fail();
        }
        rs.close();
        s1.close();
    }
}
