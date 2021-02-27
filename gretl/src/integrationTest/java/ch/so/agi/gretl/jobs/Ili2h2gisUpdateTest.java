package ch.so.agi.gretl.jobs;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

import ch.so.agi.gretl.util.IntegrationTestUtil;

public class Ili2h2gisUpdateTest {
    @Test
    public void replace_Ok() throws Exception {
        // Prepare
        String[] files = new File("src/integrationTest/jobs/Ili2h2gisUpdate/").list();
        for (String file : files) {
            if (file.contains("db")) {
                Paths.get("src/integrationTest/jobs/Ili2h2gisUpdate/", file).toFile().delete();
            }
        }
        
        // Run GRETL job
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2h2gisUpdate");
        
        // Check results
        String testDbFileName = new File("src/integrationTest/jobs/Ili2h2gisUpdate/ch.so.sk.gesetze").getAbsolutePath();
        String testDbUrl = "jdbc:h2:" + testDbFileName;
        try (Connection conn = DriverManager.getConnection(testDbUrl); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM DOKUMENT");
            while (rs.next()) {
                Assert.assertEquals(9, rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
