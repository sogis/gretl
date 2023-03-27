package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
import ch.interlis.iox_j.ObjectEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.StartBasketEvent;
import ch.interlis.iox_j.StartTransferEvent;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.metapublisher.geocat.Geocat;
import ch.so.agi.gretl.steps.metapublisher.meta.ModelDescription;
import ch.so.agi.gretl.steps.metapublisher.meta.model.AttributeDescription;
import ch.so.agi.gretl.steps.metapublisher.meta.model.ClassDescription;
import ch.so.agi.gretl.steps.metapublisher.meta.util.RegionsUtil;
import ch.so.agi.gretl.steps.metapublisher.geocat.Geocat;

import freemarker.template.TemplateException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class MetaPublisherStep {
    private GretlLogger log;
    private String taskName;
    
    public static final String PATH_ELE_AKTUELL = "aktuell";
    public static final String PATH_ELE_META = "meta";
    public static final String PATH_ELE_CONFIG = "config";

    private static final List<String> META_TOML_CONFIG_SECTIONS = new ArrayList<String>() {{
        add("meta");
        add("config");
    }};
    
    private static final String ILI_MODEL_METADATA = "SO_AGI_Metadata_20230304.ili";
    private static final String XSL_HTML_METADATA = "xtf2html.xsl";
    
    private static final String CORE_DATA_OFFICES = "offices.xtf";
    private static final String CORE_DATA_FILEFORMATS = "fileformats.xtf";
    
    private static final String ILI_TOPIC = "SO_AGI_Metadata_20230304.ThemePublications";
    private static final String BID = "SO_AGI_Metadata_20230304.ThemePublications";
    private static final String TAG = "SO_AGI_Metadata_20230304.ThemePublications.ThemePublication";
    private static final String CLASS_DESCRIPTION_TAG = "SO_AGI_Metadata_20230304.ClassDescription";
    private static final String ATTRIBUTE_DESCRIPTION_TAG = "SO_AGI_Metadata_20230304.AttributeDescription";
    private static final String OFFICE_STRUCTURE_TAG = "SO_AGI_Metadata_20230304.Office_";
    private static final String MODELLINK_STRUCTURE_TAG = "SO_AGI_Metadata_20230304.ModelLink";
    private static final String BOUNDARY_STRUCTURE_TAG = "SO_AGI_Metadata_20230304.BoundingBox";
    private static final String FILEFORMAT_STRUCTURE_TAG = "SO_AGI_Metadata_20230304.FileFormat";
    private static final String ITEM_STRUCTURE_TAG = "SO_AGI_Metadata_20230304.Item";
    
    private static final String PUBLICATION_DIR_NAME = "publication";
    private static final String ILI_DIR_NAME = "ili";
    private static final String XSL_DIR_NAME = "xsl";
    private static final String SHARED_DIR_NAME = "shared";
    private static final String CORE_DATA_DIR_NAME = "core_data";

    public MetaPublisherStep(String taskName) {
        if (taskName == null) {
            this.taskName = XslTransformerStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    public MetaPublisherStep() {
        this(null);
    }
    
    public void execute(File themeRootDirectory, String themePublication, Path target) throws IOException, IoxException, Ili2cException, SaxonApiException, TemplateException { 
        execute(themeRootDirectory, themePublication, target, null, null, null);
    }
  
//    /**
//     * 
//     * @param themeRootDirectory the root directory of the theme
//     * @param themePublication the ident of the publication that is published (= dataIdent of PublisherTask)
//     * @param target the target root directory where the meta/config files will be saved
//     * @throws IOException
//     * @throws IoxException
//     * @throws Ili2cException
//     * @throws SaxonApiException
//     * @throws TemplateException 
//     */
    public void execute(File themeRootDirectory, String themePublication, Path target, List<String> regions, 
            Path geocatTarget, String gretlEnvironment) throws IOException, IoxException, Ili2cException, SaxonApiException, TemplateException {
        log.lifecycle(String.format(
                "Start MetaPublisherStep(Name: %s themeRootDirectory: %s themePublication: %s target: %s regions: %s geocatTarget: %s gretlEnvironment)",
                taskName, themeRootDirectory, themePublication, target, regions, geocatTarget, gretlEnvironment));
        
        // (1) Ordner erstellen im Zielverzeichnis, falls er nicht existiert.
        Path targetRootPath = target;
        Path targetPath = targetRootPath.resolve(themePublication).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META);

        log.lifecycle("create <"+targetPath.toString()+">...");
        Files.createDirectories(targetPath);
        
        File tomlFile = Paths.get(themeRootDirectory.getAbsolutePath(), PUBLICATION_DIR_NAME, themePublication, "meta.toml").toFile();
        TomlParseResult metaTomlResult = Toml.parse(tomlFile.toPath());

        // (2) Informationen aus meta.toml lesen, die wir zwingend hier benoetigen.
        String modelName = metaTomlResult.getString("meta.model");
        String identifier = metaTomlResult.getString("meta.identifier");
        boolean printClassDescription = metaTomlResult.getBoolean("config.printClassDescription", () -> true);
        
        // (3) Informationen aus ILI-Modell lesen und mit Informationen aus meta.toml ergaenzen und ggf. ueberschreiben.
        Map<String, ClassDescription> classDescriptions = null;
        if (modelName != null) {
            classDescriptions = ModelDescription.getDescriptions(modelName, themeRootDirectory, printClassDescription, metaTomlResult);            
        }
    
        // (4) GeoJSON-Datei nachfuehren zwecks Publikationsdatum einzelner Regionen.
        // Weil Publisher den Inhalt des meta-Verzeichnisses loescht, ist der Master
        // im config-Verzeichnis.
        // Falls die GeoJSON-Datei nicht im Zielverzeichnis vorhanden ist, wird das 
        // Template dorthin kopiert.
        Path targetConfigPath = targetRootPath.resolve(PATH_ELE_CONFIG);
        log.lifecycle("create <"+targetConfigPath.toString()+">...");
        Files.createDirectories(targetConfigPath);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        boolean staticRegionsFile = false;
        if (regions != null) {
            File targetGeojsonFile = Paths.get(targetConfigPath.toFile().getAbsolutePath(), themePublication + ".json").toFile();
            if (!targetGeojsonFile.exists()) {
                File sourceGeojsonFile = Paths.get(themeRootDirectory.getAbsolutePath(), PUBLICATION_DIR_NAME, themePublication, themePublication + ".json").toFile();
                Files.copy(sourceGeojsonFile.toPath(), targetGeojsonFile.toPath());
            }       
            
            String formattedDate = sdf.format(new Date());
            Map<String,String> regionMap = new HashMap<>();
            for (String regionIdentifier : regions) {
                regionMap.put(regionIdentifier, formattedDate);
            }

            RegionsUtil.updateJson(targetGeojsonFile, regionMap);
        } else {
            // Es wird immer versucht ein allenfalls vorhandenes GeoJSON-Regionenfile zu deployen.
            File sourceGeojsonFile = Paths.get(themeRootDirectory.getAbsolutePath(), PUBLICATION_DIR_NAME, themePublication, themePublication + ".json").toFile();
            if (sourceGeojsonFile.exists()) {
                staticRegionsFile = true;
                File targetGeojsonFile = Paths.get(targetConfigPath.toFile().getAbsolutePath(), themePublication + ".json").toFile();
                if (!targetGeojsonFile.exists()) {
                    Files.copy(sourceGeojsonFile.toPath(), targetGeojsonFile.toPath());   
                }
            }            
        }

        // (5) Weitere Informationen aus meta.toml lesen
        String title = metaTomlResult.getString("meta.title");
        String description = metaTomlResult.getString("meta.description");
        String keywords = metaTomlResult.getString("meta.keywords");
        String synonyms = metaTomlResult.getString("meta.synonyms");
        String owner = metaTomlResult.getString("meta.owner");
        String servicer = metaTomlResult.getString("meta.servicer");
        String licence = metaTomlResult.getString("meta.licence");
        String furtherInformation = metaTomlResult.getString("meta.furtherInformation");
        TomlArray formatsArray = metaTomlResult.getArrayOrEmpty("meta.formats");
        List<?> formats = formatsArray.toList(); 
        
        // (6) XTF herstellen
        log.lifecycle("writing xtf file");

        File xtfFile = Paths.get(targetPath.toFile().getAbsolutePath(), "meta-"+identifier+".xtf").toFile();
        TransferDescription td = getMetadataTransferdescription(themeRootDirectory);
        IoxWriter ioxWriter = new XtfWriter(xtfFile, td);

        ioxWriter.write(new StartTransferEvent("SOGIS-20230305", "", null));
        ioxWriter.write(new StartBasketEvent(ILI_TOPIC,BID));

        Iom_jObject iomObj = new Iom_jObject(TAG, String.valueOf(1));
        iomObj.setattrvalue("identifier", identifier);
        
        if (modelName != null) {
            Iom_jObject modelObj = new Iom_jObject(MODELLINK_STRUCTURE_TAG, null); 
            modelObj.setattrvalue("name", modelName);
            modelObj.setattrvalue("locationHint", "https://geo.so.ch/models");
            iomObj.addattrobj("model", modelObj);    
        }
        
        iomObj.setattrvalue("title", title);
        if (description!=null) iomObj.setattrvalue("shortDescription", description); // CDATA wird nicht beruecksichtigt, d.h. auch mit einem CDATA-Block werden die "<"-Zeichen etc. escaped.
        String dateString = sdf.format(new Date());
        iomObj.setattrvalue("lastPublishingDate", dateString);
        iomObj.setattrvalue("licence", licence);
        if (furtherInformation!=null) iomObj.setattrvalue("furtherInformation", furtherInformation);            
        if (keywords!=null) iomObj.setattrvalue("keywords", keywords);
        if (synonyms!=null) iomObj.setattrvalue("synonyms", synonyms);
        
        IomObject servicerIomObject = getIomObjectById(servicer, CORE_DATA_OFFICES, themeRootDirectory.getAbsolutePath());
        IomObject ownerIomObject = getIomObjectById(owner, CORE_DATA_OFFICES, themeRootDirectory.getAbsolutePath());

        if (servicerIomObject!=null) {
            convertIomObjectToStructure(servicerIomObject, OFFICE_STRUCTURE_TAG); 
            iomObj.addattrobj("servicer", servicerIomObject);
        }

        if (ownerIomObject!=null) {
            convertIomObjectToStructure(ownerIomObject, OFFICE_STRUCTURE_TAG); 
            iomObj.addattrobj("owner", ownerIomObject);   
        }

        Iom_jObject bboxObj = new Iom_jObject(BOUNDARY_STRUCTURE_TAG, null);
        if (regions == null && !staticRegionsFile) {
            // Dann machen wir es uns relativ einfach. // FIXME: schoenere Koordinaten
            bboxObj.setattrvalue("westlimit", "2593499");
            bboxObj.setattrvalue("southlimit", "1214279");
            bboxObj.setattrvalue("eastlimit", "2644299");
            bboxObj.setattrvalue("northlimit", "1260845");
        } else {
            File jsonFile = Paths.get(targetConfigPath.toFile().getAbsolutePath(), themePublication + ".json").toFile();
            Map<String,Double> boundary = new HashMap<String,Double>();
            RegionsUtil.getBoundary(jsonFile, boundary);
            
            bboxObj.setattrvalue("westlimit", String.valueOf(boundary.get("westlimit")));
            bboxObj.setattrvalue("southlimit", String.valueOf(boundary.get("southlimit")));
            bboxObj.setattrvalue("eastlimit", String.valueOf(boundary.get("eastlimit")));
            bboxObj.setattrvalue("northlimit", String.valueOf(boundary.get("northlimit")));
        }
        iomObj.addattrobj("boundary", bboxObj);
        
        if (regions != null || staticRegionsFile) {
            File jsonFile = Paths.get(targetConfigPath.toFile().getAbsolutePath(), themePublication + ".json").toFile();
            List<IomObject> items = new ArrayList<IomObject>();
            RegionsUtil.getItems(jsonFile, items);
            
            for (IomObject item : items) {
                iomObj.addattrobj("items", item);
            }
        } else {
            Iom_jObject itemObj = new Iom_jObject(ITEM_STRUCTURE_TAG, null);
            itemObj.setattrvalue("identifier", "SO");
            itemObj.setattrvalue("title", "Kanton Solothurn");
            itemObj.setattrvalue("lastPublishingDate", dateString);
            iomObj.addattrobj("items", itemObj);
        }
        
        List<IomObject> formatIomObjects = new ArrayList<>();
        for (Object format : formats) {
            IomObject formatObj = getIomObjectById(format.toString(), CORE_DATA_FILEFORMATS, themeRootDirectory.getAbsolutePath());
            convertIomObjectToStructure(formatObj, FILEFORMAT_STRUCTURE_TAG);
            formatIomObjects.add(formatObj);
        }

        for (IomObject formatStructure : formatIomObjects) {
            iomObj.addattrobj("fileFormats", formatStructure);
        }
        
        if (gretlEnvironment.equalsIgnoreCase("production")) {
            iomObj.setattrvalue("downloadHostUrl", "https://files.geo.so.ch");
            iomObj.setattrvalue("appHostUrl", "https://data.geo.so.ch");     
        } else if (gretlEnvironment.equalsIgnoreCase("integration"))  {
            iomObj.setattrvalue("downloadHostUrl", "https://files-i.geo.so.ch");
            iomObj.setattrvalue("appHostUrl", "https://data-i.geo.so.ch");     
        } else {
            iomObj.setattrvalue("downloadHostUrl", "https://files-t.geo.so.ch");
            iomObj.setattrvalue("appHostUrl", "https://data-t.geo.so.ch");     
        }
        
        // TODO: add missing attributes etc. (??)
        
        if (printClassDescription && modelName != null) {
            for (Map.Entry<String, ClassDescription> entry : classDescriptions.entrySet()) {
                Iom_jObject classDescObj = new Iom_jObject(CLASS_DESCRIPTION_TAG, null); 

                ClassDescription classDescription = entry.getValue();

                classDescObj.setattrvalue("name", classDescription.getName());
                classDescObj.setattrvalue("title", classDescription.getTitle());
                if (classDescription.getDescription()!=null) classDescObj.setattrvalue("shortDescription", classDescription.getDescription());

                List<AttributeDescription> attributeDescriptions = classDescription.getAttributes();
                for (AttributeDescription attributeDescription : attributeDescriptions) {
                    Iom_jObject attributeDescObj = new Iom_jObject(ATTRIBUTE_DESCRIPTION_TAG, null); 

                    attributeDescObj.setattrvalue("name", attributeDescription.getName());
                    if (attributeDescription.getDescription()!=null) attributeDescObj.setattrvalue("shortDescription", attributeDescription.getDescription());
                    attributeDescObj.setattrvalue("dataType", attributeDescription.getDataType().name());
                    attributeDescObj.setattrvalue("isMandatory", attributeDescription.isMandatory()?"true":"false");
                    classDescObj.addattrobj("attributeDescription", attributeDescObj);
                }
                iomObj.addattrobj("classDescription", classDescObj);
            }            
        }
        
        ioxWriter.write(new ObjectEvent(iomObj));
        
        ioxWriter.write(new EndBasketEvent());
        ioxWriter.write(new EndTransferEvent());
        ioxWriter.flush();
        ioxWriter.close();    
        
        // (6) HTML-Datei aus XTF ableiten
        log.lifecycle("creating html file");
        
        File xsltFile = Paths.get(themeRootDirectory.getAbsolutePath(), SHARED_DIR_NAME, XSL_DIR_NAME, XSL_HTML_METADATA).toFile();
        
        Processor proc = new Processor(false);
        XsltCompiler comp = proc.newXsltCompiler();
        XsltExecutable exp = comp.compile(new StreamSource(xsltFile));
        
        XdmNode source = proc.newDocumentBuilder().build(new StreamSource(xtfFile));
        
        File outFile = Paths.get(targetPath.toFile().getAbsolutePath(), FilenameUtils.getBaseName(xtfFile.getName()) + ".html").toFile();
        Serializer outFileSerializer = proc.newSerializer(outFile);
        XsltTransformer trans = exp.load();
        trans.setInitialContextNode(source);
        trans.setDestination(outFileSerializer);
        trans.transform();
        trans.close();
        
        // (7) XTF in Config-Verzeichnis kopieren. Wird benoetigt, damit es fuer z.B. die Datensuche einfacher ist
        // an die notwendigen einzelnen Config-Dateien zu gelangen.
        Files.copy(xtfFile.toPath(), Paths.get(targetConfigPath.toFile().getAbsolutePath(), xtfFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                
        // (9) Geocat-XML erstellen
        if (geocatTarget != null) {
            String geocatFtpDir = "int";
            if (gretlEnvironment.equalsIgnoreCase("production")) {
                geocatFtpDir = "prod";
            } 
            
            String identifer = iomObj.getattrvalue("identifier");
            Path geocatLocalFile = targetConfigPath.resolve(identifier + ".xml");
            
            // Was nur lokal (beim Entwickeln) eintreffen sollte.
            if (!geocatTarget.resolve(geocatFtpDir).toFile().exists()) {
                Files.createDirectories(geocatTarget.resolve(geocatFtpDir));
            }
            
            Path geocatTargetFile = geocatTarget.resolve(geocatFtpDir).resolve(geocatLocalFile.toFile().getName());

            Geocat.export(iomObj, themeRootDirectory, geocatLocalFile);
            Files.copy(geocatLocalFile, geocatTargetFile, StandardCopyOption.REPLACE_EXISTING);   
        }
    }
    
    private TransferDescription getMetadataTransferdescription(File configRootDirectory) throws IOException, Ili2cFailure {        
        File iliFile = Paths.get(configRootDirectory.getAbsolutePath(), SHARED_DIR_NAME, ILI_DIR_NAME, ILI_MODEL_METADATA).toFile();

        ArrayList<String> filev = new ArrayList<String>() {{ add(iliFile.getAbsolutePath()); }};
        TransferDescription td = Ili2c.compileIliFiles(filev, null);

        if (td == null) {
            throw new IllegalArgumentException("INTERLIS compiler failed");
        }

        return td;
    }
    
    private IomObject getIomObjectById(String id, String coreDataFileName, String configRootDirectory) throws IOException, IoxException {
        File xtfFile = Paths.get(configRootDirectory, SHARED_DIR_NAME, CORE_DATA_DIR_NAME, coreDataFileName).toFile();
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

    private void convertIomObjectToStructure(IomObject officeObj, String tag) {
        officeObj.setobjecttag(tag);
        officeObj.setobjectoid(null);
    }
}
