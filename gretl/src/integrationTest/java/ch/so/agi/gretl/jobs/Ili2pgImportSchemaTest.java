package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class Ili2pgImportSchemaTest {
    
    @Container
    public static PostgreSQLContainer<?> postgres =
            (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                .withDatabaseName("gretl")
                .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
                .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
                .withInitScript("init_postgresql.sql")
                .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void schemaImportOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgImportSchema");
        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, variables);

        // Check results
        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
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
    
    @Test
    public void schemaImport_Options1_Ok() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgImportSchema_Options");
        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, variables);

        // Check results
        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT data_type FROM information_schema.columns WHERE table_schema = 'afu_abbaustellen_pub' AND table_name  = 'abbaustelle' AND column_name = 'gemeinde_bfs'")) {
                if (!rs.next()) {
                    fail();
                }

                assertTrue(rs.getString(1).equalsIgnoreCase("text"));

                if (rs.next()) {
                    fail();
                }
            }

            // Check sqlExtRefCols mapping
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT data_type FROM information_schema.columns WHERE table_schema = 'afu_abbaustellen_pub' AND table_name  = 'abbaustelle' AND column_name = 'geometrie'")) {
                if (!rs.next()) {
                    fail();
                }

                assertTrue(rs.getString(1).equalsIgnoreCase("character varying"));

                if (rs.next()) {
                    fail();
                }
            }
        }
    }
    
    @Test
    public void schemaImport_MetaConfigIliData_Ok() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgImportSchema_MetaConfigIliData");        
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, gvs);
        
        // Check results
        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT setting FROM simple_table_ilidata.t_ili2db_settings WHERE tag ILIKE 'ch.ehi.ili2db.metaConfigFileName'");

            if (!rs.next()) {
                fail();
            }

            assertTrue(rs.getString(1).contains("ilidata:metaconfig_simple_table_ini_20240502"));

            if (rs.next()) {
                fail();
            }
        }
    }

    @Test
    public void schemaImport_MetaConfigFile_Ok() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgImportSchema_MetaConfigFile");
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory, gvs);

        // Check results
        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT setting FROM simple_table_metaconfigfile.t_ili2db_settings WHERE tag ILIKE 'ch.ehi.ili2db.metaConfigFileName'");

            if(!rs.next()) {
                fail();
            }

            assertTrue(rs.getString(1).contains("simple_table_ini_20240502.ini"));

            if(rs.next()) {
                fail();
            }
        }
    }
}
