package ch.so.agi.gretl.jobs;


import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetaPublisherTest {
    public static final String PATH_ELE_ROOT = "build";
    public static final String PATH_ELE_AKTUELL = "aktuell";
    public static final String PATH_ELE_META = "meta";
    public static final String PATH_ELE_CONFIG = "config";
    public static final String GEOCAT_FTP_DIR = "geocat";
    public static final String GEOCAT_PATH_ELE_ENV = "int"; // TODO: beim Testen muss ich es wohl eh hardcodieren? Hier ja, aber dem Job irgend eine ENV-Variable uebergeben oder so. Andi fragen.

    @Test
    public void simple_Ok() throws Exception {

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/MetaPublisher/afu_abbaustellen_pub");
        String dataIdent = "ch.so.afu.abbaustellen";

        IntegrationTestUtil.executeTestRunner(projectDirectory, "publishMetaFile");
        
        // Check result
        Path target = projectDirectory.toPath();
        Path htmlFile = target.resolve(PATH_ELE_ROOT).resolve(dataIdent).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+dataIdent+".html");
        assertTrue(Files.exists(htmlFile));
        {
            byte[] bytes = Files.readAllBytes(htmlFile);
            String fileContent = new String (bytes);
            assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
            assertTrue(fileContent.contains("<div id=\"title\">Abbaustellen</div>"));   
        }
        
        Path xtfFile = target.resolve(PATH_ELE_ROOT).resolve(PATH_ELE_CONFIG).resolve("meta-"+dataIdent+".xtf");
        assertTrue(Files.exists(xtfFile));
        
        Path xmlFile = target.resolve(PATH_ELE_ROOT).resolve(GEOCAT_FTP_DIR).resolve(GEOCAT_PATH_ELE_ENV).resolve(dataIdent+".xml");
        assertTrue(Files.exists(xmlFile));
        {
            byte[] bytes = Files.readAllBytes(xmlFile);
            String fileContent = new String (bytes);
            assertTrue(fileContent.contains("<gco:CharacterString>Abbaustellen</gco:CharacterString>"));
            assertTrue(fileContent.contains("files-i.geo.so.ch"));
        }
    }
    
    @Test
    public void regions_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/MetaPublisher/agi_dm01so_pub");
        String dataIdent = "ch.so.agi.av.dm01_so";

        IntegrationTestUtil.executeTestRunner(projectDirectory, "publishMetaFiles");
        
        // Check result
        Path target = projectDirectory.toPath();
        Path htmlFile = target.resolve(PATH_ELE_ROOT).resolve(dataIdent).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+dataIdent+".html");
        assertTrue(Files.exists(htmlFile));
        
        {
            byte[] bytes = Files.readAllBytes(htmlFile);
            String fileContent = new String (bytes);
            assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
            assertTrue(fileContent.contains("<div id=\"title\">Amtliche Vermessung (DM01 CH + DXF/Geobau)</div>"));            
        }

        Path xtfFile = target.resolve(PATH_ELE_ROOT).resolve(PATH_ELE_CONFIG).resolve("meta-"+dataIdent+".xtf");
        assertTrue(Files.exists(xtfFile));

        Path jsonFile = target.resolve(PATH_ELE_ROOT).resolve(PATH_ELE_CONFIG).resolve(dataIdent + ".json");
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
    public void static_regions_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/MetaPublisher/agi_orthofoto_1993_meta_pub");
        String dataIdent = "ch.so.agi.orthofoto_1993.grau";

        IntegrationTestUtil.executeTestRunner(projectDirectory, "publishMetaFiles");

        // Check result
        Path target = projectDirectory.toPath();
        
        Path xtfFile = target.resolve(PATH_ELE_ROOT).resolve(PATH_ELE_CONFIG).resolve("meta-"+dataIdent+".xtf");
        assertTrue(Files.exists(xtfFile));
        
        {
            byte[] bytes = Files.readAllBytes(xtfFile);
            String fileContent = new String (bytes);
            assertTrue(fileContent.contains("<identifier>2612519_1254998</identifier>"));
        }

        Path jsonFile = target.resolve(PATH_ELE_ROOT).resolve(PATH_ELE_CONFIG).resolve(dataIdent + ".json");
        assertTrue(Files.exists(jsonFile)); 
    }
}
