package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import net.sf.saxon.s9api.SaxonApiException;

public class XslTransformerStepTest {

    public XslTransformerStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;
    
    @Test
    public void transformFile_Ok() throws IOException, SaxonApiException {
//        File outDirectory = folder.newFolder("transformFile_Ok");
        File outDirectory = new File("/Users/stefan/tmp/");
        File sourceFile = new File("src/test/resources/data/xsltransformer/MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xml");

        // Transform File
        XslTransformerStep xslTransformerStep = new XslTransformerStep();
        xslTransformerStep.execute("eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl", sourceFile, outDirectory);
        
        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get(outDirectory.getAbsolutePath(), "MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf"));
        String fileContent = new String (bytes);
        
        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
        assertTrue(fileContent.contains("<Grundstuecksnummer>1505</Grundstuecksnummer>"));
    }
}
