package ch.so.agi.gretl.util.metapublisher;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.metapublisher.meta.RegionsUtil;
import ch.so.agi.gretl.testutil.TestUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegionsTest {
    protected GretlLogger log;

    public RegionsTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @TempDir
    public Path folder;

    @Test
    public void update_Ok() throws Exception {
        // Prepare
        String updateDate = "2023-03-22";
        String origDate = "1999-01-01";
        String fileName = "ch.so.agi.av.dm01_so.json";
        File jsonFile = Paths.get("src/test/resources/data/metapublisher/util/", fileName).toFile();
        Path outDirectory = TestUtil.createTempDir(folder, "update_ok");
        File targetJsonFile = Paths.get(outDirectory.toAbsolutePath().toString(), fileName).toFile();

        Files.copy(jsonFile.toPath(), targetJsonFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        Map<String,String> regionMap = new HashMap<String,String>() {{
            put("2463", updateDate);
            put("2524", updateDate);
            put("2549", updateDate);
        }}; 
        
        // Run
        RegionsUtil regions = new RegionsUtil();
        regions.updateJson(targetJsonFile, regionMap);
        
        // Check result
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(targetJsonFile);
       
        JsonNode features = rootNode.get("features");
        Iterator<JsonNode> feati = features.iterator();
        while(feati.hasNext()) {
            JsonNode feature = feati.next();
            JsonNode properties = feature.get("properties");
            JsonNode identifier = properties.get("identifier");
            String identifierValue = identifier.asText();
            
            if (regionMap.get(identifierValue) != null) {
                assertEquals(properties.get("lastPublishingDate").asText(), updateDate);
            } else {
                assertEquals(properties.get("lastPublishingDate").asText(), origDate);
            }
        }
    }

}
