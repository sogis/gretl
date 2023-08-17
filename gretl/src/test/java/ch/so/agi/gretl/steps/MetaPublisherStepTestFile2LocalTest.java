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
    public static final String GEOCAT_FTP_DIR_INT = "int"; 
    public static final String GEOCAT_FTP_DIR_PROD = "prod"; 
    
    public MetaPublisherStepTestFile2LocalTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }
        
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void publish_raster_geocat_Ok() throws Exception {
        //Path target = Paths.get("/Users/stefan/tmp/metapublisher/out/");
        //Path geocatTarget = Paths.get("/Users/stefan/tmp/metapublisher/geocat/");
        Path target = folder.newFolder("publish_raster_geocat_Ok").toPath();
        Path geocatTarget = folder.newFolder("publish_raster_geocat_Ok_geocat").toPath();
        String themePublication = "ch.so.agi.orthofoto_1993.grau";

        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_raster_geocat_Ok");
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/agi_orthofoto_1993_meta_pub/meta.toml"), target, null, geocatTarget, "production");

        // Check results
        Path jsonFile = target.resolve(PATH_ELE_CONFIG).resolve(themePublication + ".json");
        assertTrue(Files.exists(jsonFile)); 
        
        Path xtfFile = target.resolve(PATH_ELE_CONFIG).resolve("meta-"+themePublication+".xtf");
        assertTrue(Files.exists(xtfFile)); 
        
        byte[] bytes = Files.readAllBytes(xtfFile);
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("<identifier>2612519_1254998</identifier>"));
        assertTrue(fileContent.contains("<lastPublishingDate>1993-04-01</lastPublishingDate>"));
    }
   
    @Test
    public void publish_simple_meta_Ok() throws Exception {
        // Prepare
        //Path target = Paths.get("/Users/stefan/tmp/metapublisher/out/");
        Path target = folder.newFolder("publish_simple_meta_Ok").toPath();
        String themePublication = "ch.so.afu.abbaustellen";

        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_simple_meta_Ok");
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/afu_abbaustellen_pub/meta.toml"), target, null, null, "integration");
        
        // Check results
        Path htmlFile = target.resolve(themePublication).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+themePublication+".html");
        assertTrue(Files.exists(htmlFile));
        
        byte[] bytes = Files.readAllBytes(htmlFile);
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
        assertTrue(fileContent.contains("<div id=\"title\">Abbaustellen</div>"));

        Path xtfFile = target.resolve(PATH_ELE_CONFIG).resolve("meta-"+themePublication+".xtf");
        assertTrue(Files.exists(xtfFile));
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
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/afu_abbaustellen_pub/meta.toml"), target, null, geocatTarget, "integration");
        
        // Check results
        Path xmlFile = geocatTarget.resolve(GEOCAT_FTP_DIR_INT).resolve(themePublication+".xml");
        assertTrue(Files.exists(xmlFile));
        
        byte[] bytes = Files.readAllBytes(xmlFile);
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("<gco:CharacterString>Abbaustellen</gco:CharacterString>"));
        assertTrue(fileContent.contains("files-i.geo.so.ch"));
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
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/agi_dm01so_pub/meta-dm01_so.toml"), target, regions, null, "integration");

        // Check results
        Path htmlFile = target.resolve(themePublication).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+themePublication+".html");
        assertTrue(Files.exists(htmlFile));

        {
            byte[] bytes = Files.readAllBytes(htmlFile);
            String fileContent = new String (bytes);
            assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
            assertTrue(fileContent.contains("<div id=\"title\">Amtliche Vermessung (DM01 CH + DXF/Geobau)</div>"));  
        }

        Path xtfFile = target.resolve(PATH_ELE_CONFIG).resolve("meta-"+themePublication+".xtf");
        assertTrue(Files.exists(xtfFile));
        
        Path jsonFile = target.resolve(PATH_ELE_CONFIG).resolve(themePublication + ".json");
        assertTrue(Files.exists(jsonFile)); 
        
        {
            byte[] bytes = Files.readAllBytes(jsonFile);
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
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/agi_dm01so_pub/meta-dm01_so.toml"), target, regions, geocatTarget, "integration");

        // Check results
        Path xmlFile = geocatTarget.resolve(GEOCAT_FTP_DIR_INT).resolve(themePublication+".xml");
        assertTrue(Files.exists(xmlFile));

        byte[] bytes = Files.readAllBytes(xmlFile);
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("<gco:CharacterString>Amtliche Vermessung"));  
    }
}
