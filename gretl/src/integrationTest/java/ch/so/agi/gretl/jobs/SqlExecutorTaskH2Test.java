package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;

public class SqlExecutorTaskH2Test {
    private String h2FileName = new File("src/integrationTest/jobs/SqlExecutorH2/sourceDb").getAbsolutePath();

    @Before
    public void setup() {
        String[] files = new File("src/integrationTest/jobs/SqlExecutorH2/").list();
        for (String file : files) {
            if (file.contains("db")) {
                Paths.get("src/integrationTest/jobs/SqlExecutorH2/", file).toFile().delete();
            }
        }
    }

    @Test
    public void h2_Ok() throws Exception {
        // Prepare
        String sourceUrl = "jdbc:h2:" + h2FileName;
        try (Connection conn = DriverManager.getConnection(sourceUrl); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE colors (rot INT, gruen INT, blau INT, farbname TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        // Run GRETL job
        IntegrationTestUtil.runJob("src/integrationTest/jobs/SqlExecutorH2");

        // Check results
        try (Connection conn = DriverManager.getConnection(sourceUrl); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM colors");

            int i = 0;
            int red = -99;
            int green = -99;
            int blue = -99;
            String farbname = null;
            while(rs.next()) {
                red = rs.getInt("rot");
                green = rs.getInt("gruen");
                blue = rs.getInt("blau");
                farbname = rs.getString("farbname");
                i++;
            }
            Assert.assertEquals(1, i);
            Assert.assertEquals(255, red);
            Assert.assertEquals(0, green);
            Assert.assertEquals(0, blue);
            Assert.assertEquals("rot", farbname);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
