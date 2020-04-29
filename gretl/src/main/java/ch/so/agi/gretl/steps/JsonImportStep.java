package ch.so.agi.gretl.steps;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

/**
 * The JsonImportStep class is used to import a JSON file into a text column of a database.
 */
public class JsonImportStep {
    
    private GretlLogger log;
    private String taskName;

    public JsonImportStep() {
        this(null);
    }
    
    public JsonImportStep(String taskName) {
        if (taskName == null) {
            taskName = JsonImportStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    /**
     * Imports the json file into a database text column. A top level json array will
     * be splitted into database records.
     *
     * @param targetDb    Database properties to generate database connection
     * @param jsonFile    File containing json string
     * @param qualifiedTableName The qualified table name
     * @param columnName  The name of the database text column where the json text will be stored
     * @param deleteAllRows Delete all records from the table before importing new data
     * @throws Exception if file is missing, no correct extension, no connection to
     *                   database, could not read file or problems while executing
     *                   sql-queries
     */
    public void execute(Connector targetDb, File jsonFile, String qualifiedTableName, String columnName, boolean deleteAllRows) throws Exception {
        log.lifecycle(String.format("Start JsonImportStep(Name: %s TargetDb: %s QualifiedTableName: %s ColumnName: %s)", taskName,
                targetDb, qualifiedTableName, columnName));

        Connection connection = targetDb.connect();
        connection.setAutoCommit(false);
  
        if (deleteAllRows) {
            String sqltruncate = "DELETE FROM " + qualifiedTableName;
            try {
                PreparedStatement stmt = connection.prepareStatement(sqltruncate);
                stmt.execute();
                log.info("DELETE executed");
            } catch (SQLException e) {
                log.error("DELETE FROM TABLE " + qualifiedTableName + " failed.", e);
                throw e;
            } 
        }
        
        String jsonString = new String(Files.readAllBytes(jsonFile.toPath()), Charset.forName("UTF-8"));
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootObj = mapper.readTree(jsonString);

        PreparedStatement stmt = connection.prepareStatement("INSERT INTO " + qualifiedTableName + " (" + columnName + ") VALUES (?);");
        
        int rowcount = 0;
        if (rootObj.isArray()) {
            Iterator<JsonNode> it = rootObj.iterator();
            while (it.hasNext()) {
                JsonNode node = it.next();
                stmt.setString(1, node.toString());
                rowcount = stmt.executeUpdate();
            }            
        } else {
            stmt.setString(1, rootObj.toString());
            rowcount = stmt.executeUpdate();
        }
        connection.commit();
        
        log.lifecycle("Inserted records: " + rowcount);
    }
}
