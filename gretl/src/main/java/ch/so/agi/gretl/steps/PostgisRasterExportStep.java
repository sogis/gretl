package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.GretlException;
import ch.so.agi.gretl.util.SqlReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;

/**
 * Exports raster data from a PostgreSQL database into a raster file by
 * executing a sql query. The sql query must return only one record. Use e.g.
 * ST_Union().
 *
 * @author Stefan Ziegler
 */
public class PostgisRasterExportStep {
    private GretlLogger log;
    private String taskName;

    public PostgisRasterExportStep() {
        this(null);
    }
    
    public PostgisRasterExportStep(String taskName) {
        if (taskName == null) {
            this.taskName = PostgisRasterExportStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        log = LogEnvironment.getLogger(PostgisRasterExportStep.class);        
    }

    /**
     * Executes the sql query from a file in the specified database. It will use the
     * first found _bytea_ column as raster data column.
     *
     * @param database Database properties to generate database connection.
     * @param sqlFile  File which contain query.
     * @param outFile File where raster data is written to.
     * @param params   Map with key/value pairs. Will be used to replace parameters
     *                 in the sql query.
     * @throws Exception If file is missing, no connection to database, could not
     *                   read file or problems while executing sql query.
     */
    public void execute(Connector database, File sqlFile, File outFile, Map<String, String> params) throws Exception {
        Connection conn = null;

        log.lifecycle("Database string: " + database.toString());
        log.lifecycle("SQL file:" + sqlFile.getAbsolutePath());
        log.lifecycle("Output file: " + outFile.getAbsolutePath());

        try {
            conn = database.connect();

            SqlReader reader = new SqlReader();
            String sqlStmt = reader.readSqlStmt(sqlFile, params);

            log.debug("SQL statement:" + sqlStmt);

            PreparedStatement pstmt = conn.prepareStatement(sqlStmt);
            ResultSet rs = pstmt.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();

            // Resultat der Query kann viele Attribute beinhalten.
            // Es wird das erste bytea[]-Attribut exportiert.
            // TODO: Funktioniert ebenfalls mit AAIGrid (Ascii).
            // Vielleicht waere es schlauer, man nach einem 
            // Attributnamen suchen.
            for (int i = 1; i < numberOfColumns + 1; i++) {
                String columnTypeName = rsmd.getColumnTypeName(i);
                if (columnTypeName.equalsIgnoreCase("bytea")) {
                    rs.next();
                    InputStream is = rs.getBinaryStream(i);
                    FileUtils.copyInputStreamToFile(is, outFile);
                    break; // If we found a bytea column we can stop.
                }
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void execute(Connector database, File sqlFile, File outFile) throws Exception {
        execute(database, sqlFile, outFile, null);
    }

}
