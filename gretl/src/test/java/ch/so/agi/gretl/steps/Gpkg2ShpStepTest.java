package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Date;
import java.util.List;

import ch.so.agi.gretl.testutil.TestUtil;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opengis.feature.type.AttributeDescriptor;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class Gpkg2ShpStepTest {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test 
    public void export_no_Geometry_Ok() throws Exception {
        String TEST_OUT = folder.newFolder().getAbsolutePath();
        File gpkgFile = TestUtil.getResourceFile(TestUtil.AGGLOPROGRAMME_GPKG_PATH);
        
        Gpkg2ShpStep gpkg2shpStep = new Gpkg2ShpStep();
        gpkg2shpStep.execute(gpkgFile.getAbsolutePath(), TEST_OUT);

        // Check results
        {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(TEST_OUT, "massnahmen.shp"));
            SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
            assertEquals(451, featuresSource.getFeatures().size()); 

            assertEquals(35, featuresSource.getSchema().getAttributeCount());
        }
    }
    
    @Test
    public void export_Ok() throws Exception {
        String TEST_OUT = folder.newFolder().getAbsolutePath();
        File gpkgFile = TestUtil.getResourceFile("data/gpkg2shp/ch.so.agi_av_gb_administrative_einteilungen_2020-08-20.gpkg");
        Gpkg2ShpStep gpkg2shpStep = new Gpkg2ShpStep();
        gpkg2shpStep.execute(gpkgFile.getAbsolutePath(), TEST_OUT);
        
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
            
            assertEquals(19, featuresSource.getSchema().getAttributeCount());

            AttributeDescriptor attrDesc = featuresSource.getSchema().getDescriptor("plz");
            assertEquals("java.lang.Integer", attrDesc.getType().getBinding().getName());
        }
    }
    
    @Test
    public void export_more_attribute_types_Ok() throws Exception {
        String TEST_OUT = folder.newFolder().getAbsolutePath();
        //String TEST_OUT = "/Users/stefan/tmp/shp/";

        File gpkgFile = TestUtil.getResourceFile("data/gpkg2shp/ch.so.afu.abbaustellen.gpkg");
        Gpkg2ShpStep gpkg2shpStep = new Gpkg2ShpStep();
        gpkg2shpStep.execute(gpkgFile.getAbsolutePath(), TEST_OUT);

        // Check results
        {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(TEST_OUT, "abbaustelle.shp"));
            SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
            assertEquals(98, featuresSource.getFeatures().size());
            assertEquals("CH1903+_LV95",
                    featuresSource.getSchema().getCoordinateReferenceSystem().getName().toString());

            // 14 Sachattribute plus die Geometrie
            assertEquals(15, featuresSource.getSchema().getAttributeCount());

            AttributeDescriptor attrDesc = featuresSource.getSchema().getDescriptor("rrb_datum");
            assertEquals("java.util.Date", attrDesc.getType().getBinding().getName());
        }
    }
}
