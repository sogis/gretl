package ch.so.agi.gretl.jobs;


import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlExecutorTaskDuckDbTest {

    // DB-File erstellen.
    // Tabelle erstellen und Werte inserten
    // Parquet-Datei importieren
    // GPKG importieren
    @Test
    public void multipleStuff_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/SqlExecutorTaskDuckDb");
        String dbName = "foo.duckdb";
        Path pathToDatabase = Paths.get(projectDirectory.getPath(), dbName);
        
        // Prepare
        if (Files.exists(pathToDatabase)) {
            Files.delete(pathToDatabase);
        }

        IntegrationTestUtil.executeTestRunner(projectDirectory, "importParquet");
        
        // Check result
        // Vor allem die Parquet- und GPKG-Imports werden bereits im SQL geprüft: 
        // Es wird ein SELECT auf die neue Tabelle gemacht.
        assertTrue(Files.exists(pathToDatabase));
    }
}
