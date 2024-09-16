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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

@Testcontainers
public class DatabaseDocumentExportTest {
    
    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void exportOk() throws Exception {
        seedDatabase();

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/DatabaseDocumentExport");
        GradleVariable[] variables = { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()) };

        IntegrationTestUtil.executeTestRunner(projectDirectory, "databasedocumentexport", variables);

    }

    private void seedDatabase() throws SQLException {
        String schemaName = "ada_denkmalschutz";
        String tableName = "fachapplikation_rechtsvorschrift_link";
        String columnName = "multimedia_link";

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
            stmt.close();
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

            con.commit();

            IntegrationTestUtilSql.closeCon(con);
        }
    }
}
