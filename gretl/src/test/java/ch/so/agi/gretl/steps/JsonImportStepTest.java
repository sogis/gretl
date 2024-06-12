package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;

public class JsonImportStepTest {

    @ClassRule
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName(TestUtil.PG_DB_NAME)
            .withUsername(TestUtil.PG_DDLUSR_USR)
            .withPassword(TestUtil.PG_DDLUSR_PWD)
            .withInitScript(TestUtil.PG_INIT_SCRIPT_PATH)
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    private final String schemaName;
    private final String tableName;
    private final String columnName;
    private Connector connector;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public JsonImportStepTest() {
        this.schemaName = "jsonimport";
        this.tableName = "jsonobject";
        this.columnName = "json_text_col";
    }

    @Before
    public void before() throws Exception {
        this.connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);
    }

    @After
    public void after() throws Exception {
        if (!this.connector.isClosed()) {
            this.connector.close();
        }
    }

    @Test
    public void importJsonObject_Ok() throws Exception {
        File jsonFile = TestUtil.createTempFile(folder, "{\"foo\":\"bar\"}", "test1.json");

        try (Connection con = connector.connect(); Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            initializeSchema(con, stmt);

            JsonImportStep jsonImportStep = new JsonImportStep();
            jsonImportStep.execute(connector, jsonFile, schemaName+"."+tableName, columnName, true);

            ResultSet rs = stmt.executeQuery("SELECT "+columnName+"::jsonb -> 'foo' AS foo FROM " + schemaName + "." + tableName);
            if (rs.next()) {
                String val = rs.getString(1);
                assertEquals("\"bar\"", val);
            }

            assertFalse(rs.next());
        }
    }
    
    @Test
    public void appendJsonObject_Ok() throws Exception {
        File jsonFile = TestUtil.createTempFile(folder, "{\"foo\":\"bar\"}", "test2.json");

        try (Connection con = connector.connect(); Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            initializeSchema(con, stmt);

            JsonImportStep jsonImportStep = new JsonImportStep();
            jsonImportStep.execute(connector, jsonFile, schemaName+"."+tableName, columnName, false);

            ResultSet rs = stmt.executeQuery("SELECT "+columnName+"::jsonb -> 'foo' AS foo FROM " + schemaName + "." + tableName);
            rs.next();
            rs.next();

            assertFalse(rs.next());
        }
    }
    
    @Test
    public void importJsonArray_Ok() throws Exception {
        File jsonFile = TestUtil.createTempFile(folder, "[{\"type\":\"building\"}, {\"type\":\"street\"}]", "test3.json");
        
        try (Connection con = connector.connect(); Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            initializeSchema(con, stmt);

            JsonImportStep jsonImportStep = new JsonImportStep();
            jsonImportStep.execute(connector, jsonFile, schemaName+"."+tableName, columnName, true);

            ResultSet rs = stmt.executeQuery("SELECT "+columnName+"::jsonb -> 'type' AS atype FROM "+schemaName+"."+tableName+" ORDER BY "+columnName+"::jsonb -> 'type' DESC");

            rs.next();
            String val1 = rs.getString(1);
            assertEquals("\"street\"", val1);

            rs.next();
            String val2 = rs.getString(1);
            assertEquals("\"building\"", val2);

            assertFalse(rs.next());
        }
    }

    private void initializeSchema(Connection connection, Statement statement) throws Exception {
        statement.execute("DROP SCHEMA IF EXISTS " + this.schemaName + " CASCADE;");
        statement.execute("CREATE SCHEMA " + this.schemaName+";");
        statement.execute("CREATE TABLE " + this.schemaName + "."+this.tableName + " (id serial, " +this.columnName + " text);");
        connection.commit();
    }
}
