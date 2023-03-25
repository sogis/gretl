package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

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
    public static final String GEOCAT_FTP_DIR = "int"; // TODO / FIXME

    public MetaPublisherStepTestFile2LocalTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }
        
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void publish_simple_meta_Ok() throws Exception {
        // Prepare
        //Path target = Paths.get("/Users/stefan/tmp/metapublisher/out/");
        Path target = folder.newFolder("publish_simple_meta_Ok").toPath();
        String themePublication = "ch.so.afu.abbaustellen";
        
        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_simple_meta_Ok");
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/thema-config/ch.so.afu.abbaustellen"), themePublication, target);
        
        // Check results
        File htmlFile = target.resolve(themePublication).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+themePublication+".html").toFile();
        assertTrue(htmlFile.exists());
        
        byte[] bytes = Files.readAllBytes(htmlFile.toPath());
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
        assertTrue(fileContent.contains("<div id=\"title\">Abbaustellen</div>"));

        File xtfFile = target.resolve(PATH_ELE_CONFIG).resolve("meta-"+themePublication+".xtf").toFile();
        assertTrue(xtfFile.exists());
    }

    @Test
    public void publish_simple_meta_geocat_Ok() throws Exception {
        // Prepare
        //Path target = Paths.get("/Users/stefan/tmp/metapublisher/out/");
        //Path geocatTarget = Paths.get("/Users/stefan/tmp/metapublisher/geocat/");
        Path target = folder.newFolder("publish_simple_meta_geocat_Ok").toPath();
        Path geocatTarget = folder.newFolder("publish_simple_meta_geocat_Ok_geocat").toPath();
        String themePublication = "ch.so.afu.abbaustellen";
        
        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_simple_meta_Ok");
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/thema-config/ch.so.afu.abbaustellen"), themePublication, target, null, geocatTarget);
        
        // Check results
        File xmlFile = geocatTarget.resolve(GEOCAT_FTP_DIR).resolve(themePublication+".xml").toFile();
        assertTrue(xmlFile.exists());
        
        byte[] bytes = Files.readAllBytes(xmlFile.toPath());
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("<gco:CharacterString>Abbaustellen</gco:CharacterString>"));
    }

    @Test
    public void publish_regions_meta_Ok() throws Exception {
        // Prepare
        //Path target = Paths.get("/Users/stefan/tmp/metapublisher/out/");
        Path target = folder.newFolder("publish_regions_meta_Ok").toPath();
        String themePublication = "ch.so.agi.av.dm01_so";
        List<String> regions = new ArrayList<String>() {{ 
            add("2463"); 
            add("2549"); 
            add("2524"); 
        }};
        
        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_regions_meta_Ok");
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/thema-config/ch.so.agi.amtliche_vermessung"), themePublication, target, regions, null);

        // Check results
        File htmlFile = target.resolve(themePublication).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+themePublication+".html").toFile();
        assertTrue(htmlFile.exists());

        {
            byte[] bytes = Files.readAllBytes(htmlFile.toPath());
            String fileContent = new String (bytes);
            assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
            assertTrue(fileContent.contains("<div id=\"title\">Amtliche Vermessung (DM01 CH + DXF/Geobau)</div>"));  
        }

        File xtfFile = target.resolve(PATH_ELE_CONFIG).resolve("meta-"+themePublication+".xtf").toFile();
        assertTrue(xtfFile.exists());
        
        File jsonFile = target.resolve(PATH_ELE_CONFIG).resolve(themePublication + ".json").toFile();
        assertTrue(jsonFile.exists()); 
        
        {
            byte[] bytes = Files.readAllBytes(jsonFile.toPath());
            String fileContent = new String (bytes);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(new Date());

            assertTrue(fileContent.contains(formattedDate));
        }
    }
    
    @Test
    public void publish_regions_meta_geocat_Ok() throws Exception {
        // Prepare
        //Path target = Paths.get("/Users/stefan/tmp/metapublisher/out/");
        //Path geocatTarget = Paths.get("/Users/stefan/tmp/metapublisher/geocat/");
        Path target = folder.newFolder("publish_regions_meta_Ok").toPath();
        Path geocatTarget = folder.newFolder("publish_regions_meta_Ok_geocat").toPath();
        String themePublication = "ch.so.agi.av.dm01_so";
        List<String> regions = new ArrayList<String>() {{ 
            add("2463"); 
            add("2549"); 
            add("2524"); 
        }};
        
        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_regions_meta_Ok");
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/thema-config/ch.so.agi.amtliche_vermessung"), themePublication, target, regions, geocatTarget);

        // Check results
        File xmlFile = geocatTarget.resolve(GEOCAT_FTP_DIR).resolve(themePublication+".xml").toFile();
        assertTrue(xmlFile.exists());

        byte[] bytes = Files.readAllBytes(xmlFile.toPath());
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("<gco:CharacterString>Amtliche Vermessung"));  
    }
}
