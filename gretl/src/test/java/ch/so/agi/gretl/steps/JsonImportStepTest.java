package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
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
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(TestUtil.PG_DDLUSR_USR)
        .withPassword(TestUtil.PG_DDLUSR_PWD)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    public JsonImportStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;

    @Test
    public void importJsonObject_Ok() throws Exception {
        Connector connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);
        File jsonFile = TestUtil.createFile(folder, "{\"foo\":\"bar\"}", "test1.json");

        String schemaName = "jsonimport";
        String tableName = "jsonobject";
        String columnName = "json_text_col";
        
        Connection con = connector.connect();
        con.setAutoCommit(false);
        try {
            Statement stmt = con.createStatement();

            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            con.commit();
            
            JsonImportStep jsonImportStep = new JsonImportStep();
            jsonImportStep.execute(connector, jsonFile, schemaName+"."+tableName, columnName, true);

            ResultSet rs = stmt.executeQuery("SELECT "+columnName+"::jsonb -> 'foo' AS foo FROM " + schemaName + "." + tableName);
            if (rs.next()) {
                String val = rs.getString(1);
                assertEquals("\"bar\"", val);
            }
            
            assertEquals(false, rs.next());
        } finally {
            con.close();
        }
    }
    
    @Test
    public void appendJsonObject_Ok() throws Exception {
        Connector connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);
        File jsonFile = TestUtil.createFile(folder, "{\"foo\":\"bar\"}", "test2.json");
        
        String schemaName = "jsonimport";
        String tableName = "jsonobject";
        String columnName = "json_text_col";

        Connection con = connector.connect();
        con.setAutoCommit(false);
        try {
            Statement stmt = con.createStatement();

            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES ('{\"yin\": \"yang\"}');");
            con.commit();
            
            JsonImportStep jsonImportStep = new JsonImportStep();
            jsonImportStep.execute(connector, jsonFile, schemaName+"."+tableName, columnName, false);

            ResultSet rs = stmt.executeQuery("SELECT "+columnName+"::jsonb -> 'foo' AS foo FROM " + schemaName + "." + tableName);
            rs.next();
            rs.next();
            assertEquals(false, rs.next());
        } finally {
            con.close();
        }
    }
    
    @Test
    public void importJsonArray_Ok() throws Exception {
        Connector connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);
        File jsonFile = TestUtil.createFile(folder, "[{\"type\":\"building\"}, {\"type\":\"street\"}]", "test3.json");

        String schemaName = "jsonimport";
        String tableName = "jsonarray";
        String columnName = "json_text_col";
        
        Connection con = connector.connect();
        con.setAutoCommit(false);
        try {
            Statement stmt = con.createStatement();

            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            con.commit();
            
            JsonImportStep jsonImportStep = new JsonImportStep();
            jsonImportStep.execute(connector, jsonFile, schemaName+"."+tableName, columnName, true);

            ResultSet rs = stmt.executeQuery("SELECT "+columnName+"::jsonb -> 'type' AS atype FROM "+schemaName+"."+tableName+" ORDER BY "+columnName+"::jsonb -> 'type' DESC");
            
            rs.next();
            String val1 = rs.getString(1);
            assertEquals("\"street\"", val1);
            
            rs.next();
            String val2 = rs.getString(1);
            assertEquals("\"building\"", val2);

            assertEquals(false, rs.next());
        } finally {
            con.close();
        }
    }
}
