package ch.so.agi.gretl.steps.publisher.in.db.schemacopy;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CopyOperationTest {
    @Test
    public void copiesTargetColumnsInCatalogOrderToMultipleTargetSchemas() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createSourceTables(statement);
            createTargetTables(statement, "PUB_A");
            createTargetTables(statement, "PUB_B");
            statement.execute("INSERT INTO PUB_A.PARCELS VALUES ('old-a', 999, 'stale')");
            statement.execute("INSERT INTO PUB_B.PARCELS VALUES ('old-b', 998, 'stale')");

            Request request = Request.builder("SRC")
                    .targetSchemas(Arrays.asList("PUB_A", "PUB_B"))
                    .table(TableCopySpecification.builder("PARCELS")
                            .rowFilter("STATUS = 'PUBLIC'")
                            .build())
                    .build();

            int copyCount = new CopyOperation(connection, request).execute();

            assertEquals(4, copyCount);
        }
    }

    @Test
    public void refusesMissingTargetTable() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createSourceTables(statement);
            statement.execute("CREATE SCHEMA PUB");

            Request request = Request.builder("SRC")
                    .targetSchema("PUB")
                    .table(TableCopySpecification.builder("PARCELS").build())
                    .build();

            assertThrows(RuntimeException.class, () -> new CopyOperation(connection, request).execute());
        }
    }

    @Test
    public void refusesTargetColumnThatDoesNotExistInSource() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createSourceTables(statement);
            createTargetTablesWithUnknownColumn(statement, "PUB");

            Request request = Request.builder("SRC")
                    .targetSchema("PUB")
                    .table(TableCopySpecification.builder("PARCELS").build())
                    .build();

            assertThrows(RuntimeException.class, () -> new CopyOperation(connection, request).execute());
        }
    }

    @Test
    public void refusesSameSchemaTarget() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createSourceTables(statement);

            Request request = Request.builder("SRC")
                    .targetSchema("SRC")
                    .table(TableCopySpecification.builder("PARCELS").build())
                    .build();

            assertThrows(IllegalArgumentException.class, () -> new CopyOperation(connection, request).execute());
        }
    }

    @Test
    public void whereInWhereclauseThrows() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createSourceTables(statement);

            Request withWhere = Request.builder("SRC")
                    .targetSchema("SRC")
                    .table(TableCopySpecification.builder("PARCELS").rowFilter("wHERe").build())
                    .build();

            assertThrows(IllegalArgumentException.class, () -> new CopyOperation(connection, withWhere).execute());


            Request withDelim = Request.builder("SRC")
                    .targetSchema("SRC")
                    .table(TableCopySpecification.builder("PARCELS").rowFilter(";").build())
                    .build();

            assertThrows(IllegalArgumentException.class, () -> new CopyOperation(connection, withDelim).execute());
        }
    }

    @Test
    public void delimInWhereclauseThrows() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createSourceTables(statement);

            Request withDelim = Request.builder("SRC")
                    .targetSchema("SRC")
                    .table(TableCopySpecification.builder("PARCELS").rowFilter(";").build())
                    .build();

            assertThrows(IllegalArgumentException.class, () -> new CopyOperation(connection, withDelim).execute());
        }
    }

    private Connection openDatabase() throws Exception {
        return DriverManager.getConnection("jdbc:derby:memory:db_copy_" + System.nanoTime() + ";create=true");
    }

    private void createSourceTables(Statement statement) throws Exception {
        statement.execute("CREATE SCHEMA SRC");
        statement.execute("CREATE TABLE SRC.PARCELS ("
                + "ID INTEGER, "
                + "NAME VARCHAR(50), "
                + "STATUS VARCHAR(20), "
                + "AREA DECIMAL(10,2))");
        statement.execute("INSERT INTO SRC.PARCELS VALUES (1, 'alpha', 'PUBLIC', 12.5)");
        statement.execute("INSERT INTO SRC.PARCELS VALUES (2, 'beta', 'PRIVATE', 7.0)");
        statement.execute("INSERT INTO SRC.PARCELS VALUES (3, 'gamma', 'PUBLIC', 8.75)");
    }

    private void createTargetTables(Statement statement, String schemaName) throws Exception {
        statement.execute("CREATE SCHEMA " + schemaName);
        statement.execute("CREATE TABLE " + schemaName + ".PARCELS ("
                + "NAME VARCHAR(50), "
                + "ID INTEGER, "
                + "STATUS VARCHAR(20))");
    }

    private void createTargetTablesWithUnknownColumn(Statement statement, String schemaName) throws Exception {
        statement.execute("CREATE SCHEMA " + schemaName);
        statement.execute("CREATE TABLE " + schemaName + ".PARCELS ("
                + "ID INTEGER, "
                + "NAME VARCHAR(50), "
                + "UNKNOWN_COL VARCHAR(20))");
    }

    private void assertTargetRows(Statement statement, String schemaName) throws Exception {
        try (ResultSet rs = statement.executeQuery("SELECT NAME, ID, STATUS FROM " + schemaName + ".PARCELS ORDER BY ID")) {
            rs.next();
            assertEquals("alpha", rs.getString("NAME"));
            assertEquals(1, rs.getInt("ID"));
            assertEquals("PUBLIC", rs.getString("STATUS"));

            rs.next();
            assertEquals("gamma", rs.getString("NAME"));
            assertEquals(3, rs.getInt("ID"));
            assertEquals("PUBLIC", rs.getString("STATUS"));
        }
    }
}
