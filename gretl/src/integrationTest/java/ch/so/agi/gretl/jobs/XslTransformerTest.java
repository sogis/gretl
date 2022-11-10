package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class XslTransformerTest {

    @Test
    public void transformFile_Ok() throws Exception {
        // Transform xml file
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/XslTransformer", gvs);
        
        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get("src/integrationTest/jobs/XslTransformer", "MeldungAnGeometer_G-0111102_20221103_145001.xtf"));
        String fileContent = new String (bytes);

        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
        assertTrue(fileContent.contains("<Grundstuecksnummer>2979</Grundstuecksnummer>"));        
    }
}
