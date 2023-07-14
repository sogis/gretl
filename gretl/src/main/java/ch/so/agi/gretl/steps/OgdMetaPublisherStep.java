package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Objects;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iom_j.xtf.XtfWriter;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxWriter;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.ObjectEvent;
import ch.interlis.iox_j.StartBasketEvent;
import ch.interlis.iox_j.StartTransferEvent;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.ogdmetapublisher.AttributeDescription;
import ch.so.agi.gretl.steps.ogdmetapublisher.ClassDescription;
import ch.so.agi.gretl.steps.ogdmetapublisher.ModelMetaDescription;

public class OgdMetaPublisherStep {
    private GretlLogger log;
    private String taskName;

    private static final String MODEL_NAME = "SO_OGD_Metadata_20230629";
    private static final String ILI_TOPIC = MODEL_NAME+".Datasets";
    private static final String TAG = ILI_TOPIC+".Dataset";
    private static final String BID = MODEL_NAME+".Datasets";
    
    private static final String ILI_MODEL_METADATA = "SO_OGD_Metadata_20230629.ili";
    private static final String METAPUBLISHER_RESOURCE_DIR = "ogdmetapublisher";
    private static final String ILI_DIR_NAME = "ili";
    private static final String CORE_DATA_DIR_NAME = "core_data";

    private static final String RESOURCE_STRUCTURE_TAG = MODEL_NAME+".Resource";
    private static final String MODELLINK_STRUCTURE_TAG = MODEL_NAME+".ModelLink";
    private static final String CLASS_DESCRIPTION_TAG = MODEL_NAME+".ClassDescription";
    private static final String ATTRIBUTE_DESCRIPTION_TAG = MODEL_NAME+".AttributeDescription";

    private static final String OFFICE_STRUCTURE_TAG = MODEL_NAME+".Office_";
    private static final String CORE_DATA_OFFICES = "offices.xtf";
    
    private static final String FILEFORMAT_STRUCTURE_TAG = MODEL_NAME+".FileFormat";
    private static final String CORE_DATA_FILEFORMATS = "fileformats.xtf";

    public OgdMetaPublisherStep() {
        this(null);
    }
    
    public OgdMetaPublisherStep(String taskName) {
        if (taskName == null) {
            this.taskName = OgdMetaPublisherStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    // TODO -> doc: Es wird immer das ganze Dataset publiziert. Es koennen nicht sub datasets publiziert
    // werden. Als Konvention ("Verstaendniseinheit"). Darum wird auch immer das Meta-Xtf fuer den
    // ganzen Datensatz erzeugt.
    public void execute(Path configPath, Path outputPath) throws IOException, Ili2cException, IoxException {
        log.lifecycle(String.format("Start OgdMetaPublisher(Name: %s configPath: %s outputPath: %s)", taskName,
                configPath, outputPath));

        TomlParseResult tomlContent = Toml.parse(configPath);
        Map<String,Object> tomlMap = tomlContent.toMap();
        
        Entry<String,Object> parentEntry = null;
        for (Map.Entry<String, Object> entry : tomlMap.entrySet()) {            
            if (parentEntry == null || parentEntry.getKey().length() > entry.getKey().length()) {
                parentEntry = entry;
            } 
        }

        TomlTable parentTable = (TomlTable) parentEntry.getValue();
        String identifier = parentEntry.getKey();
        String title = parentTable.getString("title");
        String description = parentTable.getString("description");
        String publisher = parentTable.getString("publisher");
        List<Object> themeList = parentTable.getArray("theme").toList();
        List<String> themeStringList = themeList
            .stream()
            .map((obj) -> Objects.toString(obj, null))
            .collect(Collectors.toList());
        String theme = String.join(",",themeStringList);
        List<Object> keywordsList = parentTable.getArray("keywords").toList();
        List<String> keywordsStringList = keywordsList
            .stream()
            .map((obj) -> Objects.toString(obj, null))
            .collect(Collectors.toList());
        String keywords = String.join(",",keywordsStringList); 
        LocalDate startDate = parentTable.getLocalDate("startDate");
        LocalDate endDate = parentTable.getLocalDate("endDate");
                
        File xtfFile = Paths.get(outputPath.toFile().getAbsolutePath(), "meta-"+identifier+".xtf").toFile();
        TransferDescription td = getTransferdescription();
        IoxWriter ioxWriter = new XtfWriter(xtfFile, td);

        ioxWriter.write(new StartTransferEvent("SOGIS-20230701", "", null));
        ioxWriter.write(new StartBasketEvent(ILI_TOPIC,BID));

        Iom_jObject iomObj = new Iom_jObject(TAG, identifier);
        iomObj.setattrvalue("Identifier", identifier);
        iomObj.setattrvalue("Title", title);
        if (description!=null) iomObj.setattrvalue("Description", description);

        IomObject publisherIomObject = getIomObjectById(publisher, CORE_DATA_OFFICES);

        if (publisherIomObject!=null) {
            convertIomObjectToStructure(publisherIomObject, OFFICE_STRUCTURE_TAG); 
            iomObj.addattrobj("Publisher", publisherIomObject);
        }
        
        if (theme!=null) iomObj.setattrvalue("Theme", theme);
        if (keywords!=null) iomObj.setattrvalue("Keywords", keywords);
        
        iomObj.setattrvalue("StartDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        iomObj.setattrvalue("EndDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        for (Map.Entry<String, Object> entry : tomlMap.entrySet()) {            
            if (tomlMap.size() > 1 && entry.getKey().equals(parentEntry.getKey())) {
                continue;
            } 
            TomlTable tomlTable = (TomlTable) entry.getValue();
            
            String resourceIdentifier = entry.getKey();
            String resourceTitle = tomlTable.getString("title");
            String resourceDescription = tomlTable.getString("description");
            String models = tomlTable.getString("models");
            
            Iom_jObject resourceObj = new Iom_jObject(RESOURCE_STRUCTURE_TAG, null);
            resourceObj.setattrvalue("Identifier", resourceIdentifier);
            resourceObj.setattrvalue("Title", resourceTitle);
            if (resourceDescription != null) resourceObj.setattrvalue("Description", resourceDescription);

            Iom_jObject modelObj = new Iom_jObject(MODELLINK_STRUCTURE_TAG, null); 
            modelObj.setattrvalue("Name", models);
            modelObj.setattrvalue("LocationHint", "https://geo.so.ch/models"); 
            resourceObj.addattrobj("Model", modelObj);    
            
            resourceObj.setattrvalue("ConfigId", resourceIdentifier);
            resourceObj.setattrvalue("lastPublishingDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            Map<String,ClassDescription> classDescriptions = ModelMetaDescription.getDescription(models, configPath.getParent().toFile());
            for (Map.Entry<String, ClassDescription> classEntry : classDescriptions.entrySet()) {
                Iom_jObject classDescObj = new Iom_jObject(CLASS_DESCRIPTION_TAG, null); 

                ClassDescription classDescription = classEntry.getValue();

                classDescObj.setattrvalue("Name", classDescription.getName());
                classDescObj.setattrvalue("Title", classDescription.getTitle());
                if (classDescription.getDescription()!=null) classDescObj.setattrvalue("Description", classDescription.getDescription());

                List<AttributeDescription> attributeDescriptions = classDescription.getAttributes();
                for (AttributeDescription attributeDescription : attributeDescriptions) {
                    Iom_jObject attributeDescObj = new Iom_jObject(ATTRIBUTE_DESCRIPTION_TAG, null); 

                    attributeDescObj.setattrvalue("Name", attributeDescription.getName());
                    if (attributeDescription.getDescription()!=null) attributeDescObj.setattrvalue("Description", attributeDescription.getDescription());
                    attributeDescObj.setattrvalue("DataType", attributeDescription.getDataType().name());
                    attributeDescObj.setattrvalue("isMandatory", attributeDescription.isMandatory()?"true":"false");
                    classDescObj.addattrobj("AttributeDescription", attributeDescObj);
                }
                resourceObj.addattrobj("ClassDescription", classDescObj);
            }     
            
            List<Object> fileFormatList = tomlTable.getArray("fileFormats").toList();

            List<IomObject> formatIomObjects = new ArrayList<>();
            for (Object format : fileFormatList) {
                IomObject formatObj = getIomObjectById(format.toString(), CORE_DATA_FILEFORMATS);
                convertIomObjectToStructure(formatObj, FILEFORMAT_STRUCTURE_TAG);
                formatIomObjects.add(formatObj);
            }

            for (IomObject formatStructure : formatIomObjects) {
                resourceObj.addattrobj("FileFormats", formatStructure);
            }

            iomObj.addattrobj("Resources", resourceObj);
        }

        ioxWriter.write(new ObjectEvent(iomObj));
        
        ioxWriter.write(new EndBasketEvent());
        ioxWriter.write(new EndTransferEvent());
        ioxWriter.flush();
        ioxWriter.close();    

    }
    
    private TransferDescription getTransferdescription() throws IOException, Ili2cFailure {        
        File iliFile = copyResourceToTmpDir(METAPUBLISHER_RESOURCE_DIR +"/"+ ILI_DIR_NAME + "/" + ILI_MODEL_METADATA);
        //File iliFile = copyResourceToTmpDir(ILI_DIR_NAME + "/" + ILI_MODEL_METADATA);

        ArrayList<String> filev = new ArrayList<String>() {{ add(iliFile.getAbsolutePath()); }};
        TransferDescription td = Ili2c.compileIliFiles(filev, null);

        if (td == null) {
            throw new IllegalArgumentException("INTERLIS compiler failed");
        }

        return td;
    }
    
    private IomObject getIomObjectById(String id, String coreDataFileName) throws IOException, IoxException {
        File xtfFile = copyResourceToTmpDir(METAPUBLISHER_RESOURCE_DIR + "/" + CORE_DATA_DIR_NAME + "/" + coreDataFileName);
        //File xtfFile = Utils.copyResourceToTmpDir(CORE_DATA_DIR_NAME + "/" + coreDataFileName);

        XtfReader xtfReader = new XtfReader(xtfFile);

        IoxEvent event = xtfReader.read();
        while (event instanceof IoxEvent) {
            if (event instanceof ObjectEvent) {
                ObjectEvent objectEvent = (ObjectEvent) event;
                IomObject iomObj = objectEvent.getIomObject();
                if (iomObj.getobjectoid().equalsIgnoreCase(id)) {
                    return iomObj;
                }
            }
            event = xtfReader.read();
        }
        return null;
    }

    private void convertIomObjectToStructure(IomObject iomObj, String tag) {
        iomObj.setobjecttag(tag);
        iomObj.setobjectoid(null);
    }

    public static File copyResourceToTmpDir(String resource) {
        try {
            InputStream is = OgdMetaPublisherStep.class.getClassLoader().getResourceAsStream(resource);
            if (is==null) return null;
            Path exportDir = Files.createTempDirectory("ogdmetapublisherstep");
            Path exportedFile = exportDir.resolve(new File(resource).getName());
            Files.copy(is, exportedFile, StandardCopyOption.REPLACE_EXISTING);
            return exportedFile.toFile();            
        } catch (IOException e) {
            return null;
        }
    }

}
