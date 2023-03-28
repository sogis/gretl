package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

public class Ili2pgImportSchemaTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Test
    public void schemaImportOk() throws Exception {
        Connection con = null;
        try {
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgImportSchema", gvs);
            
            // check results
            con = IntegrationTestUtilSql.connectPG(postgres);
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT content FROM gb2av.t_ili2db_model");

            if(!rs.next()) {
                fail();
            }

            assertTrue(rs.getString(1).contains("INTERLIS 2.2;"));
            
            if(rs.next()) {
                fail();
            }

            // check json mapping
            s = con.createStatement();
            rs = s.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_schema = 'gb2av' AND table_name  = 'vollzugsgegenstand' AND column_name = 'mutationsnummer'");
            
            if(!rs.next()) {
                fail();
            }
            
            assertTrue(rs.getString(1).contains("mutationsnummer"));

            if(rs.next()) {
                fail();
            }
            
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }
    
    @Test
    public void schemaImport_Options1_Ok() throws Exception {
        Connection con = null;
        try {
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgImportSchema_Options", gvs);
            
            // check results
            // check sqlColsAsText mapping
            con = IntegrationTestUtilSql.connectPG(postgres);
            Statement s = con.createStatement();
            ResultSet rs  = s.executeQuery("SELECT data_type FROM information_schema.columns WHERE table_schema = 'afu_abbaustellen_pub' AND table_name  = 'abbaustelle' AND column_name = 'gemeinde_bfs'");
            
            if(!rs.next()) {
                fail();
            }
            
            assertTrue(rs.getString(1).equalsIgnoreCase("text"));

            if(rs.next()) {
                fail();
            }
            
            // check sqlExtRefCols mapping
            s = con.createStatement();
            rs = s.executeQuery("SELECT data_type FROM information_schema.columns WHERE table_schema = 'afu_abbaustellen_pub' AND table_name  = 'abbaustelle' AND column_name = 'geometrie'");
            
            if(!rs.next()) {
                fail();
            }
            
            assertTrue(rs.getString(1).equalsIgnoreCase("character varying"));

            if(rs.next()) {
                fail();
            }

            
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }

}
