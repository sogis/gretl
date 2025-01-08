package ch.so.agi.gretl.steps;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.TransferSet;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Db2DbStep Class is used as a step for transfer of tabulated data from one
 * to another database. It needs a sourceDb (Connector), a targetDb (Connector)
 * and a list of transferSet, containing 1. a boolean paramterer concerning the
 * emptying of the Targettable, 2. a SQL-file containing a SELECT-statement and
 * 3. a qualified target schema and table name (schema.table).
 */
public class Db2DbStep {
    public static final String PREFIX = "ch.so.agi.gretl.steps.Db2DbStep";
    public static final String SETTING_BATCH_SIZE = PREFIX + ".batchSize";
    public static final String SETTING_FETCH_SIZE = PREFIX + ".fetchSize";
    private static GretlLogger log;
    private String taskName;
    private int batchSize = 5000;
    private int fetchSize = 5000;

    public Db2DbStep() {
        this(null);
    }

    public Db2DbStep(String taskName) {
        if (taskName == null) {
            this.taskName = Db2DbStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    /**
     * Main method. Calls for each transferSet methode processTransferSet
     * 
     * @param sourceDb     The Source Databaseconnection
     * @param targetDb     The Target Databaseconnection
     * @param transferSets A list of Transfersets
     * @throws Exception
     */
    public void processAllTransferSets(Connector sourceDb, Connector targetDb, List<TransferSet> transferSets)
            throws Exception {
        processAllTransferSets(sourceDb, targetDb, transferSets, new Settings(),
                new java.util.HashMap<String, String>());
    }

    public void processAllTransferSets(Connector sourceDb, Connector targetDb, List<TransferSet> transferSets,
            Settings settings, Map<String, String> params) throws Exception {
        assertValidTransferSets(transferSets);

        String batchSizeStr = settings.getValue(SETTING_BATCH_SIZE);
        if (batchSizeStr != null) {
            try {
                int newBatchSize = Integer.parseInt(batchSizeStr);
                if (newBatchSize > 0) {
                    batchSize = newBatchSize;
                }
            } catch (NumberFormatException e) {

            }
        }

        String fetchSizeStr = settings.getValue(SETTING_FETCH_SIZE);
        if (fetchSizeStr != null) {
            try {
                int newFetchSize = Integer.parseInt(fetchSizeStr);
                if (newFetchSize >= 0) { // fetchSize 0 -> fetch all at once
                    fetchSize = newFetchSize;
                }
            } catch (NumberFormatException e) {

            }
        }

        log.lifecycle(String.format("Start Db2DbStep(Name: %s SourceDb: %s TargetDb: %s Transfers: %s)", taskName,
                sourceDb, targetDb, transferSets));

        ArrayList<String> rowCountStrings = new ArrayList<String>();

        try {
            Connection sourceDbConnection = sourceDb.connect();
            Connection targetDbConnection = targetDb.connect();
            for (TransferSet transferSet : transferSets) {
                // Check if file is readable
                if (!transferSet.getInputSqlFile().canRead()) {
                    throw new IllegalArgumentException(
                            "File" + transferSet.getInputSqlFile().getName() + " not found or not readable");
                }
                // Check if File is UTF8
                FileStylingDefinition.checkForUtf8(transferSet.getInputSqlFile());
                // Check if File contains no BOM. If File is Empty, there will be a
                // NullPointerException catched away.
                try {
                    FileStylingDefinition.checkForBOMInFile(transferSet.getInputSqlFile());
                } catch (NullPointerException e) {
                }
                ;

                int rowCount = processTransferSet(sourceDbConnection, targetDbConnection, transferSet, params);
                rowCountStrings.add(Integer.toString(rowCount));
            }
            sourceDbConnection.commit();
            targetDbConnection.commit();
            sourceDb.close();
            targetDb.close();

            String rowCountList = String.join(",", rowCountStrings);
            log.lifecycle(String.format(
                    "Db2DbStep %s: Transfered all Transfersets. Number of Transfersets: %s, transfered rows: [%s]",
                    taskName, rowCountStrings.size(), rowCountList));
        } catch (Exception e) {
            log.error("Exception while executing processAllTransferSets()", e);
            throw e;
        } finally {
            if (!sourceDb.isClosed()) {
                try {
                    sourceDb.connect().rollback();
                } catch (SQLException e) {
                    log.error("failed to rollback", e);
                }finally {
                    try {
                        sourceDb.close();
                    } catch (SQLException e) {
                        log.error("failed to close", e);
                    }
                }
            }
            if (!targetDb.isClosed()) {
                try {
                    targetDb.connect().rollback();
                } catch (SQLException e) {
                    log.error("failed to rollback", e);
                }finally {
                    try {
                        targetDb.close();
                    } catch (SQLException e) {
                        log.error("failed to close", e);
                    }
                }
            }
        }
    }

    /**
     * Controls the execution of a TransferSet
     * 
     * @param srcCon      SourceDB Connection
     * @param targetCon   TargetDB Connection
     * @param transferSet Transferset
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws EmptyFileException
     * @throws NotAllowedSqlExpressionException
     * @returns The number of processed rows
     */
    private int processTransferSet(Connection srcCon, Connection targetCon, TransferSet transferSet,
            Map<String, String> params)
            throws SQLException, IOException, EmptyFileException, NotAllowedSqlExpressionException {
        if (transferSet.deleteAllRows()) {
            deleteDestTableContents(targetCon, transferSet.getOutputQualifiedTableName());
        }
//        String selectStatement = extractSingleStatement(transferSet.getInputSqlFile(), params);
        List<String> selectStatements = extractStatements(transferSet.getInputSqlFile(), params);
        String selectStatement; 
        // Sql file can contain two statements. If so, the first on is a "set search_path" statement.
        if (selectStatements.size() > 1) {
            try(Statement stmt = srcCon.createStatement()) {
                stmt.execute(selectStatements.get(0));
            }
            selectStatement = selectStatements.get(1);
        } else {
            selectStatement = selectStatements.get(0);
        }
        log.debug("SQL statement: " + selectStatement);
        ResultSet rs = createResultSet(srcCon, selectStatement);
        PreparedStatement insertRowStatement = createInsertRowStatement(srcCon, targetCon, rs, transferSet);

        int columncount = rs.getMetaData().getColumnCount();
        int k = 0;
        while (rs.next()) {
            transferRow(rs, insertRowStatement, columncount);
            if (k % batchSize == 0) {
                log.debug("Batching next " + batchSize + " records. (Total: " + String.valueOf(k) + ")");
                insertRowStatement.executeBatch();
                insertRowStatement.clearBatch();
            }
            k += 1;
        }

        insertRowStatement.executeBatch();
        log.debug("Transfer " + k + " rows and " + columncount + " columns to table "
                + transferSet.getOutputQualifiedTableName());

        return k;
    }

    /**
     * Copies a row of the source ResultSet to the target table
     * 
     * @param rs                 ResultSet
     * @param insertRowStatement The prepared Insertstatement
     * @param columncount        How many columns
     * @throws SQLException
     */
    private void transferRow(ResultSet rs, PreparedStatement insertRowStatement, int columncount) throws SQLException {
        // assign column wise values
        for (int j = 1; j <= columncount; j++) {
            insertRowStatement.setObject(j, rs.getObject(j));
        }
        // insertRowStatement.execute();
        insertRowStatement.addBatch();
    }

    /**
     * Delete the content of the target table
     * 
     * @param targetCon     TargedDB Connection
     * @param destTableName Qualified Target Table Name (Schema.Table)
     * @throws SQLException
     */
    private void deleteDestTableContents(Connection targetCon, String destTableName) throws SQLException {
        String sqltruncate = "DELETE FROM " + destTableName;
        try {
            PreparedStatement stmt = targetCon.prepareStatement(sqltruncate);
            stmt.execute();
            log.info("DELETE executed");
        } catch (SQLException e1) {
            log.error("DELETE FROM TABLE " + destTableName + " failed.", e1);
            throw e1;
        }
    }

    /**
     * Creates the ResultSet with the SelectStatement from the InputFile
     * 
     * @param srcCon             SourceDB Connection
     * @param sqlSelectStatement The SQL Statement extract from the input-file
     * @return rs Resultset
     * @throws SQLException
     */
    private ResultSet createResultSet(Connection srcCon, String sqlSelectStatement) throws SQLException {
        Statement stmt = srcCon.createStatement();
        stmt.setFetchSize(fetchSize);
        ResultSet rs = stmt.executeQuery(sqlSelectStatement);

        return rs;
    }

    /**
     * Prepares the insert Statement. Leaves the Values as ?
     * 
     * @param srcCon    SourceDB Connection
     * @param targetCon DargetDB Connection
     * @param rs        ResultSet
     * @param tSet      TransferSet
     * @return The InsertRowStatement
     * @throws SQLException
     */
    private PreparedStatement createInsertRowStatement(Connection srcCon, Connection targetCon, ResultSet rs,
            TransferSet tSet) {
        ResultSetMetaData meta = null;
        PreparedStatement insertRowStatement = null;

        try {
            meta = rs.getMetaData();

            String insertColNames = buildInsertColumnNames(meta, targetCon, tSet.getOutputQualifiedTableName());
            String valuesList = buildValuesList(meta, tSet);

            String sql = "INSERT INTO " + tSet.getOutputQualifiedTableName() + " (" + insertColNames + ") VALUES ("
                    + valuesList + ")";
            insertRowStatement = targetCon.prepareStatement(sql);

            log.info(String.format(taskName + ": Sql insert statement: [%s]", sql));

        } catch (SQLException g) {
            throw new GretlException(g);
        }

        return insertRowStatement;
    }

    private static String buildValuesList(ResultSetMetaData meta, TransferSet tSet) {
        StringBuffer valuesList = new StringBuffer();
        try {
            for (int j = 1; j <= meta.getColumnCount(); j++) {
                if (j > 1) {
                    valuesList.append(", ");
                }

                String colName = meta.getColumnName(j);

                if (tSet.isGeoColumn(colName)) {
                    String func = tSet.wrapWithGeoTransformFunction(colName, "?");
                    valuesList.append(func);
                } else {
                    valuesList.append("?");
                }
            }
        } catch (SQLException se) {
            throw new GretlException(se);
        }
        return valuesList.toString();
    }

    private static String buildInsertColumnNames(ResultSetMetaData sourceMeta, Connection targetCon,
            String targetTableName) {
        StringBuffer columnNames = new StringBuffer();
        AttributeNameMap colMap = AttributeNameMap.createAttributeNameMap(targetCon, targetTableName);
        try {
            for (int j = 1; j <= sourceMeta.getColumnCount(); j++) {
                if (j > 1) {
                    columnNames.append(", ");
                }

                String srcColName = sourceMeta.getColumnName(j);
                String targetColName = colMap.getAttributeName(srcColName);
                columnNames.append(targetColName);

            }
        } catch (SQLException se) {
            throw new GretlException(se);
        }
        return columnNames.toString();
    }

    /**
     * Extracts the statements out of the sql file and checks if it fits the
     * conditions: the file contains either a single statement or max two
     * statements, where the first is a "set search_path to" statement.
     * There are no checks to ensure that the "select" statement is
     * really a "select" statement.
     *
     * @param targetFile
     * @param params
     * @return
     * @throws IOException
     */
    private List<String> extractStatements(File targetFile, Map<String, String> params) throws IOException {
        SqlReader reader = new SqlReader();
        String firstStmt = reader.readSqlStmt(targetFile, params);
        if (firstStmt == null) {
            log.info("Empty file. No statement to execute!");
            throw new EmptyFileException("Empty file: " + targetFile.getName());
        } 
        
        String secondStmt = reader.nextSqlStmt();
        if (secondStmt != null) {
            if (!firstStmt.toLowerCase().trim().startsWith("set search_path to")) {
                log.info("First statement must be a set search_path statement.");
                throw new IllegalArgumentException("First statement must be a set search_path statement.");
            }
        } 
        
        String thirdStmt = reader.nextSqlStmt();
        if (thirdStmt != null) {
            log.info("There are more then 2 statement in the file!");
            throw new IOException("There are more then 2 statement in the file");
        }

        if (secondStmt != null) {
            return List.of(firstStmt,secondStmt);
        } else {
            return List.of(firstStmt);
        }
    } 
    
    /**
     * Extracts a single statement out of the sql file and checks if it fits the
     * conditions.
     * 
     * @param targetFile
     * @return A select statement as String
     * @throws IOException
     * @throws EmptyFileException
     */
    private String extractSingleStatement(File targetFile, Map<String, String> params) throws IOException {

        SqlReader reader = new SqlReader();
        String firstStmt = reader.readSqlStmt(targetFile, params);
        if (firstStmt == null) {
            log.info("Empty file. No statement to execute!");
            throw new EmptyFileException("Empty file: " + targetFile.getName());
        }
        String secondStmt = reader.nextSqlStmt();
        if (secondStmt != null) {
            log.info("There is more then 1 statement in the file!");
            throw new IOException("There is more then 1 statement in the file");
        }
        reader.close();

        return firstStmt;
    }

    /**
     * Checks if the Transferset List is not Empty.
     * 
     * @param transferSets
     * @throws EmptyListException
     */
    private void assertValidTransferSets(List<TransferSet> transferSets) throws EmptyListException {
        if (transferSets.size() == 0) {
            throw new EmptyListException();
        }

        for (TransferSet ts : transferSets) {
            if (!ts.getInputSqlFile().canRead()) {
                throw new GretlException("Can not read input sql file at path: " + ts.getInputSqlFile().getPath());
            }
        }
    }

}