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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class JsonImportTest {
    
    private static final String dbusr = "ddluser";
    private static final String dbpwd = "ddluser";
    private static final String dbdatabase = "gretl";

    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName(dbdatabase)
            .withUsername(dbusr)
            .withPassword(dbpwd)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void importJsonObject_Ok() throws Exception {
        String schemaName = "jsonimport";
        String tableName = "jsonobject";
        String columnName = "json_text_col";

        Connection con = null;
        try {
            con = IntegrationTestUtilSql.connectPG(postgres);
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            Statement s1 = con.createStatement();            
            s1.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            s1.close();
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            
            con.commit();
            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/JsonImportObject", gvs);

            //reconnect to check results
            con = IntegrationTestUtilSql.connectPG(postgres);

            Statement s2 = con.createStatement();
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
            
            Statement s3 = con.createStatement();
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
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }
    
    @Test
    public void importJsonArray_Ok() throws Exception {
        String schemaName = "jsonimport";
        String tableName = "jsonarray";
        String columnName = "json_text_col";

        Connection con = null;
        try {
            con = IntegrationTestUtilSql.connectPG(postgres);
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            Statement s1 = con.createStatement();            
            s1.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            s1.close();
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            
            con.commit();
            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/JsonImportArray", gvs);

//            Thread.sleep(1000*60*20);

            //reconnect to check results
            con = IntegrationTestUtilSql.connectPG(postgres);

            Statement s2 = con.createStatement();
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
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }
}
