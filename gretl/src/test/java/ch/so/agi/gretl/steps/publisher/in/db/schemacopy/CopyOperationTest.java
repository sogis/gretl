package ch.so.agi.gretl.steps.publisher.in.db.schemacopy;

import ch.ehi.ili2db.base.DbNames;
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
            assertTargetRows(statement, "PUB_A");
            assertTargetRows(statement, "PUB_B");
        }
    }

    @Test
    public void copiesBasketMetadataWhenSourceAndTargetHaveBasketReferences() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createBasketAwareSourceTables(statement);
            createBasketAwareTargetTables(statement, "PUB");
            insertStaleBasketAwareTargetRows(statement, "PUB");

            Request request = Request.builder("SRC")
                    .targetSchema("PUB")
                    .table(TableCopySpecification.builder("PARCELS")
                            .rowFilter("STATUS = 'PUBLIC'")
                            .build())
                    .build();

            int copyCount = new CopyOperation(connection, request).execute();

            assertEquals(2, copyCount);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB." + DbNames.DATASETS_TAB, 2);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB." + DbNames.BASKETS_TAB, 2);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB.PARCELS", 2);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB.PARCELS p JOIN PUB." + DbNames.BASKETS_TAB
                    + " b ON p.T_BASKET = b.T_ID", 2);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB.PARCELS WHERE T_BASKET = 999", 0);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB." + DbNames.DATASETS_TAB + " WHERE T_ID = 999", 0);
        }
    }

    @Test
    public void sourceBasketReferenceWithTargetWithoutBasketReferenceCopiesDataOnly() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createBasketAwareSourceTables(statement);
            createTargetTables(statement, "PUB");
            statement.execute("INSERT INTO PUB.PARCELS VALUES ('old', 999, 'stale')");

            Request request = Request.builder("SRC")
                    .targetSchema("PUB")
                    .table(TableCopySpecification.builder("PARCELS")
                            .rowFilter("STATUS = 'PUBLIC'")
                            .build())
                    .build();

            int copyCount = new CopyOperation(connection, request).execute();

            assertEquals(2, copyCount);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB.PARCELS", 2);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB.PARCELS WHERE NAME = 'old'", 0);
            assertTargetRows(statement, "PUB");
        }
    }

    @Test
    public void targetBasketReferenceWithoutSourceBasketReferenceFailsBeforeMutation() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createSourceTables(statement);
            createBasketAwareTargetTables(statement, "PUB");
            insertStaleBasketAwareTargetRows(statement, "PUB");

            Request request = Request.builder("SRC")
                    .targetSchema("PUB")
                    .table(TableCopySpecification.builder("PARCELS").build())
                    .build();

            assertThrows(IllegalArgumentException.class, () -> new CopyOperation(connection, request).execute());
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB.PARCELS", 1);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB." + DbNames.BASKETS_TAB, 1);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB." + DbNames.DATASETS_TAB, 1);
        }
    }

    @Test
    public void mixedBasketReferenceModesFailBeforeMutation() throws Exception {
        try (Connection connection = openDatabase(); Statement statement = connection.createStatement()) {
            createMixedBasketModeSourceTables(statement);
            createMixedBasketModeTargetTables(statement, "PUB");
            insertStaleBasketAwareTargetRows(statement, "PUB");
            statement.execute("INSERT INTO PUB.OWNERS VALUES (999, 'old-owner')");

            Request request = Request.builder("SRC")
                    .targetSchema("PUB")
                    .table(TableCopySpecification.builder("PARCELS").build())
                    .table(TableCopySpecification.builder("OWNERS").build())
                    .build();

            assertThrows(IllegalArgumentException.class, () -> new CopyOperation(connection, request).execute());
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB.PARCELS", 1);
            assertSingleValue(statement, "SELECT COUNT(*) FROM PUB.OWNERS", 1);
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

    private void createBasketAwareSourceTables(Statement statement) throws Exception {
        statement.execute("CREATE SCHEMA SRC");
        createBasketMetadataTables(statement, "SRC");
        statement.execute("CREATE TABLE SRC.PARCELS ("
                + "ID INTEGER, "
                + "NAME VARCHAR(50), "
                + "STATUS VARCHAR(20), "
                + "AREA DECIMAL(10,2), "
                + "T_BASKET INTEGER NOT NULL, "
                + "CONSTRAINT SRC_PARCELS_BASKET_FK FOREIGN KEY (T_BASKET) REFERENCES SRC."
                + DbNames.BASKETS_TAB + "(T_ID))");
        insertSourceBasketMetadata(statement, "SRC");
        statement.execute("INSERT INTO SRC.PARCELS VALUES (1, 'alpha', 'PUBLIC', 12.5, 100)");
        statement.execute("INSERT INTO SRC.PARCELS VALUES (2, 'beta', 'PRIVATE', 7.0, 100)");
        statement.execute("INSERT INTO SRC.PARCELS VALUES (3, 'gamma', 'PUBLIC', 8.75, 200)");
    }

    private void createBasketAwareTargetTables(Statement statement, String schemaName) throws Exception {
        statement.execute("CREATE SCHEMA " + schemaName);
        createBasketMetadataTables(statement, schemaName);
        statement.execute("CREATE TABLE " + schemaName + ".PARCELS ("
                + "ID INTEGER, "
                + "NAME VARCHAR(50), "
                + "STATUS VARCHAR(20), "
                + "T_BASKET INTEGER NOT NULL, "
                + "CONSTRAINT " + schemaName + "_PARCELS_BASKET_FK FOREIGN KEY (T_BASKET) REFERENCES "
                + schemaName + "." + DbNames.BASKETS_TAB + "(T_ID))");
    }

    private void createMixedBasketModeSourceTables(Statement statement) throws Exception {
        createBasketAwareSourceTables(statement);
        statement.execute("CREATE TABLE SRC.OWNERS (ID INTEGER, NAME VARCHAR(50))");
        statement.execute("INSERT INTO SRC.OWNERS VALUES (1, 'owner')");
    }

    private void createMixedBasketModeTargetTables(Statement statement, String schemaName) throws Exception {
        createBasketAwareTargetTables(statement, schemaName);
        statement.execute("CREATE TABLE " + schemaName + ".OWNERS (ID INTEGER, NAME VARCHAR(50))");
    }

    private void createBasketMetadataTables(Statement statement, String schemaName) throws Exception {
        statement.execute("CREATE TABLE " + schemaName + "." + DbNames.DATASETS_TAB + " ("
                + "T_ID INTEGER NOT NULL PRIMARY KEY, "
                + "DATASETNAME VARCHAR(50))");
        statement.execute("CREATE TABLE " + schemaName + "." + DbNames.BASKETS_TAB + " ("
                + "T_ID INTEGER NOT NULL PRIMARY KEY, "
                + "TOPIC VARCHAR(100), "
                + "DATASET INTEGER NOT NULL, "
                + "CONSTRAINT " + schemaName + "_BASKET_DATASET_FK FOREIGN KEY (DATASET) REFERENCES "
                + schemaName + "." + DbNames.DATASETS_TAB + "(T_ID))");
    }

    private void insertSourceBasketMetadata(Statement statement, String schemaName) throws Exception {
        statement.execute("INSERT INTO " + schemaName + "." + DbNames.DATASETS_TAB + " VALUES (10, 'DatasetA')");
        statement.execute("INSERT INTO " + schemaName + "." + DbNames.DATASETS_TAB + " VALUES (20, 'DatasetB')");
        statement.execute("INSERT INTO " + schemaName + "." + DbNames.BASKETS_TAB + " VALUES (100, 'TopicA', 10)");
        statement.execute("INSERT INTO " + schemaName + "." + DbNames.BASKETS_TAB + " VALUES (200, 'TopicB', 20)");
    }

    private void insertStaleBasketAwareTargetRows(Statement statement, String schemaName) throws Exception {
        statement.execute("INSERT INTO " + schemaName + "." + DbNames.DATASETS_TAB + " VALUES (999, 'StaleDataset')");
        statement.execute("INSERT INTO " + schemaName + "." + DbNames.BASKETS_TAB + " VALUES (999, 'StaleTopic', 999)");
        statement.execute("INSERT INTO " + schemaName + ".PARCELS VALUES (999, 'old', 'PUBLIC', 999)");
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

    private void assertSingleValue(Statement statement, String sql, int expected) throws Exception {
        try (ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            assertEquals(expected, rs.getInt(1));
        }
    }
}
