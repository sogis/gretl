package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class Gpkg2ShpTest {
    @Test
    public void export_Ok() throws Exception {
        String TEST_OUT = "src/integrationTest/jobs/Gpkg2Shp/out/";
        
        Files.deleteIfExists(Paths.get("src/integrationTest/jobs/Gpkg2Shp/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg")); 
        Files.list(Paths.get(TEST_OUT)).filter(p -> p.toString().contains("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20")).forEach((p) -> {
            try {
                Files.deleteIfExists(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Gpkg2Shp", gvs);
        
        //Check results
        {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(TEST_OUT, "nachfuehrngskrise_gemeinde.shp"));
            SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
            assertEquals(109, featuresSource.getFeatures().size()); 
            assertEquals("CH1903+_LV95", featuresSource.getSchema().getCoordinateReferenceSystem().getName().toString());
        }
        {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(TEST_OUT, "grundbuchkreise_grundbuchkreis.shp"));
            SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
            assertEquals(127, featuresSource.getFeatures().size()); 
            assertEquals("CH1903+_LV95", featuresSource.getSchema().getCoordinateReferenceSystem().getName().toString());
        }
    }
}
