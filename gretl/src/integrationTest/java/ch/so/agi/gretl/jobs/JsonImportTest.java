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
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class JsonImportTest {
    private final GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
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
        // Prepare
        String schemaName = "jsonimport";
        String tableName = "jsonobject";
        String columnName = "json_text_col";

        try (
            Connection con = IntegrationTestUtilSql.connectPG(postgres);
            Statement stmt = con.createStatement()
        ) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            stmt.execute("CREATE TABLE " + schemaName + "." + tableName + " (id serial, " + columnName + " text);");
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/JsonImportObject");
        
        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check results
        try (
                Connection con = IntegrationTestUtilSql.connectPG(postgres);
                Statement stmt = con.createStatement()
        ) {
            {
                String sql = "SELECT CAST(" + columnName + "::jsonb->'features'->>1 AS jsonb)->'id' AS id FROM " + schemaName + "." + tableName;
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (!rs.next()) {
                        fail();
                    }
                    assertEquals(821114211, rs.getInt(1));
                    if (rs.next()) {
                        fail();
                    }
                }
            }
            {
                String sql = "SELECT ROUND(ST_XMin(ST_SetSRID(ST_GeomFromGeoJSON(CAST(" + columnName + "::jsonb->'features'->>1 AS jsonb)->'geometry'), 2056))) AS foo FROM " + schemaName + "." + tableName;
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (!rs.next()) {
                        fail();
                    }
                    assertEquals(2626724, rs.getInt(1));
                    if (rs.next()) {
                        fail();
                    }
                }
            }
        }
    }
    
    @Test
    public void importJsonArray_Ok() throws Exception {
        // Prepare
        String schemaName = "jsonimport";
        String tableName = "jsonarray";
        String columnName = "json_text_col";

        try (
            Connection con = IntegrationTestUtilSql.connectPG(postgres);
            Statement stmt = con.createStatement()
        ) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);

            stmt.execute("CREATE TABLE " + schemaName + "." + tableName + " (id serial, " + columnName + " text);");
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/JsonImportArray");
       
        // Check result
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        // Check results
        try (
            Connection con = IntegrationTestUtilSql.connectPG(postgres);
            Statement stmt = con.createStatement()
        ) {
            String sql = "SELECT " + columnName + "::jsonb -> 'surname' AS surname FROM " + schemaName + "." + tableName;

            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (!rs.next()) {
                    fail();
                }
                assertEquals("\"Doe\"", rs.getString(1));
                if (!rs.next()) {
                    fail();
                }
                assertEquals("\"Doe\"", rs.getString(1));
                if (rs.next()) {
                    fail();
                }
            }
        }
    }
}
