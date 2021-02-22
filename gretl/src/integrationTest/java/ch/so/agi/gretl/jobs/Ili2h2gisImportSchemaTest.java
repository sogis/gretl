package ch.so.agi.gretl.jobs;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.Assert;

public class Ili2h2gisImportSchemaTest {
    @Test
    public void schemaImportOk() throws Exception {
        // Prepare
        String[] files = new File("src/integrationTest/jobs/Ili2h2gisImportSchema/").list();
        for (String file : files) {
            if (file.contains("db")) {
                Paths.get("src/integrationTest/jobs/Ili2h2gisImportSchema/", file).toFile().delete();
            }
        }
        
        // Run GRETL job
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2h2gisImportSchema");
        
        // Check results
        String testDbFileName = new File("src/integrationTest/jobs/Ili2h2gisImportSchema/testdb").getAbsolutePath();
        String testDbUrl = "jdbc:h2:" + testDbFileName;
        try (Connection conn = DriverManager.getConnection(testDbUrl); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT table_schema, table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_name = 'AMT'");
            while (rs.next()) {
                Assert.assertEquals("PUBLIC", rs.getString(1).toUpperCase());
                Assert.assertEquals("AMT", rs.getString(2).toUpperCase());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
