package ch.so.agi.gretl.steps.metapublisher.meta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RegionsUtil {
    private GretlLogger log = LogEnvironment.getLogger(this.getClass());;
    
    private static final String ITEM_STRUCTURE_TAG = "SO_AGI_Metadata_20230304.Item";
    
    public static void updateJson(File jsonFile, Map<String,String> regionMap) throws JsonParseException, JsonMappingException, IOException {
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
    
    // TODO: boundary und geometry -> verheiraten mit getBoundary
    public static void getItems(File jsonFile, List<IomObject> items) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);
        JsonNode features = rootNode.get("features");
        Iterator<JsonNode> feati = features.iterator();
        while(feati.hasNext()) {
            JsonNode feature = feati.next();
            JsonNode properties = feature.get("properties");
            JsonNode identifier = properties.get("identifier");
            JsonNode title = properties.get("title");
            JsonNode lastPublishingDate = properties.get("lastPublishingDate");
            
            Iom_jObject itemObj = new Iom_jObject(ITEM_STRUCTURE_TAG, null);
            itemObj.setattrvalue("identifier", identifier.asText());
            itemObj.setattrvalue("title", title.asText());
            itemObj.setattrvalue("lastPublishingDate", lastPublishingDate.asText());
            items.add(itemObj);
        }
    }
    
    public static void getBoundary(File jsonFile, Map<String, Double> boundary) throws JsonProcessingException, IOException {
        Double westlimit = null;
        Double southlimit = null;
        Double eastlimit = null;
        Double northlimit = null;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);
        JsonNode features = rootNode.get("features");
        Iterator<JsonNode> feati = features.iterator();
        while(feati.hasNext()) {
            JsonNode feature = feati.next();
            JsonNode properties = feature.get("properties");
            JsonNode identifier = properties.get("identifier");
            JsonNode title = properties.get("title");
            JsonNode geometry = feature.get("geometry");
            JsonNode type = geometry.get("type");
            
            List<JsonNode> exteriorRings = new ArrayList<JsonNode>();
            if (type.asText().equalsIgnoreCase("MultiPolygon")) {
                JsonNode coordinates = geometry.get("coordinates");
                for (int i=0;i<coordinates.size();i++) {
                    JsonNode polygon = coordinates.get(0);
                    JsonNode exteriorRing = polygon.get(0);
                    exteriorRings.add(exteriorRing);
                }
            } else {
                JsonNode coordinates = geometry.get("coordinates");
                JsonNode exteriorRing = coordinates.get(0);
                exteriorRings.add(exteriorRing);
            }
            
            for (JsonNode exteriorRing : exteriorRings) {
                Iterator<JsonNode> coordi = exteriorRing.iterator();
                while (coordi.hasNext()) {
                    JsonNode coordinate = coordi.next();
                    double easting = coordinate.get(0).asDouble();
                    double northing = coordinate.get(1).asDouble();

                    if (westlimit == null) {
                        westlimit = easting;
                    }

                    if (southlimit == null) {
                        southlimit = northing;
                    }

                    if (eastlimit == null) {
                        eastlimit = easting;
                    }

                    if (northlimit == null) {
                        northlimit = northing;
                    }

                    if (easting < westlimit)
                        westlimit = easting;
                    if (northing < southlimit)
                        southlimit = northing;
                    if (easting > eastlimit)
                        eastlimit = easting;
                    if (northing > northlimit)
                        northlimit = northing;
                }
            }
            
            boundary.put("westlimit", westlimit);
            boundary.put("southlimit", southlimit);
            boundary.put("eastlimit", eastlimit);
            boundary.put("northlimit", northlimit);
        }
    }
}
