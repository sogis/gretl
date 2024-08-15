package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gpkg2DxfStepTest {

    @TempDir
    public Path folder;
    
    @Test
    public void export_Ok() throws Exception {
        File gpkgFile = TestUtil.getResourceFile(TestUtil.ADMINISTRATIVE_EINSTELLUNGEN_GPKG_PATH);

        Gpkg2DxfStep gpkg2dxfStep = new Gpkg2DxfStep();
        gpkg2dxfStep.execute(gpkgFile.getAbsolutePath(), folder.toAbsolutePath().toString());
        
        // Check results
        String contentNF = new String(Files.readAllBytes(Paths.get(folder.toAbsolutePath().toString(), "nachfuehrngskrise_gemeinde.dxf")));
        assertTrue(contentNF.contains("LerchWeberAG"));
        assertTrue(contentNF.contains("2638171.578"));
        
        String contentGB = new String(Files.readAllBytes(Paths.get(folder.toAbsolutePath().toString(), "grundbuchkreise_grundbuchkreis.dxf")));
        assertTrue(contentGB.contains("2619682.201"));
    }
}
