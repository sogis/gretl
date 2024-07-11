package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class MetaPublisherStepTestFile2LocalTest {

    public static final String PATH_ELE_AKTUELL = "aktuell";
    public static final String PATH_ELE_META = "meta";
    public static final String PATH_ELE_CONFIG = "config";
    public static final String GEOCAT_FTP_DIR_INT = "int";
    public static final String GEOCAT_FTP_DIR_PROD = "prod";

    @TempDir
    public Path folder;

    @Test
    public void publish_raster_geocat_Ok() throws Exception {
        Path target = TestUtil.createTempDir(folder, "publish_raster_geocat_Ok");
        Path geocatTarget = TestUtil.createTempDir(folder, "publish_raster_geocat_Ok_geocat");
        String themePublication = "ch.so.agi.orthofoto_1993.grau";

        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_raster_geocat_Ok");
        metaPublisherStep.execute(TestUtil.getResourceFile(TestUtil.AGI_ORTHOFOTO_META_TOML_PATH), target, null, geocatTarget, "production");

        // Check results
        Path jsonFile = target.resolve(PATH_ELE_CONFIG).resolve(themePublication + ".json");
        assertTrue(Files.exists(jsonFile)); 
        
        Path xtfFile = target.resolve(PATH_ELE_CONFIG).resolve(String.format("meta-%s.xtf", themePublication));
        assertTrue(Files.exists(xtfFile)); 
        
        byte[] bytes = Files.readAllBytes(xtfFile);
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("<identifier>2612519_1254998</identifier>"));
        assertTrue(fileContent.contains("<lastPublishingDate>1993-04-01</lastPublishingDate>"));
    }
   
    @Test
    public void publish_simple_meta_Ok() throws Exception {
        // Prepare
        Path target = TestUtil.createTempDir(folder, "publish_simple_meta_Ok");
        String themePublication = "ch.so.afu.abbaustellen";

        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_simple_meta_Ok");
        metaPublisherStep.execute(TestUtil.getResourceFile(TestUtil.AFU_ABBAUSTELLEN_META_TOML_PATH), target, null, null, "integration");
        
        // Check results
        Path htmlFile = target
                .resolve(themePublication)
                .resolve(PATH_ELE_AKTUELL)
                .resolve(PATH_ELE_META)
                .resolve(String.format("meta-%s.html", themePublication));

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
        Path target = TestUtil.createTempDir(folder, "publish_simple_meta_geocat_Ok");
        Path geocatTarget = TestUtil.createTempDir(folder, "publish_simple_meta_geocat_Ok_geocat");
        String themePublication = "ch.so.afu.abbaustellen";
        
        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_simple_meta_Ok");
        metaPublisherStep.execute(TestUtil.getResourceFile(TestUtil.AFU_ABBAUSTELLEN_META_TOML_PATH), target, null, geocatTarget, "integration");
        
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
        Path target = TestUtil.createTempDir(folder, "publish_regions_meta_Ok");
        String themePublication = "ch.so.agi.av.dm01_so";
        List<String> regions = new ArrayList<String>() {{ 
            add("2463"); 
            add("2549"); 
            add("2524"); 
        }};
        
        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_regions_meta_Ok");
        metaPublisherStep.execute(TestUtil.getResourceFile(TestUtil.AGI_DM01SO_META_TOML_PATH), target, regions, null, "integration");

        // Check results
        Path htmlFile = target.resolve(themePublication).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+themePublication+".html");
        assertTrue(Files.exists(htmlFile));

        {
            byte[] bytes = Files.readAllBytes(htmlFile);
            String fileContent = new String (bytes);
            assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
            assertTrue(fileContent.contains("<div id=\"title\">Amtliche Vermessung (DM01 CH + DXF/Geobau)</div>"));  
        }

        Path xtfFile = target.resolve(PATH_ELE_CONFIG).resolve(String.format("meta-%s.xtf", themePublication));
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
        Path target = TestUtil.createTempDir(folder, "publish_regions_meta_Ok");
        Path geocatTarget = TestUtil.createTempDir(folder, "publish_regions_meta_Ok_geocat");
        String themePublication = "ch.so.agi.av.dm01_so";
        List<String> regions = new ArrayList<String>() {{ 
            add("2463"); 
            add("2549"); 
            add("2524"); 
        }};
        
        // Run step
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("publish_regions_meta_Ok");
        metaPublisherStep.execute(TestUtil.getResourceFile(TestUtil.AGI_DM01SO_META_TOML_PATH), target, regions, geocatTarget, "integration");

        // Check results
        Path xmlFile = geocatTarget.resolve(GEOCAT_FTP_DIR_INT).resolve(themePublication+".xml");
        assertTrue(Files.exists(xmlFile));

        byte[] bytes = Files.readAllBytes(xmlFile);
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("<gco:CharacterString>Amtliche Vermessung"));  
    }
}
