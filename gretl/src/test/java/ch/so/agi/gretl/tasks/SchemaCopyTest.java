package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.steps.schemacopy.Request;
import ch.so.agi.gretl.steps.schemacopy.TableCopySpecification;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchemaCopyTest {
    @Test
    void createsRequestFromOptionalTableListsInDefinedOrder() {
        SchemaCopy task = configuredTask();
        task.setTables(Arrays.asList("PARCELS", "ROADS"));
        task.setTablesWithFilter(Arrays.asList("BUILDINGS", "STATUS = 'PUBLIC'", "ADDRESSES", "MUNICIPALITY = 'OLTEN'"));

        Request request = task.createRequest();

        assertEquals("EDIT", request.getSourceSchema());
        assertEquals(List.of("PUBLISH"), request.getTargetSchemas());
        assertSpecifications(request.getSourceTables(),
                "PARCELS", null,
                "ROADS", null,
                "BUILDINGS", "STATUS = 'PUBLIC'",
                "ADDRESSES", "MUNICIPALITY = 'OLTEN'");
    }

    @Test
    void acceptsFilteredTablesWithoutUnfilteredTables() {
        SchemaCopy task = configuredTask();
        task.setTargetSchema(null);
        task.setTargetSchemas(Arrays.asList("PUBLISH_A", "PUBLISH_B"));
        task.setTablesWithFilter(Arrays.asList("BUILDINGS", "STATUS = 'PUBLIC'"));

        Request request = task.createRequest();

        assertEquals(List.of("PUBLISH_A", "PUBLISH_B"), request.getTargetSchemas());
        assertSpecifications(request.getSourceTables(), "BUILDINGS", "STATUS = 'PUBLIC'");
    }

    @Test
    void rejectsOddNumberOfFilteredTableValues() {
        SchemaCopy task = configuredTask();
        task.setTablesWithFilter(List.of("BUILDINGS"));

        assertThrows(IllegalArgumentException.class, task::createRequest);
    }

    @Test
    void rejectsDuplicateTablesAcrossBothLists() {
        SchemaCopy task = configuredTask();
        task.setTables(List.of("PARCELS"));
        task.setTablesWithFilter(Arrays.asList("parcels", "STATUS = 'PUBLIC'"));

        assertThrows(IllegalArgumentException.class, task::createRequest);
    }

    @Test
    void rejectsBlankAndUnsafeFilters() {
        SchemaCopy task = configuredTask();
        task.setTablesWithFilter(Arrays.asList("PARCELS", "WHERE STATUS = 'PUBLIC'"));
        assertThrows(IllegalArgumentException.class, task::createRequest);

        task.setTablesWithFilter(Arrays.asList("PARCELS", " "));
        assertThrows(IllegalArgumentException.class, task::createRequest);
    }

    @Test
    void rejectsAbsentTablesAndConflictingTargetSelectors() {
        SchemaCopy task = configuredTask();
        assertThrows(IllegalArgumentException.class, task::createRequest);

        task.setTables(List.of("PARCELS"));
        task.setTargetSchemas(List.of("PUBLISH_B"));
        assertThrows(IllegalArgumentException.class, task::createRequest);
    }

    @Test
    void copiesUnfilteredAndFilteredTablesToEveryTargetSchema() throws Exception {
        String databaseUrl = "jdbc:derby:memory:schemacopy_task_" + System.nanoTime() + ";create=true";
        createCopyTables(databaseUrl);

        Project project = ProjectBuilder.builder().build();
        SchemaCopy task = project.getTasks().create("copy", SchemaCopy.class);
        task.setDatabase(List.of(databaseUrl));
        task.setSourceSchema("SRC");
        task.setTargetSchemas(List.of("PUB_A", "PUB_B"));
        task.setTables(List.of("ROADS"));
        task.setTablesWithFilter(List.of("PARCELS", "STATUS = 'PUBLIC'"));

        task.copy();

        try (Connection connection = DriverManager.getConnection(databaseUrl); Statement statement = connection.createStatement()) {
            assertRowCount(statement, "PUB_A", "ROADS", 2);
            assertRowCount(statement, "PUB_B", "ROADS", 2);
            assertRowCount(statement, "PUB_A", "PARCELS", 2);
            assertRowCount(statement, "PUB_B", "PARCELS", 2);
        }
    }

    private static SchemaCopy configuredTask() {
        Project project = ProjectBuilder.builder().build();
        SchemaCopy task = project.getTasks().create("copy", SchemaCopy.class);
        task.setDatabase(Arrays.asList("jdbc:derby:memory:schemacopy"));
        task.setSourceSchema("EDIT");
        task.setTargetSchema("PUBLISH");
        return task;
    }

    private static void createCopyTables(String databaseUrl) throws Exception {
        try (Connection connection = DriverManager.getConnection(databaseUrl); Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA SRC");
            statement.execute("CREATE TABLE SRC.ROADS (ID INTEGER, NAME VARCHAR(50))");
            statement.execute("CREATE TABLE SRC.PARCELS (ID INTEGER, STATUS VARCHAR(20))");
            statement.execute("INSERT INTO SRC.ROADS VALUES (1, 'A')");
            statement.execute("INSERT INTO SRC.ROADS VALUES (2, 'B')");
            statement.execute("INSERT INTO SRC.PARCELS VALUES (1, 'PUBLIC')");
            statement.execute("INSERT INTO SRC.PARCELS VALUES (2, 'PRIVATE')");
            statement.execute("INSERT INTO SRC.PARCELS VALUES (3, 'PUBLIC')");
            for (String schema : List.of("PUB_A", "PUB_B")) {
                statement.execute("CREATE SCHEMA " + schema);
                statement.execute("CREATE TABLE " + schema + ".ROADS (ID INTEGER, NAME VARCHAR(50))");
                statement.execute("CREATE TABLE " + schema + ".PARCELS (ID INTEGER, STATUS VARCHAR(20))");
                statement.execute("INSERT INTO " + schema + ".ROADS VALUES (999, 'stale')");
                statement.execute("INSERT INTO " + schema + ".PARCELS VALUES (999, 'PRIVATE')");
            }
        }
    }

    private static void assertRowCount(Statement statement, String schema, String table, int expected) throws Exception {
        try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + schema + "." + table)) {
            resultSet.next();
            assertEquals(expected, resultSet.getInt(1));
        }
    }

    private static void assertSpecifications(List<TableCopySpecification> specifications, String... expectedValues) {
        assertEquals(expectedValues.length / 2, specifications.size());
        for (int index = 0; index < specifications.size(); index++) {
            TableCopySpecification specification = specifications.get(index);
            assertEquals(expectedValues[index * 2], specification.getTableName());
            assertEquals(expectedValues[index * 2 + 1], specification.getRowFilter());
        }
    }
}
