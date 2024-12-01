package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Gpkg2ShpTest {

    private final GretlLogger log;

    public Gpkg2ShpTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void export_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Gpkg2Shp");
        String testOutDir = projectDirectory + "/out/";
        String gpkgFilePath = projectDirectory + "/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg";
        String[] shpFiles = {
                "nachfuehrngskrise_gemeinde.shp",
                "grundbuchkreise_grundbuchkreis.shp"
        };

        // Clean up existing files
        Files.deleteIfExists(Paths.get(gpkgFilePath));
        try (Stream<Path> paths = Files.list(Paths.get(testOutDir))) {
            paths.filter(p -> p.toString().contains("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20"))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    });
        }

        IntegrationTestUtil.executeTestRunner(projectDirectory, "gpkg2shp");

        // Check results
        {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(testOutDir, "nachfuehrngskrise_gemeinde.shp"));
            SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
            assertEquals(109, featuresSource.getFeatures().size());
            assertEquals("CH1903+_LV95", featuresSource.getSchema().getCoordinateReferenceSystem().getName().toString());
        }
        {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(testOutDir, "grundbuchkreise_grundbuchkreis.shp"));
            SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
            assertEquals(127, featuresSource.getFeatures().size());
            assertEquals("CH1903+_LV95", featuresSource.getSchema().getCoordinateReferenceSystem().getName().toString());
        }
    }
}
