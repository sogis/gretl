package ch.so.agi.gretl.steps;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;

public class JsonImportStepTest {
    
    public JsonImportStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;

    // FIXME!!!
    @Ignore
    @Test
    public void importJsonObject_Ok() throws Exception {
        Connector connector = new Connector("jdbc:postgresql://localhost:54321/oereb", "admin", "admin");
        File jsonFile = TestUtil.createFile(folder, "{\"foo\":\"bar\"}", "test1.json");

        String schemaName = "jsonimport";
        String tableName = "jsonobject";
        String columnName = "json_text_col";
        
        Connection con = connector.connect();
        con.setAutoCommit(false);
        try {
            Statement stmt = con.createStatement();

            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id integer, "+columnName+" text);");
            con.commit();

        } catch (SQLException e) {
            log.info(e.getMessage());
        } finally {
            con.close();
        }
        
        JsonImportStep jsonImportStep = new JsonImportStep();
        jsonImportStep.execute(connector, jsonFile, schemaName+"."+tableName, columnName, true);
    }
    
    //@Test
    //appendJsonObject_Ok
}
