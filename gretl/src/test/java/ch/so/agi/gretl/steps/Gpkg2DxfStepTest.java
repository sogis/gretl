package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        
        // Check results
        String contentNF = new String (Files.readAllBytes(Paths.get(TEST_OUT, "nachfuehrngskrise_gemeinde.dxf")));
        assertTrue(contentNF.contains("LerchWeberAG"));
        assertTrue(contentNF.contains("2638171.578"));
        
        String contentGB = new String (Files.readAllBytes(Paths.get(TEST_OUT, "grundbuchkreise_grundbuchkreis.dxf")));
        assertTrue(contentGB.contains("2619682.201"));
    }
}
