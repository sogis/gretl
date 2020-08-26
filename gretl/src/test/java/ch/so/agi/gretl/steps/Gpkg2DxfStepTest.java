package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class Gpkg2DxfStepTest {

    public Gpkg2DxfStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;
    
    @Test
    public void export_Ok() throws Exception {
        String TEST_OUT = folder.newFolder().getAbsolutePath();
        File gpkgFile = new File("src/test/resources/data/gpkg2dxf/ch.so.agi_av_gb_administrative_einteilungen_2020-08-20.gpkg");

        Gpkg2DxfStep gpkg2dxfStep = new Gpkg2DxfStep();
        gpkg2dxfStep.execute(gpkgFile.getAbsolutePath(), TEST_OUT);
        
        //Check results
//        {
//            FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(TEST_OUT, "nachfuehrngskrise_gemeinde.shp"));
//            SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
//            assertEquals(109, featuresSource.getFeatures().size()); 
//            assertEquals("EPSG:CH1903+ / LV95", featuresSource.getSchema().getCoordinateReferenceSystem().getName().toString());
//        }
//        {
//            FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(TEST_OUT, "grundbuchkreise_grundbuchkreis.shp"));
//            SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
//            assertEquals(127, featuresSource.getFeatures().size()); 
//            assertEquals("EPSG:CH1903+ / LV95", featuresSource.getSchema().getCoordinateReferenceSystem().getName().toString());
//        }
    }
}
