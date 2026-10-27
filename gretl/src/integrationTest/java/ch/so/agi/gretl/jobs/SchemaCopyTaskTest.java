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
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class SchemaCopyTaskTest {
    private static final String SOURCE_SCHEMA = "schemacopytasktest_src";
    private static final String TARGET_SCHEMA = "schemacopytasktest_pub";

    private final GradleVariable[] gradleVariables = {
            GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())
    };

    @Container
    public static PostgreSQLContainer<?> postgres =
            (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                    .withDatabaseName("gretl")
                    .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
                    .withInitScript("init_postgresql.sql")
                    .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void copiesFourTablesToOneSchemaWithTwoRowFilters() throws Exception {
        try (Connection connection = IntegrationTestUtilSql.connectPG(postgres);
                Statement statement = connection.createStatement()) {
            createSchemas(connection);
            createTables(statement, SOURCE_SCHEMA);
            createTables(statement, TARGET_SCHEMA);
            insertSourceRows(statement);
            insertStaleTargetRows(statement);
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(
                    connection, SOURCE_SCHEMA, IntegrationTestUtilSql.PG_CON_DMLUSER);
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(
                    connection, TARGET_SCHEMA, IntegrationTestUtilSql.PG_CON_DMLUSER);
            connection.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir")
                + "/src/integrationTest/jobs/SchemaCopy");
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        try (Connection connection = IntegrationTestUtilSql.connectPG(postgres)) {
            assertCount(connection, TARGET_SCHEMA + ".roads", 2);
            assertCount(connection, TARGET_SCHEMA + ".addresses", 2);
            assertCount(connection, TARGET_SCHEMA + ".parcels", 2);
            assertCount(connection, TARGET_SCHEMA + ".buildings", 2);
            assertCount(connection, TARGET_SCHEMA + ".roads WHERE id = 999", 0);
            assertCount(connection, TARGET_SCHEMA + ".addresses WHERE id = 999", 0);
            assertCount(connection, TARGET_SCHEMA + ".parcels WHERE status = 'PRIVATE'", 0);
            assertCount(connection, TARGET_SCHEMA + ".buildings WHERE published = false", 0);
        }
    }

    private static void createSchemas(Connection connection) {
        IntegrationTestUtilSql.createOrReplaceSchema(connection, SOURCE_SCHEMA);
        IntegrationTestUtilSql.createOrReplaceSchema(connection, TARGET_SCHEMA);
    }

    private static void createTables(Statement statement, String schema) throws Exception {
        statement.execute("CREATE TABLE " + schema + ".roads (id INTEGER, name TEXT)");
        statement.execute("CREATE TABLE " + schema + ".addresses (id INTEGER, street TEXT)");
        statement.execute("CREATE TABLE " + schema + ".parcels (id INTEGER, status TEXT)");
        statement.execute("CREATE TABLE " + schema + ".buildings (id INTEGER, published BOOLEAN)");
    }

    private static void insertSourceRows(Statement statement) throws Exception {
        statement.execute("INSERT INTO " + SOURCE_SCHEMA + ".roads VALUES (1, 'A'), (2, 'B')");
        statement.execute("INSERT INTO " + SOURCE_SCHEMA + ".addresses VALUES (1, 'First'), (2, 'Second')");
        statement.execute("INSERT INTO " + SOURCE_SCHEMA
                + ".parcels VALUES (1, 'PUBLIC'), (2, 'PRIVATE'), (3, 'PUBLIC')");
        statement.execute("INSERT INTO " + SOURCE_SCHEMA
                + ".buildings VALUES (1, true), (2, false), (3, true)");
    }

    private static void insertStaleTargetRows(Statement statement) throws Exception {
        statement.execute("INSERT INTO " + TARGET_SCHEMA + ".roads VALUES (999, 'stale')");
        statement.execute("INSERT INTO " + TARGET_SCHEMA + ".addresses VALUES (999, 'stale')");
        statement.execute("INSERT INTO " + TARGET_SCHEMA + ".parcels VALUES (999, 'STALE')");
        statement.execute("INSERT INTO " + TARGET_SCHEMA + ".buildings VALUES (999, false)");
    }

    private static void assertCount(Connection connection, String relation, int expected) {
        int actual = IntegrationTestUtilSql.execCountQuery(connection, "SELECT COUNT(*) FROM " + relation);
        assertEquals(expected, actual, relation);
    }
}
