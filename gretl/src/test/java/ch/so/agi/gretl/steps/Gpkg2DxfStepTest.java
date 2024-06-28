package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class Gpkg2DxfStepTest {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void export_Ok() throws Exception {
        String TEST_OUT = folder.newFolder().getAbsolutePath();
        File gpkgFile = TestUtil.getResourceFile(TestUtil.ADMINISTRATIVE_EINSTELLUNGEN_GPKG_PATH);

        Gpkg2DxfStep gpkg2dxfStep = new Gpkg2DxfStep();
        gpkg2dxfStep.execute(gpkgFile.getAbsolutePath(), TEST_OUT);
        
        // Check results
        String contentNF = new String(Files.readAllBytes(Paths.get(TEST_OUT, "nachfuehrngskrise_gemeinde.dxf")));
        assertTrue(contentNF.contains("LerchWeberAG"));
        assertTrue(contentNF.contains("2638171.578"));
        
        String contentGB = new String(Files.readAllBytes(Paths.get(TEST_OUT, "grundbuchkreise_grundbuchkreis.dxf")));
        assertTrue(contentGB.contains("2619682.201"));
    }
}
