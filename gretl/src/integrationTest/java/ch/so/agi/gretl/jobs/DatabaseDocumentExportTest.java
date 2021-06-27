package ch.so.agi.gretl.jobs;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

public class DatabaseDocumentExportTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Test
    public void exportOk() throws Exception {
        String schemaName = "ada_denkmalschutz";
        String tableName = "fachapplikation_rechtsvorschrift_link";
        String columnName = "multimedia_link";
        
        File targetDir = new File("src/integrationTest/jobs/DatabaseDocumentExport/");

        Connection con = null;
        try {
            con = IntegrationTestUtilSql.connectPG(postgres);
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);

            Statement stmt = con.createStatement();
            
            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
            stmt.close();
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

            con.commit();

            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()) };
            IntegrationTestUtil.runJob("src/integrationTest/jobs/DatabaseDocumentExport", gvs);
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }
}
