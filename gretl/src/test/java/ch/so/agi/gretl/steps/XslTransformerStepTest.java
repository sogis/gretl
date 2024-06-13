package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import net.sf.saxon.s9api.SaxonApiException;

public class XslTransformerStepTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void transformFile_Resource_Ok() throws Exception {
        File outDirectory = folder.newFolder("transformFile_Ok");
        File sourceFile = TestUtil.getResourceFile("data/xsltransformer/MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xml");

        // Transform File
        XslTransformerStep xslTransformerStep = new XslTransformerStep();
        xslTransformerStep.execute("eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl", sourceFile, outDirectory);
        
        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get(outDirectory.getAbsolutePath(), "MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf"));
        String fileContent = new String (bytes);
        
        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
        assertTrue(fileContent.contains("<Grundstuecksnummer>1505</Grundstuecksnummer>"));
    }
    
    @Test
    public void transformFile_File_Ok() throws Exception {
        File xslFile = new File("src/main/resources/xslt/eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl");
        File outDirectory = folder.newFolder("transformFile_Ok");
        File sourceFile = TestUtil.getResourceFile("data/xsltransformer/MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xml");
        
        // Transform File
        XslTransformerStep xslTransformerStep = new XslTransformerStep();
        xslTransformerStep.execute(xslFile, sourceFile, outDirectory);

        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get(outDirectory.getAbsolutePath(), "MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf"));
        String fileContent = new String (bytes);
        
        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
        assertTrue(fileContent.contains("<Grundstuecksnummer>1505</Grundstuecksnummer>"));
    }
    
}
