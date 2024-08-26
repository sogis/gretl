package ch.so.agi.gretl.jobs;

import ch.ehi.ili2db.base.DbNames;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        Files.deleteIfExists(Paths.get("src/integrationTest/jobs/Ili2gpkgImportFileSet/Beispiel2.gpkg")); 
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2gpkgImportFileSet", gvs);

        // check results
        {
            Statement stmt = null;
            ResultSet rs = null;
            Connection connection = null;
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(
                    "src/integrationTest/jobs/Ili2gpkgImportFileSet/Beispiel2.gpkg")
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
