package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.IntegrationTestUtil;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class Ili2duckdbImportSchemaTest {

    private final GretlLogger log;

    public Ili2duckdbImportSchemaTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void importOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2duckdbImportSchema");
        Files.deleteIfExists(Paths.get(projectDirectory.getAbsolutePath(), "my_gb2av.duckdb"));

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory);

        // Check results
        String url = "jdbc:duckdb:" + Paths.get(projectDirectory.getAbsolutePath() + "/my_gb2av.duckdb");

        try (Connection con = DriverManager.getConnection(url); Statement stmt = con.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT content FROM gb2av.t_ili2db_model")) {
                if (!rs.next()) {
                    fail();
                }

                assertTrue(rs.getString(1).contains("INTERLIS 2.2;"));

                if (rs.next()) {
                    fail();
                }
            }

            try (ResultSet rs = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema = 'gb2av' AND table_name  = 'vollzugsgegenstand' AND column_name = 'mutationsnummer'")) {
                if (!rs.next()) {
                    fail();
                }

                assertTrue(rs.getString(1).contains("mutationsnummer"));

                if (rs.next()) {
                    fail();
                }
            }
        }
    }
}
