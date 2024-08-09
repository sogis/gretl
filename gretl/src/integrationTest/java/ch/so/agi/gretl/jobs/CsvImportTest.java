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

public class CsvImportTest {
    private final GradleVariable[] gradleVariables = { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()) };

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
        String schemaName = "csvimport".toLowerCase();

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        Statement s1 = connection.createStatement();
        s1.execute("CREATE TABLE "+schemaName+".importdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");
        s1.close();
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvImport");
        IntegrationTestUtil.executeTestRunner(projectDirectory, "csvimport", gradleVariables).build();

        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);

        Statement s2 = connection.createStatement();
        ResultSet rs=s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum,adate, atimestamp, aboolean, aextra FROM "+schemaName+".importdata WHERE t_id=1");
        if(!rs.next()) {
            fail();
        }
        assertEquals(2,rs.getInt(1));
        assertEquals(new BigDecimal("3.1"),rs.getBigDecimal(2));
        assertEquals("abc",rs.getString(3));
        assertEquals("rot",rs.getString(4));
        assertEquals(new java.sql.Date(2017-1900,9-1,21),rs.getDate(5));
        assertEquals(new java.sql.Timestamp(2016-1900,8-1,22,13,15,22,450000000),rs.getTimestamp(6));
        assertTrue(rs.getBoolean(7));
        if(rs.next()) {
            fail();
        }
        rs.close();
        s2.close();
    }
    
    @Test
    public void importOk_batchSize() throws Exception {
        String schemaName = "csvimport".toLowerCase();
        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        Statement s1 = connection.createStatement();
        s1.execute("CREATE TABLE "+schemaName+".importdata_batchsize(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");
        s1.close();
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvImportBatchSize");
        IntegrationTestUtil.executeTestRunner(projectDirectory, "csvimport", gradleVariables).build();

        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);

        Statement s2 = connection.createStatement();
        ResultSet rs=s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum,adate, atimestamp, aboolean, aextra FROM "+schemaName+".importdata_batchsize WHERE t_id=1");
        if(!rs.next()) {
            fail();
        }
        assertEquals(2,rs.getInt(1));
        assertEquals(new BigDecimal("3.1"),rs.getBigDecimal(2));
        assertEquals("abc",rs.getString(3));
        assertEquals("rot",rs.getString(4));
        assertEquals(new java.sql.Date(2017-1900,9-1,21),rs.getDate(5));
        assertEquals(new java.sql.Timestamp(2016-1900,8-1,22,13,15,22,450000000),rs.getTimestamp(6));
        assertTrue(rs.getBoolean(7));
        if(rs.next()) {
            fail();
        }
        rs.close();
        s1.close();
    }
}
