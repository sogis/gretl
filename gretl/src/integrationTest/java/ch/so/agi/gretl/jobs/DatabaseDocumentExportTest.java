package ch.so.agi.gretl.jobs;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.After;
import org.junit.Before;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import static org.junit.Assert.assertEquals;

public class DatabaseDocumentExportTest {
    private Connection connection = null;

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Before
    public void setup() {
        connection = IntegrationTestUtilSql.connectPG(postgres);
    }

    @After
    public void tearDown() {
        IntegrationTestUtilSql.closeCon(connection);
    }

    @Test
    public void exportOk() throws Exception {
        String schemaName = "ada_denkmalschutz";
        String tableName = "fachapplikation_rechtsvorschrift_link";
        String columnName = "multimedia_link";

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);

        Statement stmt = connection.createStatement();

        stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
        stmt.execute("CREATE SCHEMA "+schemaName+";");
        stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
        stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
        stmt.close();
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/DatabaseDocumentExport");
        GradleVariable[] variables = { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()) };

        IntegrationTestUtil.executeTestRunner(projectDirectory, "databasedocumentexport", variables);
    }
}
