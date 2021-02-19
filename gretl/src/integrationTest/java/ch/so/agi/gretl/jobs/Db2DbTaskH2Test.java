package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class Db2DbTaskH2Test {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";

    @ClassRule
    public static PostgreSQLContainer postgres =
            (PostgreSQLContainer) new PostgisContainerProvider()
                    .newInstance().withDatabaseName("gretl")
                    .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
                    .withInitScript("init_postgresql.sql")
                    .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

//    @Before
//    public void setup() {
//        String[] files = new File("src/integrationTest/jobs/Db2DbH2/").list();
//        for (String file : files) {
//            if (file.contains("db")) {
//                Paths.get("src/integrationTest/jobs/Db2DbH2/", file).toFile().delete();
//            }
//        }
//    }

    @Test
    public void pgToH2gis_Ok() throws Exception {
        // Prepare
        String h2TargetFileName = new File("src/integrationTest/jobs/Db2DbPg2H2gis/targetDb").getAbsolutePath();

        String[] files = new File("src/integrationTest/jobs/Db2DbPg2H2gis/").list();
        for (String file : files) {
            if (file.contains("db")) {
                Paths.get("src/integrationTest/jobs/Db2DbPg2H2gis/", file).toFile().delete();
            }
        }

        String sourceUrl = postgres.getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(sourceUrl, postgres.getUsername(), postgres.getPassword()); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE public.locations (t_id int PRIMARY KEY, geom geometry(POINT,2056), buffer geometry(POLYGON,2056))");
            stmt.execute("INSERT INTO public.locations VALUES (1, ST_PointFromText('POINT(2600000 1200000)', 2056))");
            stmt.execute("GRANT USAGE ON SCHEMA public TO dmluser");
            stmt.execute("GRANT select, insert, update, delete ON ALL TABLES IN SCHEMA public TO dmluser");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        String targetUrl = "jdbc:h2:" + h2TargetFileName;
        try (Connection conn = DriverManager.getConnection(targetUrl); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR \"org.h2gis.functions.factory.H2GISFunctions.load\"");
            stmt.execute("CALL H2GIS_SPATIAL()");
            stmt.execute("CREATE TABLE public.locations (t_id int IDENTITY PRIMARY KEY, geom geometry)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        // Run GRETL job
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Db2DbPg2H2gis", gvs);

        // Check results
        try (Connection conn = DriverManager.getConnection(targetUrl); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT t_id, ST_AsText(geom) FROM public.locations");

            int i=0;
            int tid = -99;
            String geomWkt = null;
            while(rs.next()) {
                tid = rs.getInt(1);
                geomWkt = rs.getString(2);
                i++;
            }
            Assert.assertEquals(1, tid);
            Assert.assertEquals("POINT (2600000 1200000)", geomWkt);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        try (Connection conn = DriverManager.getConnection(sourceUrl, postgres.getUsername(), postgres.getPassword()); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT t_id, ST_AsText(geom), ST_AsText(buffer) FROM public.locations");

            int i=0;
            int tid = -99;
            String geomWkt = null;
            String bufferWkt = null;
            while(rs.next()) {
                tid = rs.getInt(1);
                geomWkt = rs.getString(2);
                bufferWkt = rs.getString(3);
                i++;
            }
            Assert.assertEquals(1, i);
            Assert.assertEquals(1001, tid);
            Assert.assertEquals("POINT(2600000 1200000)", geomWkt);
            Assert.assertTrue(bufferWkt.contains("2600001 1200000"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @Test
    public void h2ToH2_Ok() throws Exception {
        // Prepare
        String h2SourceFileName = new File("src/integrationTest/jobs/Db2DbH2/sourceDb").getAbsolutePath();
        String h2TargetFileName = new File("src/integrationTest/jobs/Db2DbH2/targetDb").getAbsolutePath();

        String[] files = new File("src/integrationTest/jobs/Db2DbH2/").list();
        for (String file : files) {
            if (file.contains("db")) {
                Paths.get("src/integrationTest/jobs/Db2DbH2/", file).toFile().delete();
            }
        }

        String sourceUrl = "jdbc:h2:" + h2SourceFileName;
        try (Connection conn = DriverManager.getConnection(sourceUrl); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE colors (rot INT, gruen INT, blau INT, farbname TEXT)");
            stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        String targetUrl = "jdbc:h2:" + h2TargetFileName;
        try (Connection conn = DriverManager.getConnection(targetUrl); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE colors (rot INT, gruen INT, blau INT, farbname TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        // Run GRETL job
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Db2DbH2");

        // Check results
        try (Connection conn = DriverManager.getConnection(targetUrl); Statement stmt = conn.createStatement()) {
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
