package ch.so.agi.gretl.jobs;

import ch.ehi.ili2db.base.DbNames;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class Ili2gpkgImportTest {

    private final GretlLogger log;

    public Ili2gpkgImportTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void importOk() throws Exception {
        Files.deleteIfExists(Paths.get("src/integrationTest/jobs/Ili2gpkgImport/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg"));
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2gpkgImport", null);
        String url = "jdbc:sqlite:" +
                new File("src/integrationTest/jobs/Ili2gpkgImport/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg").getAbsolutePath();

        // Check results
        try (
            Connection connection = DriverManager.getConnection(url);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT aname FROM nachfuehrungsgeometer")
        ) {
            log.info("cwd " + new File(".").getAbsolutePath());
            List<String> geometer = new ArrayList<String>(){{
                add("K\u00e4gi");
                add("Cantaluppi");
                add("Schor");
                add("Meile");
                add("Weber");
            }};

            int i = 0;
            while (rs.next()) {
                i++;
                String geometerName = rs.getString("aname");
                assertTrue(geometer.contains(geometerName));
            }
            assertEquals(5, i);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    public void importFileSet() throws Exception {
        Files.deleteIfExists(Paths.get("src/integrationTest/jobs/Ili2gpkgImportFileSet/Beispiel2.gpkg"));
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2gpkgImportFileSet", null);
        String url = "jdbc:sqlite:" +
                new File("src/integrationTest/jobs/Ili2gpkgImportFileSet/Beispiel2.gpkg").getAbsolutePath();

        // Check results
        try (
            Connection connection = DriverManager.getConnection(url);
            Statement stmt = connection.createStatement()
        ) {
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM boflaechen")) {
                assertTrue(rs.next());
                int count = rs.getInt(1);
                assertEquals(4, count);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT " + DbNames.DATASETS_TAB_DATASETNAME + "  FROM " + DbNames.DATASETS_TAB)) {
                Set<String> datasets = new HashSet<>();

                while (rs.next()) {
                    datasets.add(rs.getString(1));
                }

                assertEquals(2,datasets.size());
                assertTrue(datasets.contains("DatasetA"));
                assertTrue(datasets.contains("DatasetB"));
            }
        }
    }
}
