package ch.so.agi.gretl.jobs;


import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class XslTransformerTest {

    @Test
    public void transformFile_Resource_Ok() throws Exception {
        // Transform xml file
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/XslTransformerResource", gvs);
        
        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get("src/integrationTest/jobs/XslTransformerResource", "MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf"));
        String fileContent = new String (bytes);

        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
        assertTrue(fileContent.contains("<Grundstuecksnummer>1505</Grundstuecksnummer>"));
        assertTrue(fileContent.contains("<Gebaeudebezeichnung>Reine Wohngebäude (Wohnnutzung ausschliesslich)</Gebaeudebezeichnung>"));
    }
    
    @Test
    public void transformFile_File_Ok() throws Exception {
        // Transform xml file
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/XslTransformerFile", gvs);
        
        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get("src/integrationTest/jobs/XslTransformerFile", "MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf"));
        String fileContent = new String (bytes);

        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
        assertTrue(fileContent.contains("<Grundstuecksnummer>1505</Grundstuecksnummer>"));
        assertTrue(fileContent.contains("<Gebaeudebezeichnung>Reine Wohngebäude (Wohnnutzung ausschliesslich)</Gebaeudebezeichnung>"));
    }

    @Test
    public void transformFileSet_Ok() throws Exception {
        // Transform xml file
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/XslTransformerFileSet", gvs);

        // Check result
        assertTrue(new File("src/integrationTest/jobs/XslTransformerFileSet/MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf").exists());
        assertTrue(new File("src/integrationTest/jobs/XslTransformerFileSet/MeldungAnGeometer_mehrere_gebaeude_mehrere_grundstuecke.xtf").exists());
    }
}
