package ch.so.agi.gretl.steps.publisher.in.db.schemacopy;

import ch.ehi.ili2db.base.DbNames;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsibility: copy selected source-schema tables into pre-existing target
 * schemas and tables by reading catalog metadata and executing DML only.
 */
public class CopyOperation {
    private static final GretlLogger log = LogEnvironment.getLogger(CopyOperation.class);
    private static final String DATASET_TABLE = DbNames.DATASETS_TAB;
    private static final String BASKET_TABLE = DbNames.BASKETS_TAB;
    private static final String T_BASKET_COLUMN = DbNames.T_BASKET_COL;

    private final Connection con;
    private final Request copyRequest;

    public CopyOperation(Connection connection, Request request){
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        this.con = connection;
        this.copyRequest = request;
    }

    public int execute() throws SQLException {
        int totalCopiedRows = 0;

        try {
            for (String targetSchema : copyRequest.getTargetSchemas()) {
                TargetCopyPlan targetPlan = createTargetCopyPlan(targetSchema);
                List<String> tableInserts = new ArrayList<>();

                try {
                    if (targetPlan.isBasketAware()) {
                        deleteDataTables(targetPlan);
                        int deletedBaskets = deleteTableRows(targetSchema, BASKET_TABLE);
                        int deletedDatasets = deleteTableRows(targetSchema, DATASET_TABLE);
                        int copiedDatasets = copyWholeTable(targetSchema, DATASET_TABLE);
                        int copiedBaskets = copyWholeTable(targetSchema, BASKET_TABLE);

                        log.info(String.format(
                                "%s metadata: deleted datasets:%d, baskets:%d; inserted datasets:%d, baskets:%d",
                                targetSchema, deletedDatasets, deletedBaskets, copiedDatasets, copiedBaskets));
                    }

                    for (TableCopyPlan tablePlan : targetPlan.getTablePlans()) {
                        int copiedRows = targetPlan.isBasketAware()
                                ? copyTableData(tablePlan)
                                : replaceTableData(tablePlan);
                        tableInserts.add(tablePlan.getSpec().getTableName() + ":" + copiedRows);
                        totalCopiedRows += copiedRows;
                    }
                } catch (Exception e) {
                    log.error(String.format("Failed to copy to schema %s", targetSchema), e);
                    throw e;
                }

                log.info(targetSchema + " inserts: " + String.join(", ", tableInserts));
            }
        } catch (SQLException se) {
            throw new RuntimeException(se);
        }

        return totalCopiedRows;
    }

    private TargetCopyPlan createTargetCopyPlan(String targetSchema) throws SQLException {
        if (matchesIdentifier(copyRequest.getSourceSchema(), targetSchema)) {
            throw new IllegalArgumentException("target schema <" + targetSchema
                    + "> must be different from source schema <" + copyRequest.getSourceSchema() + ">");
        }

        List<TableCopyPlan> tablePlans = new ArrayList<>();
        Boolean basketAware = null;

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

            boolean tableIsBasketAware = sourceHasBasketRef && targetHasBasketRef;
            if (basketAware == null) {
                basketAware = tableIsBasketAware;
            } else if (basketAware.booleanValue() != tableIsBasketAware) {
                throw new IllegalArgumentException("all copied tables for target schema <" + targetSchema
                        + "> must use the same basket-reference mode");
            }

            tablePlans.add(new TableCopyPlan(spec, targetSchema, targetColumns));
        }

        if (Boolean.TRUE.equals(basketAware)) {
            assertTableExists(copyRequest.getSourceSchema(), DATASET_TABLE);
            assertTableExists(copyRequest.getSourceSchema(), BASKET_TABLE);
            assertTableExists(targetSchema, DATASET_TABLE);
            assertTableExists(targetSchema, BASKET_TABLE);
        }

        return new TargetCopyPlan(targetSchema, tablePlans, Boolean.TRUE.equals(basketAware));
    }

    private void deleteDataTables(TargetCopyPlan targetPlan) throws SQLException {
        for (TableCopyPlan tablePlan : targetPlan.getTablePlans()) {
            deleteTableRows(targetPlan.getTargetSchema(), tablePlan.getSpec().getTableName());
        }
    }

    private int replaceTableData(TableCopyPlan tablePlan) throws SQLException {
        deleteTableRows(tablePlan.getTargetSchema(), tablePlan.getSpec().getTableName());
        return copyTableData(tablePlan);
    }

    private int copyTableData(TableCopyPlan tablePlan) throws SQLException {
        String insertSql = createInsertSqlStatement(
                tablePlan.getTargetSchema(),
                tablePlan.getSpec().getTableName(),
                tablePlan.getTargetColumns(),
                tablePlan.getSpec().getRowFilter()
        );

        try (Statement statement = con.createStatement()) {
            return statement.executeUpdate(insertSql);
        }
    }

    private int copyWholeTable(String targetSchema, String tableName) throws SQLException {
        List<String> targetColumns = readColumns(con.getMetaData(), targetSchema, tableName);
        String insertSql = createInsertSqlStatement(targetSchema, tableName, targetColumns, null);

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
                qualifiedName(con, copyRequest.getSourceSchema(), tableName)
        );

        if (rowFilter != null) {
            assertValidRowFilter(rowFilter);
            select += " WHERE " + rowFilter;
        }

        return insert + " " + select;
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
