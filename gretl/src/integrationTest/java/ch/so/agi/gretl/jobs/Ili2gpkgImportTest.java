package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class Ili2gpkgImportTest {

    @Test
    public void importOk() throws Exception {
        Files.deleteIfExists(Paths.get("src/integrationTest/jobs/Ili2gpkgImport/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg")); 
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2gpkgImport", gvs);

        // check results
        {
            System.out.println("cwd " + new File(".").getAbsolutePath());
            Statement stmt = null;
            ResultSet rs = null;
            Connection connection = null;
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + new File(
                        "src/integrationTest/jobs/Ili2gpkgImport/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg")
                                .getAbsolutePath());
                stmt = connection.createStatement();
                rs = stmt.executeQuery("SELECT aname FROM nachfuehrungsgeometer");

                List<String> geometer = new ArrayList<String>();
                geometer.add("Kägi");
                geometer.add("Cantaluppi");
                geometer.add("Schor");
                geometer.add("Meile");
                geometer.add("Weber");
                int i = 0;
                while (rs.next()) {
                    i++;
                    String geometerName = rs.getString("aname");
                    assertTrue(geometer.contains(geometerName));
                }
                rs.close();
                stmt.close();
                assertEquals(5, i);
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
        }
    }
}
