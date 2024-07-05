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
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ch.ehi.ili2db.base.DbNames;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class Ili2gpkgImportTest {

    @Test
    public void importOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2gpkgImport");

        Files.deleteIfExists(Paths.get(projectDirectory + "/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg"));

        IntegrationTestUtil.getGradleRunner(projectDirectory, "ili2gpkgimport").build();

        // check results
        {
            System.out.println("cwd " + new File(".").getAbsolutePath());
            Statement stmt = null;
            ResultSet rs = null;
            Connection connection = null;
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + new File(
                        projectDirectory + "/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg")
                                .getAbsolutePath());
                stmt = connection.createStatement();
                rs = stmt.executeQuery("SELECT aname FROM nachfuehrungsgeometer");

                List<String> geometer = new ArrayList<String>();
                geometer.add("K\u00e4gi");
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
    @Test
    public void importFileSet() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2gpkgImportFileSet");
        Files.deleteIfExists(Paths.get(projectDirectory + "/Beispiel2.gpkg"));

        IntegrationTestUtil.getGradleRunner(projectDirectory, "ili2gpkgimport").build();

        // check results
        {
            Statement stmt = null;
            ResultSet rs = null;
            Connection connection = null;
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(
                    projectDirectory + "/Beispiel2.gpkg")
                            .getAbsolutePath());
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT count(*) FROM boflaechen");

            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertEquals(4,count);
            rs.close();
            stmt.close();
            rs = stmt.executeQuery("SELECT "+DbNames.DATASETS_TAB_DATASETNAME+"  FROM "+DbNames.DATASETS_TAB);
            HashSet<String> datasets=new HashSet<String>();
            while(rs.next()) {
                datasets.add(rs.getString(1));
            }
            assertEquals(2,datasets.size());
            assertTrue(datasets.contains("DatasetA"));
            assertTrue(datasets.contains("DatasetB"));
            rs.close();
            stmt.close();
        }
    }
}
