package ch.so.agi.gretl.jobs;

import static org.junit.Assert.*;

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

public class Ili2pgImportFileSetTest {
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
    public void importOk() throws Exception {
        Connection con = null;
        try {
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgImportFileSet", gvs);
            
            // check results
            con = IntegrationTestUtilSql.connectPG(postgres);
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT count(*) FROM beispiel2.boflaechen");

            assertTrue(rs.next());

            assertEquals(4,rs.getInt(1));
            
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }

}
