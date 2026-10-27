package ch.so.agi.gretl.steps.schemacopy;

import ch.ehi.ili2db.base.DbNames;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsibility: copy selected source-schema tables into pre-existing target
 * schemas and tables in one transaction by reading catalog metadata and executing DML.
 */
public class CopyOperation {
    private static final GretlLogger log = LogEnvironment.getLogger(CopyOperation.class);
    private static final String DATASET_TABLE = DbNames.DATASETS_TAB;
    private static final String BASKET_TABLE = DbNames.BASKETS_TAB;
    private static final String T_BASKET_COLUMN = DbNames.T_BASKET_COL;

    private final Connector database;
    private Connection con;

    public CopyOperation(Connector database) {
        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        this.database = database;
    }

    public int execute(Request copyRequest) throws SQLException {
        if (copyRequest == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        Exception failure = null;
        int totalCopiedRows = 0;

        try {
            con = database.connect();
            log.debug("Starting schema copy transaction.");
            List<TargetCopyPlan> targetPlans = createTargetCopyPlans(copyRequest);
            log.debug("Validated schema copy plan: " + copiedTables(targetPlans));

            for (TargetCopyPlan targetPlan : targetPlans) {
                String targetSchema = targetPlan.getTargetSchema();
                List<String> tableInserts = new ArrayList<>();

                try {
                    if (targetPlan.isBasketAware()) {
                        deleteDataTables(targetPlan);
                        int deletedBaskets = deleteTableRows(targetSchema, BASKET_TABLE);
                        int deletedDatasets = deleteTableRows(targetSchema, DATASET_TABLE);
                        int copiedDatasets = copyWholeTable(copyRequest.getSourceSchema(), targetSchema, DATASET_TABLE);
                        int copiedBaskets = copyWholeTable(copyRequest.getSourceSchema(), targetSchema, BASKET_TABLE);

                        log.info(String.format(
                                "%s metadata: deleted datasets:%d, baskets:%d; inserted datasets:%d, baskets:%d",
                                targetSchema, deletedDatasets, deletedBaskets, copiedDatasets, copiedBaskets));
                    }

                    for (TableCopyPlan tablePlan : targetPlan.getTablePlans()) {
                        log.debug("Copying " + tablePlan.getTargetSchema() + "."
                                + tablePlan.getSpec().getTableName());
                        int copiedRows = targetPlan.isBasketAware()
                                ? copyTableData(copyRequest.getSourceSchema(), tablePlan)
                                : replaceTableData(copyRequest.getSourceSchema(), tablePlan);
                        tableInserts.add(tablePlan.getSpec().getTableName() + ":" + copiedRows);
                        totalCopiedRows += copiedRows;
                    }
                } catch (Exception e) {
                    log.error(String.format("Failed to copy to schema %s", targetSchema), e);
                    throw e;
                }

                log.info(targetSchema + " inserts: " + String.join(", ", tableInserts));
            }

            con.commit();
            log.debug("Committed schema copy transaction.");
            log.info(String.format("Schema copy committed: %d data rows copied to %s",
                    totalCopiedRows, copiedTables(targetPlans)));
            return totalCopiedRows;
        } catch (SQLException exception) {
            RuntimeException wrappedException = new RuntimeException(exception);
            failure = wrappedException;
            rollback(wrappedException);
            log.error("Schema copy operation failed.", wrappedException);
            throw wrappedException;
        } catch (RuntimeException exception) {
            failure = exception;
            rollback(exception);
            log.error("Schema copy operation failed.", exception);
            throw exception;
        } finally {
            try {
                database.close();
            } catch (SQLException closeException) {
                if (failure != null) {
                    failure.addSuppressed(closeException);
                } else {
                    throw closeException;
                }
            } finally {
                con = null;
            }
        }
    }

    private List<TargetCopyPlan> createTargetCopyPlans(Request copyRequest) throws SQLException {
        List<TargetCopyPlan> targetPlans = new ArrayList<>();
        for (String targetSchema : copyRequest.getTargetSchemas()) {
            targetPlans.add(createTargetCopyPlan(copyRequest, targetSchema));
        }
        return targetPlans;
    }

    private TargetCopyPlan createTargetCopyPlan(Request copyRequest, String targetSchema) throws SQLException {
        if (matchesIdentifier(copyRequest.getSourceSchema(), targetSchema)) {
            throw new IllegalArgumentException("target schema <" + targetSchema
                    + "> must be different from source schema <" + copyRequest.getSourceSchema() + ">");
        }

        List<TableCopyPlan> tablePlans = new ArrayList<>();
        boolean basketAware = false;

        for (TableCopySpecification spec : copyRequest.getSourceTables()) {
            if (spec.getRowFilter() != null) {
                assertValidRowFilter(spec.getRowFilter());
            }

            List<String> sourceColumns = readColumns(con.getMetaData(), copyRequest.getSourceSchema(), spec.getTableName());
            List<String> targetColumns = readColumns(con.getMetaData(), targetSchema, spec.getTableName());
            boolean sourceHasBasketRef = containsIdentifier(sourceColumns, T_BASKET_COLUMN);
            boolean targetHasBasketRef = containsIdentifier(targetColumns, T_BASKET_COLUMN);

            if (!sourceHasBasketRef && targetHasBasketRef) {
                throw new IllegalArgumentException(String.format(
                        "target table <%s.%s> has basket column <%s>, but source table <%s.%s> does not",
                        targetSchema, spec.getTableName(), T_BASKET_COLUMN,
                        copyRequest.getSourceSchema(), spec.getTableName()));
            }

            basketAware |= sourceHasBasketRef && targetHasBasketRef;

            tablePlans.add(new TableCopyPlan(spec, targetSchema, targetColumns));
        }

        if (basketAware) {
            assertTableExists(copyRequest.getSourceSchema(), DATASET_TABLE);
            assertTableExists(copyRequest.getSourceSchema(), BASKET_TABLE);
            assertTableExists(targetSchema, DATASET_TABLE);
            assertTableExists(targetSchema, BASKET_TABLE);
        }

        return new TargetCopyPlan(targetSchema, tablePlans, basketAware);
    }

    private void deleteDataTables(TargetCopyPlan targetPlan) throws SQLException {
        for (TableCopyPlan tablePlan : targetPlan.getTablePlans()) {
            deleteTableRows(targetPlan.getTargetSchema(), tablePlan.getSpec().getTableName());
        }
    }

    private int replaceTableData(String sourceSchema, TableCopyPlan tablePlan) throws SQLException {
        deleteTableRows(tablePlan.getTargetSchema(), tablePlan.getSpec().getTableName());
        return copyTableData(sourceSchema, tablePlan);
    }

    private int copyTableData(String sourceSchema, TableCopyPlan tablePlan) throws SQLException {
        String insertSql = createInsertSqlStatement(
                sourceSchema,
                tablePlan.getTargetSchema(),
                tablePlan.getSpec().getTableName(),
                tablePlan.getTargetColumns(),
                tablePlan.getSpec().getRowFilter()
        );

        try (Statement statement = con.createStatement()) {
            return statement.executeUpdate(insertSql);
        }
    }

    private int copyWholeTable(String sourceSchema, String targetSchema, String tableName) throws SQLException {
        List<String> targetColumns = readColumns(con.getMetaData(), targetSchema, tableName);
        String insertSql = createInsertSqlStatement(sourceSchema, targetSchema, tableName, targetColumns, null);

        try (Statement statement = con.createStatement()) {
            return statement.executeUpdate(insertSql);
        }
    }

    private int deleteTableRows(String schemaName, String tableName) throws SQLException {
        String deleteSql = "DELETE FROM " + qualifiedName(con, schemaName, tableName);
        try (Statement statement = con.createStatement()) {
            return statement.executeUpdate(deleteSql);
        }
    }

    private void assertTableExists(String schemaName, String tableName) throws SQLException {
        if (readColumns(con.getMetaData(), schemaName, tableName).isEmpty()) {
            throw new IllegalArgumentException("required table <" + schemaName + "." + tableName + "> does not exist");
        }
    }

    private String createInsertSqlStatement(
            String sourceSchema,
            String targetSchema,
            String tableName,
            List<String> targetTableColumns,
            String rowFilter
    ) throws SQLException {
        String columns = joinQuotedIdentifiers(targetTableColumns);

        String insert = String.format(
                "INSERT INTO %s (%s)",
                qualifiedName(con, targetSchema, tableName),
                columns
        );

        String select = String.format(
                "SELECT %s FROM %s",
                columns,
                qualifiedName(con, sourceSchema, tableName)
        );

        if (rowFilter != null) {
            assertValidRowFilter(rowFilter);
            select += " WHERE " + rowFilter;
        }

        return insert + " " + select;
    }

    private void rollback(Exception failure) {
        if (con == null) {
            return;
        }
        try {
            con.rollback();
            log.debug("Rolled back schema copy transaction.");
        } catch (SQLException rollbackException) {
            failure.addSuppressed(rollbackException);
        }
    }

    private static String copiedTables(List<TargetCopyPlan> targetPlans) {
        return targetPlans.stream()
                .flatMap(targetPlan -> targetPlan.getTablePlans().stream()
                        .map(tablePlan -> tablePlan.getTargetSchema() + "." + tablePlan.getSpec().getTableName()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
    }

    private String joinQuotedIdentifiers(List<String> identifiers) throws SQLException {
        List<String> quotedIdentifiers = new ArrayList<>();
        for (String identifier : identifiers) {
            quotedIdentifiers.add(quoteIdentifier(con, identifier));
        }
        return String.join(", ", quotedIdentifiers);
    }

    private static void assertValidRowFilter(String rowFilter) {
        String rowFilterLower = rowFilter.toLowerCase();
        if(rowFilterLower.contains("where")){
            throw new IllegalArgumentException("Whereclause must not contain the keyword 'where'");
        }

        if(rowFilterLower.contains(";")){
            throw new IllegalArgumentException("Whereclause must not contain the statement delimiter ';'");
        }
    }

    private static String qualifiedName(Connection connection, String schemaName, String tableName) throws SQLException {
        return quoteIdentifier(connection, schemaName) + "." + quoteIdentifier(connection, tableName);
    }

    private static String quoteIdentifier(Connection connection, String identifier) throws SQLException {
        String quote = connection.getMetaData().getIdentifierQuoteString();
        if (quote == null || quote.trim().isEmpty()) {
            quote = "\"";
        }
        return quote + identifier.replace(quote, quote + quote) + quote;
    }

    private static boolean containsIdentifier(List<String> identifiers, String requestedIdentifier) {
        for (String identifier : identifiers) {
            if (matchesIdentifier(identifier, requestedIdentifier)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesIdentifier(String actualIdentifier, String requestedIdentifier) {
        return actualIdentifier != null
                && (actualIdentifier.equals(requestedIdentifier)
                        || actualIdentifier.equalsIgnoreCase(requestedIdentifier));
    }

    private static List<String> readColumns(DatabaseMetaData metadata, String schemaName, String tableName)
            throws SQLException {
        List<String> columns = new ArrayList<>();
        try (ResultSet rs = metadata.getColumns(null, schemaName, tableName, null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }

    private static final class TargetCopyPlan {
        private final String targetSchema;
        private final List<TableCopyPlan> tablePlans;
        private final boolean basketAware;

        private TargetCopyPlan(String targetSchema, List<TableCopyPlan> tablePlans, boolean basketAware) {
            this.targetSchema = targetSchema;
            this.tablePlans = tablePlans;
            this.basketAware = basketAware;
        }

        private String getTargetSchema() {
            return targetSchema;
        }

        private List<TableCopyPlan> getTablePlans() {
            return tablePlans;
        }

        private boolean isBasketAware() {
            return basketAware;
        }
    }

    private static final class TableCopyPlan {
        private final TableCopySpecification spec;
        private final String targetSchema;
        private final List<String> targetColumns;

        private TableCopyPlan(TableCopySpecification spec, String targetSchema, List<String> targetColumns) {
            this.spec = spec;
            this.targetSchema = targetSchema;
            this.targetColumns = targetColumns;
        }

        private TableCopySpecification getSpec() {
            return spec;
        }

        private String getTargetSchema() {
            return targetSchema;
        }

        private List<String> getTargetColumns() {
            return targetColumns;
        }
    }
}
