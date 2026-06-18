package ch.so.agi.gretl.steps.publisher.in.db.schemacopy;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Responsibility: copy selected source-schema tables into pre-existing target
 * schemas and tables by reading catalog metadata and executing DML only.
 */
public class CopyOperation {
    private static final GretlLogger log = LogEnvironment.getLogger(CopyOperation.class);

    private Connection con;
    private Request copyRequest;

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

        try{
            DatabaseMetaData metadata = con.getMetaData();

            for (String targetSchema : copyRequest.getTargetSchemas()){
                List<String> tableInserts = new ArrayList<>();
                for (TableCopySpecification spec : copyRequest.getSourceTables()) {
                    try{
                        int numInserts = copyTable(spec, targetSchema);
                        tableInserts.add(spec.getTableName() + ":" + numInserts);
                        totalCopiedRows += numInserts;
                    }
                    catch (Exception e) {
                        log.error(
                                String.format("Failed to copy to table %s.%s", targetSchema, spec.getTableName()),
                                e
                        );
                        throw e;
                    }
                }
                log.info(targetSchema + " inserts: " + String.join(", ", tableInserts));
            }
        }
        catch (SQLException se){
            throw new RuntimeException(se);
        }

        return totalCopiedRows;
    }

    private int copyTable(TableCopySpecification spec, String targetSchema) throws SQLException {
        int rowCopyCount = 0;

        if (matchesIdentifier(copyRequest.getSourceSchema(), targetSchema)) {
            throw new IllegalArgumentException("target schema <" + targetSchema
                    + "> must be different from source schema <" + copyRequest.getSourceSchema() + ">");
        }

        String deleteSql = "DELETE FROM " + qualifiedName(con, targetSchema, spec.getTableName());
        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(deleteSql);
        }

        List<String> targetColumns = readColumns(con.getMetaData(), targetSchema, spec.getTableName());
        String insertSql = createInsertSqlStatement(targetSchema, spec, targetColumns);

        try (Statement statement = con.createStatement()) {
            rowCopyCount = statement.executeUpdate(insertSql);
        }

        return rowCopyCount;
    }

    private String createInsertSqlStatement(String targetSchema, TableCopySpecification spec, List<String> targetTableColumns){
        String columns = String.join(", ", targetTableColumns);

        String insert = String.format(
                "INSERT INTO %s (%s)",
                targetSchema + "." + spec.getTableName(),
                columns
        );

        String select = String.format(
                "SELECT %s FROM %s",
                columns,
                copyRequest.getSourceSchema() + "." + spec.getTableName()
        );

        if (spec.getRowFilter() != null) {
            assertValidRowFilter(spec.getRowFilter());
            select += " WHERE " + spec.getRowFilter();
        }

        return insert + " " + select;
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
}