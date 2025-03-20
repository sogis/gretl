package ch.so.agi.gretl.tasks.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import ch.interlis.iox_j.PipelinePool;
import org.interlis2.validator.Validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.interlis.ioxwkf.json.JsonReader;

public class JsonValidatorImpl extends Validator {

    @Override
    protected IoxReader createReader(String filename, TransferDescription td, LogEventFactory errFactory,
            Settings settings, PipelinePool pool) throws IoxException {
                
        try {
            Path jsonFile = preprocessJsonFile(filename);

            JsonReader reader = new JsonReader(jsonFile.toFile(), settings);
            reader.setModel(td);

            return reader;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IoxException(e);
        }

//        boolean firstLineIsHeader = false;
//        {
//            String val = settings.getValue(IoxWkfConfig.SETTING_FIRSTLINE);
//            if (IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER.equals(val)) {
//                firstLineIsHeader = true;
//            }
//        }
//        reader.setFirstLineIsHeader(firstLineIsHeader);
//        char valueDelimiter = IoxWkfConfig.SETTING_VALUEDELIMITER_DEFAULT;
//        {
//            String val = settings.getValue(IoxWkfConfig.SETTING_VALUEDELIMITER);
//            if (val != null) {
//                valueDelimiter = val.charAt(0);
//            }
//        }
//        reader.setValueDelimiter(valueDelimiter);
//        char valueSeparator = IoxWkfConfig.SETTING_VALUESEPARATOR_DEFAULT;
//        {
//            String val = settings.getValue(IoxWkfConfig.SETTING_VALUESEPARATOR);
//            if (val != null) {
//                valueSeparator = val.charAt(0);
//            }
//        }
//        reader.setValueSeparator(valueSeparator);
    }
    
    private Path preprocessJsonFile(String jsonFile) throws IOException {
        Path sourcePath = Paths.get(jsonFile);        
        Path tempDir = Files.createTempDirectory("jsonvalidator_");
        Path newFile = tempDir.resolve(sourcePath.getFileName());

        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(jsonFile);
        JsonNode rootNode = objectMapper.readTree(file);

        // Check if the root node is an array
        if (!rootNode.isArray()) {
            // Create a new array node and add the root node to it
            ArrayNode newArrayNode = objectMapper.createArrayNode();
            
            // Process the root node
            if (rootNode.isObject()) {
                addAttributes((ObjectNode) rootNode, 1);
            }
            
            newArrayNode.add(rootNode);
            
            // Write the updated JSON to the new file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(newFile.toFile(), newArrayNode);
        } else {
            int idCounter = 1;
            // Process each object in the array
            for (JsonNode node : rootNode) {
                if (node.isObject()) {
                    addAttributes((ObjectNode) node, idCounter++);
                }
            }
            
            // Write the updated JSON to the new file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(newFile.toFile(), rootNode);
        }
        return newFile;   
    }
    
    private void addAttributes(ObjectNode objectNode, int id) {
        JsonNode typeNode = objectNode.get("@type");
        if (typeNode == null || !typeNode.isTextual()) {
            throw new IllegalArgumentException("Missing or invalid @type attribute in JSON object.");
        }
        
        if (!objectNode.has("@topic")) {
            String typeValue = typeNode.asText();
            String[] parts = typeValue.split("\\.");
            if (parts.length >= 2) {
                String topicValue = parts[0] + "." + parts[1];
                objectNode.put("@topic", topicValue);
            }
        }
        
        if (!objectNode.has("@id")) {
            objectNode.put("@id", "o" + id);
        }
        
        if (!objectNode.has("@bid")) {
            objectNode.put("@bid", "b1");
        }
    }
}
