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
import org.locationtech.jts.geom.Point;

import ch.so.agi.gretl.util.IntegrationTestUtil;

public class Ili2h2gisImportTest {
    @Test
    public void import_Ok() throws Exception {
        // Prepare
        String[] files = new File("src/integrationTest/jobs/Ili2h2gisImport/").list();
        for (String file : files) {
            if (file.contains("db")) {
                Paths.get("src/integrationTest/jobs/Ili2h2gisImport/", file).toFile().delete();
            }
        }
        
        // Run GRETL job
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2h2gisImport");
        
        // Check results
        String testDbFileName = new File("src/integrationTest/jobs/Ili2h2gisImport/254900").getAbsolutePath();
        String testDbUrl = "jdbc:h2:" + testDbFileName;
        try (Connection conn = DriverManager.getConnection(testDbUrl); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT nummer, geometrie, hoehegeom FROM LFP3 WHERE nummer = '11075350'");
            while (rs.next()) {
                Assert.assertEquals("11075350", rs.getString(1));
                Assert.assertEquals(2611985.735, ((Point)rs.getObject(2)).getX(), 0.001);
                Assert.assertEquals(584.330, rs.getDouble(3), 0.001);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
   
        

        
    }

}
