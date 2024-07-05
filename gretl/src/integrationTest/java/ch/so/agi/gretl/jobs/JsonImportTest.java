package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.*;
import ch.so.agi.gretl.testutil.TestUtil;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

public class JsonImportTest {
    private final GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
    private static String dbusr = "ddluser";
    private static String dbpwd = "ddluser";
    private static String dbdatabase = "gretl";

    private Connection connection = null;

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName(dbdatabase)
        .withUsername(dbusr)
        .withPassword(dbpwd)
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
    public void importJsonObject_Ok() throws Exception {
        String schemaName = "jsonimport";
        String tableName = "jsonobject";
        String columnName = "json_text_col";

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        Statement s1 = connection.createStatement();
        s1.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
        s1.close();
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/JsonImportObject");
        IntegrationTestUtil.getGradleRunner(projectDirectory, "jsonimport", gradleVariables).build();

        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);

        Statement s2 = connection.createStatement();
        String sql = "SELECT CAST("+columnName+"::jsonb->'features'->>1 AS jsonb)->'id' AS id FROM "+schemaName+"."+tableName;
        ResultSet rs = s2.executeQuery(sql);
        if(!rs.next()) {
            fail();
        }
        assertEquals(821114211, rs.getInt(1));
        if(rs.next()) {
            fail();
        }
        rs.close();
        s2.close();

        Statement s3 = connection.createStatement();
        sql = "SELECT ROUND(ST_XMin(ST_SetSRID(ST_GeomFromGeoJSON(CAST("+columnName+"::jsonb->'features'->>1 AS jsonb)->'geometry'), 2056))) AS foo FROM "+schemaName+"."+tableName;
        ResultSet rs3 = s3.executeQuery(sql);
        if(!rs3.next()) {
            fail();
        }
        assertEquals(2626724, rs3.getInt(1));
        if(rs3.next()) {
            fail();
        }
        rs3.close();
        s3.close();
    }
    
    @Test
    public void importJsonArray_Ok() throws Exception {
        String schemaName = "jsonimport";
        String tableName = "jsonarray";
        String columnName = "json_text_col";

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        Statement s1 = connection.createStatement();
        s1.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
        s1.close();
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/JsonImportArray");
        IntegrationTestUtil.getGradleRunner(projectDirectory, "jsonimport", gradleVariables).build();

        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);

        Statement s2 = connection.createStatement();
        String sql = "SELECT "+columnName+"::jsonb -> 'surname' AS surname FROM "+schemaName+"."+tableName;
        ResultSet rs = s2.executeQuery(sql);
        if(!rs.next()) {
            fail();
        }
        assertEquals("\"Doe\"", rs.getString(1));
        if(!rs.next()) {
            fail();
        }
        assertEquals("\"Doe\"", rs.getString(1));
        if(rs.next()) {
            fail();
        }
        rs.close();
        s2.close();
    }

}
