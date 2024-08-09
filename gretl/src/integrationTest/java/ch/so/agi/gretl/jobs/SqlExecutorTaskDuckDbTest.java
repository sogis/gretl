package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import ch.so.agi.gretl.util.IntegrationTestUtil;

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
        
        IntegrationTestUtil.executeTestRunner(projectDirectory, "importParquet").build();

        // Check result
        // vor allem die Parquet- und GPKG-Imports werden bereits im SQL geprüft:
        // Es wird ein SELECT auf die neue Tabelle gemacht.
        assertTrue(Files.exists(pathToDatabase));
    }
}
