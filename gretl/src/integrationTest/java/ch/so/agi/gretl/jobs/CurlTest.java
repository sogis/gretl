package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class CurlTest {
    
    @Test
    public void dummy() throws Exception {
        // Transform xml file
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Curl", gvs);
        
        // Check result
//        byte[] bytes = Files.readAllBytes(Paths.get("src/integrationTest/jobs/XslTransformerResource", "MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf"));
//        String fileContent = new String (bytes);
//
//        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
//        assertTrue(fileContent.contains("<Grundstuecksnummer>1505</Grundstuecksnummer>"));
//        assertTrue(fileContent.contains("<Gebaeudebezeichnung>Reine Wohngebäude (Wohnnutzung ausschliesslich)</Gebaeudebezeichnung>"));
    }

}
