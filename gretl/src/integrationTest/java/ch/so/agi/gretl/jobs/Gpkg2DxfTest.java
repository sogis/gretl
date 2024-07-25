package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gpkg2DxfTest {

    @Test
    public void export_Ok() throws Exception {
        String TEST_OUT = "src/integrationTest/jobs/Gpkg2Dxf/out/";
        
        Files.deleteIfExists(Paths.get("src/integrationTest/jobs/Gpkg2Dxf/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg")); 
        Files.list(Paths.get(TEST_OUT)).filter(p -> p.toString().contains("dxf")).forEach((p) -> {
            try {
                Files.deleteIfExists(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Gpkg2Dxf", gvs);
        
        //Check results
        String contentNF = new String (Files.readAllBytes(Paths.get(TEST_OUT, "nachfuehrngskrise_gemeinde.dxf")));
        assertTrue(contentNF.contains("LerchWeberAG"));
        assertTrue(contentNF.contains("2638171.578"));
        
        String contentGB = new String (Files.readAllBytes(Paths.get(TEST_OUT, "grundbuchkreise_grundbuchkreis.dxf")));
        assertTrue(contentGB.contains("2619682.201"));
    }
}
