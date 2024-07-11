package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class XslTransformerStepTest {

    @TempDir
    public Path folder;
    
    @Test
    public void transformFile_Resource_Ok() throws Exception {
        Path outDirectory = TestUtil.createTempDir(folder, "transformFile_Ok");
        File sourceFile = TestUtil.getResourceFile("data/xsltransformer/MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xml");

        // Transform File
        XslTransformerStep xslTransformerStep = new XslTransformerStep();
        xslTransformerStep.execute("eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl", sourceFile, outDirectory.toFile());
        
        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get(outDirectory.toAbsolutePath().toString(), "MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf"));
        String fileContent = new String (bytes);
        
        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
        assertTrue(fileContent.contains("<Grundstuecksnummer>1505</Grundstuecksnummer>"));
    }
    
    @Test
    public void transformFile_File_Ok() throws Exception {
        File xslFile = new File("src/main/resources/xslt/eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl");
        Path outDirectory = TestUtil.createTempDir(folder, "transformFile_Ok");
        File sourceFile = TestUtil.getResourceFile("data/xsltransformer/MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xml");
        
        // Transform File
        XslTransformerStep xslTransformerStep = new XslTransformerStep();
        xslTransformerStep.execute(xslFile, sourceFile, outDirectory.toFile());

        // Check result
        byte[] bytes = Files.readAllBytes(Paths.get(outDirectory.toAbsolutePath().toString(), "MeldungAnGeometer_G-0098981_20230214_104054_Koordinaten.xtf"));
        String fileContent = new String (bytes);
        
        assertTrue(fileContent.contains("<SO_AGI_SGV_Meldungen_20221109.Meldungen BID=\"SO_AGI_SGV_Meldungen_20221109.Meldungen\">"));
        assertTrue(fileContent.contains("<Grundstuecksnummer>1505</Grundstuecksnummer>"));
    }
    
}
