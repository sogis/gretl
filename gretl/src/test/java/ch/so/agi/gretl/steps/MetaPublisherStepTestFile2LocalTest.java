package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class MetaPublisherStepTestFile2LocalTest {
    protected GretlLogger log;

    public static final String PATH_ELE_AKTUELL = "aktuell";
    public static final String PATH_ELE_META = "meta";
    public static final String PATH_ELE_CONFIG = "config";

    public MetaPublisherStepTestFile2LocalTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }
        
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void publish_simple_meta_Ok() throws Exception {
        // Prepare
        //Path target = Paths.get("/Users/stefan/tmp/metapublisher/out/");
        File outDirectory = folder.newFolder("transformFile_Ok");
        Path target = outDirectory.toPath();
        String themePublication = "ch.so.afu.abbaustellen";
        
        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_simple_meta_Ok");
        //metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/thema-config/ch.so.awjf.seltene_baumarten"), "ch.so.awjf.seltene_baumarten", target);
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/thema-config/ch.so.afu.abbaustellen"), themePublication, target);
        
        // Check results
        File htmlFile = target.resolve(themePublication).resolve(PATH_ELE_AKTUELL).resolve("meta-"+themePublication+".html").toFile();
        assertTrue(htmlFile.exists());
        
        byte[] bytes = Files.readAllBytes(htmlFile.toPath());
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
        assertTrue(fileContent.contains("<div id=\"title\">Abbaustellen</div>"));

        File xtfFile = target.resolve(PATH_ELE_CONFIG).resolve("meta-"+themePublication+".xtf").toFile();
        assertTrue(xtfFile.exists());
    }
}
