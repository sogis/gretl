package ch.so.agi.gretl.util.metapublisher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Regions {
    private GretlLogger log = LogEnvironment.getLogger(this.getClass());;
    
    public void updateJson(File jsonFile, Map<String,String> regionMap) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);
       
        JsonNode features = rootNode.get("features");
        Iterator<JsonNode> feati = features.iterator();
        while(feati.hasNext()) {
            JsonNode feature = feati.next();
            JsonNode properties = feature.get("properties");
            JsonNode identifier = properties.get("identifier");
            String identifierValue = identifier.asText();
            
            if (regionMap.get(identifierValue) != null) {
                ((ObjectNode)properties).put("lastPublishingDate", regionMap.get(identifierValue));
            }
        }
        
        try (FileWriter file = new FileWriter(jsonFile)) {
            file.write(rootNode.toString());
        }        
    }
}
