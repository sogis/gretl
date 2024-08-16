package ch.so.agi.gretl.jobs;


import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlExecutorTaskDuckDbTest {

    // DB-File erstellen.
    // Tabelle erstellen und Werte inserten
    // Parquet-Datei importieren
    // GPKG importieren
//    @Test
    public void multipleStuff_Ok() throws Exception {
        String jobDir = "src/integrationTest/jobs/SqlExecutorTaskDuckDb";
        String dbName = "foo.duckdb";
        
        // Prepare
        if (Files.exists(Paths.get(jobDir, dbName))) {
            Files.delete(Paths.get(jobDir, dbName));
        }
        
        // Run job
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob(jobDir, gvs);
        
        // Check result
        // Vor allem die Parquet- und GPKG-Imports werden bereits im SQL geprüft: 
        // Es wird ein SELECT auf die neue Tabelle gemacht.
        assertTrue(Files.exists(Paths.get(jobDir, dbName)));
    }
}
