package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class MetaPublisherTest {
    public static final String PATH_ELE_ROOT = "build";
    public static final String PATH_ELE_AKTUELL = "aktuell";
    public static final String PATH_ELE_META = "meta";
    public static final String PATH_ELE_CONFIG = "config";

    @Test
    public void simple_Ok() throws Exception {
        // Prepare
        String jobDirectory = "src/integrationTest/jobs/MetaPublisher/ch.so.afu.abbaustellen/gretl/afu_abbaustellen_pub";
        String dataIdent = "ch.so.afu.abbaustellen";
                
        // Run task
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob(jobDirectory, gvs);
        
        // Check result
        Path target = Paths.get(jobDirectory);
        File htmlFile = target.resolve(PATH_ELE_ROOT).resolve(dataIdent).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+dataIdent+".html").toFile();
        assertTrue(htmlFile.exists());

        byte[] bytes = Files.readAllBytes(htmlFile.toPath());
        String fileContent = new String (bytes);
        assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
        assertTrue(fileContent.contains("<div id=\"title\">Abbaustellen</div>"));

        File xtfFile = target.resolve(PATH_ELE_ROOT).resolve(PATH_ELE_CONFIG).resolve("meta-"+dataIdent+".xtf").toFile();
        assertTrue(xtfFile.exists());
    }
    
    @Test
    public void regions_Ok() throws Exception {
        // Prepare
        String jobDirectory = "src/integrationTest/jobs/MetaPublisher/ch.so.agi.amtliche_vermessung/gretl/agi_dm01so_pub";
        String dataIdent = "ch.so.agi.av.dm01_so";
        
        // Run task
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob(jobDirectory, gvs);
        
        // Check result
        Path target = Paths.get(jobDirectory);
        File htmlFile = target.resolve(PATH_ELE_ROOT).resolve(dataIdent).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META).resolve("meta-"+dataIdent+".html").toFile();
        assertTrue(htmlFile.exists());
        
        {
            byte[] bytes = Files.readAllBytes(htmlFile.toPath());
            String fileContent = new String (bytes);
            assertTrue(fileContent.contains("Datenbeschreibung • Amt für Geoinformation Kanton Solothurn"));
            assertTrue(fileContent.contains("<div id=\"title\">Amtliche Vermessung (DM01 CH + DXF/Geobau)</div>"));            
        }

        File xtfFile = target.resolve(PATH_ELE_ROOT).resolve(PATH_ELE_CONFIG).resolve("meta-"+dataIdent+".xtf").toFile();
        assertTrue(xtfFile.exists());

        File jsonFile = target.resolve(PATH_ELE_ROOT).resolve(PATH_ELE_CONFIG).resolve(dataIdent + ".json").toFile();
        assertTrue(jsonFile.exists()); 
        
        {
            byte[] bytes = Files.readAllBytes(jsonFile.toPath());
            String fileContent = new String (bytes);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(new Date());

            assertTrue(fileContent.contains(formattedDate));
        }

    }
    

}
