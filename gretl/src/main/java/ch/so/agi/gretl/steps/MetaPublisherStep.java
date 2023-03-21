package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import ch.ehi.ili2db.base.Ili2cUtility;
import ch.ehi.ili2db.metaattr.IliMetaAttrNames;
import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.CompositionType;
import ch.interlis.ili2c.metamodel.CoordType;
import ch.interlis.ili2c.metamodel.Domain;
import ch.interlis.ili2c.metamodel.Element;
import ch.interlis.ili2c.metamodel.EnumerationType;
import ch.interlis.ili2c.metamodel.FormattedType;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.NumericType;
import ch.interlis.ili2c.metamodel.PolylineType;
import ch.interlis.ili2c.metamodel.SurfaceOrAreaType;
import ch.interlis.ili2c.metamodel.Table;
import ch.interlis.ili2c.metamodel.TextType;
import ch.interlis.ili2c.metamodel.Topic;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Type;
import ch.interlis.ilirepository.IliManager;
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
import ch.so.agi.gretl.util.metapublisher.AttributeDescription;
import ch.so.agi.gretl.util.metapublisher.ClassDescription;
import ch.so.agi.gretl.util.metapublisher.DataType;
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
        add("basic");
        add("formats");
    }};
    
    private static final String ILI_MODEL_METADATA = "SO_AGI_Metadata_20230304.ili";
    private static final String XSL_HTML_METADATA = "xtf2html.xsl";

    private static final String ILI_TOPIC = "SO_AGI_Metadata_20230304.ThemePublications";
    private static final String BID = "SO_AGI_Metadata_20230304.ThemePublications";
    private static final String TAG = "SO_AGI_Metadata_20230304.ThemePublications.ThemePublication";
    private static final String CLASS_DESCRIPTION_TAG = "SO_AGI_Metadata_20230304.ClassDescription";
    private static final String ATTRIBUTE_DESCRIPTION_TAG = "SO_AGI_Metadata_20230304.AttributeDescription";
    private static final String OFFICE_STRUCTURE_TAG = "SO_AGI_Metadata_20230304.Office_";
    
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
    
    public void execute(File themeRootDirectory, String themePublication, Path target) throws IOException, IoxException, Ili2cException, SaxonApiException { 
        execute(themeRootDirectory, themePublication, target, null);
    }

    // (1) Ordner im Zielverzeichnis erstellen.
    // (2) Modellnamen aus Toml-Datei lesen.
    // (3) Zuerst wird das Modell geparsed.
    // (4) Anschliessend die Toml-Datei mit zusaetzlichen Informationen lesen und ggf. 
    // die aus (1) eruierten Infos ueberschreiben.
    // (5) XTF schreiben
    // (6) HTML aus XTF ableiten
    // (7) XTF in spezielle config-Verzeichnis im Zielverzeichnis kopieren
        
    /**
     * 
     * @param themeRootDirectory the root directory of the theme
     * @param themePublication the ident of the publication that is published (= dataIdent of PublisherTask)
     * @param target the target root directory where the meta/config files will be saved
     * @throws IOException
     * @throws IoxException
     * @throws Ili2cException
     * @throws SaxonApiException
     */
    public void execute(File themeRootDirectory, String themePublication, Path target, List<String> regions) throws IOException, IoxException, Ili2cException, SaxonApiException { 
        log.lifecycle(String.format("Start MetaPublisherStep(Name: %s themeRootDirectory: %s themePublication: %s target: %s)", taskName, themeRootDirectory, themePublication, target));
        
        // (1) Ordner erstellen im Zielverzeichnis, falls er nicht existiert.
        Path targetRootPath = target;
        Path targetPath = targetRootPath.resolve(themePublication).resolve(PATH_ELE_AKTUELL).resolve(PATH_ELE_META);

        log.lifecycle("create <"+targetPath.toString()+">...");
        Files.createDirectories(targetPath);
        
        File tomlFile = Paths.get(themeRootDirectory.getAbsolutePath(), PUBLICATION_DIR_NAME, themePublication, "meta.toml").toFile();

        TomlParseResult metaTomlResult = Toml.parse(tomlFile.toPath());

        // (2) Modellnamen wird auch fuer (1) benoetigt.
        String modelName = metaTomlResult.getString("basic.model");
        System.out.println("model: " + modelName);
        
        // TODO: if model == null

        // (3) Informationen aus ILI-Modell lesen.
        Map<String, ClassDescription> classDescriptions = getModelDescription(modelName, themeRootDirectory.getAbsolutePath());
    
        // (4) Weitere Informationen aus Toml-Datei lesen und ggf. Modellinformationen uebersteuern.
        String identifier = metaTomlResult.getString("basic.identifier");
        String title = metaTomlResult.getString("basic.title");
        String description = metaTomlResult.getString("basic.description");
        String keywords = metaTomlResult.getString("basic.keywords");
        String synonyms = metaTomlResult.getString("basic.synonyms");
        String owner = metaTomlResult.getString("basic.owner");
        String servicer = metaTomlResult.getString("basic.servicer");
        String licence = metaTomlResult.getString("basic.licence");
        String furtherInformation = metaTomlResult.getString("basic.furtherInformation");
        boolean printClassDescription = metaTomlResult.getBoolean("config.printClassDescription", () -> true);

        if (printClassDescription) overrideModelDescription(classDescriptions, metaTomlResult);

        IomObject servicerIomObject = getOfficeById(servicer, themeRootDirectory.getAbsolutePath());
        IomObject ownerIomObject = getOfficeById(owner, themeRootDirectory.getAbsolutePath());

        // (5) XTF schreiben.
        log.lifecycle("writing xtf file");
        File xtfFile = Paths.get(targetPath.toFile().getAbsolutePath(), "meta-"+identifier+".xtf").toFile();
        IoxWriter ioxWriter = createMetaIoxWriter(themeRootDirectory, xtfFile);
        ioxWriter.write(new StartTransferEvent("SOGIS-20230305", "", null));
        ioxWriter.write(new StartBasketEvent(ILI_TOPIC,BID));

        Iom_jObject iomObj = new Iom_jObject(TAG, String.valueOf(1));
        iomObj.setattrvalue("identifier", identifier);
        
        Iom_jObject modelObj = new Iom_jObject("SO_AGI_Metadata_20230304.ModelLink", null); 
        modelObj.setattrvalue("name", modelName);
        modelObj.setattrvalue("locationHint", "https://geo.so.ch/models");
        iomObj.addattrobj("model", modelObj);
        
        iomObj.setattrvalue("title", title);
        if (description!=null) iomObj.setattrvalue("shortDescription", description); // CDATA wird nicht beruecksichtigt, d.h. auch mit einem CDATA-Block werden die "<"-Zeichen etc. escaped.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(new Date());
        iomObj.setattrvalue("lastPublishingDate", dateString);
        iomObj.setattrvalue("licence", licence);
        if (furtherInformation!=null) iomObj.setattrvalue("furtherInformation", furtherInformation);            
        if (keywords!=null) iomObj.setattrvalue("keywords", keywords);
        if (synonyms!=null) iomObj.setattrvalue("synonyms", synonyms);
        
        if (servicerIomObject!=null) {
            convertOfficeToStructure(servicerIomObject); 
            iomObj.addattrobj("servicer", servicerIomObject);
        }

        if (ownerIomObject!=null) {
            convertOfficeToStructure(ownerIomObject); 
            iomObj.addattrobj("owner", ownerIomObject);   
        }

        // TODO: add missing attributes etc. (??)
        
        if (printClassDescription) {
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
        Path targetConfigPath = targetRootPath.resolve(PATH_ELE_CONFIG);
        log.lifecycle("create <"+targetConfigPath.toString()+">...");
        Files.createDirectories(targetConfigPath);
        Files.copy(xtfFile.toPath(), Paths.get(targetConfigPath.toFile().getAbsolutePath(), xtfFile.getName()), StandardCopyOption.REPLACE_EXISTING);
        
        // (8) GeoJSON-Datei nachfuehren zwecks Publikationsdatum einzelner Regionen.
        // Weil Publisher den Inhalt des meta-Verzeichnisses loescht, ist der Master
        // im config-Verzeichnis.
        if (regions != null) {
            File geojsonFile = Paths.get(targetConfigPath.toFile().getAbsolutePath(), themePublication + ".json").toFile();
            if (!geojsonFile.exists()) {
                File sourceGeojsonFile = Paths.get(themeRootDirectory.getAbsolutePath(), PUBLICATION_DIR_NAME, themePublication, themePublication + ".json").toFile();
                Files.copy(sourceGeojsonFile.toPath(), geojsonFile.toPath());
            }       
            
            
        }
    }

    private IoxWriter createMetaIoxWriter(File themeRootDirectory, File dataFile) throws IOException, Ili2cFailure, IoxException {
        TransferDescription td = getMetadataTransferdescription(themeRootDirectory);
        IoxWriter ioxWriter = new XtfWriter(dataFile, td);
        return ioxWriter;
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
    
    private IomObject getOfficeById(String id, String configRootDirectory) throws IOException, IoxException {
        File xtfFile = Paths.get(configRootDirectory, SHARED_DIR_NAME, CORE_DATA_DIR_NAME, "offices.xtf").toFile();
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

    private void convertOfficeToStructure(IomObject officeObj) {
        officeObj.setobjecttag(OFFICE_STRUCTURE_TAG);
        officeObj.setobjectoid(null);
    }
    
    private void overrideModelDescription(Map<String, ClassDescription> classDescriptions, TomlParseResult metaTomlResult) {
        Map<String, Object> metaTomlMap = metaTomlResult.toMap();
        for (Map.Entry<String, Object> entry : metaTomlMap.entrySet()) {
            if (!META_TOML_CONFIG_SECTIONS.contains(entry.getKey())) {
                String modelName = (String) entry.getKey();
                parseTopicDesc(classDescriptions, modelName, (TomlTable) entry.getValue());
            }      
        }
    }
    
    private void parseTopicDesc(Map<String, ClassDescription> classDescriptions, String modelName, TomlTable topicDescs) {
        for (Map.Entry<String, Object> entry : topicDescs.entrySet()) {
            String topicName = (String) entry.getKey();
            parseClassDesc(classDescriptions, modelName, topicName, (TomlTable) entry.getValue());   
        }
    }
    
    private void parseClassDesc(Map<String, ClassDescription> classDescriptions, String modelName, String topicName, TomlTable classDescs) {
        for (Map.Entry<String, Object> entry : classDescs.entrySet()) {
            String className = (String) entry.getKey();
            
            TomlTable classDesc = (TomlTable) entry.getValue();
            String title = classDesc.getString("title");
            String description = classDesc.getString("description");

            String qualifiedClassName = modelName + "." + topicName + "." + className;
//            System.out.println("qualifiedClassName: " + qualifiedClassName);
//            System.out.println("className: " + className);
//            System.out.println("title: " + title);
//            System.out.println("description: " + description);
            
            ClassDescription classDescription = classDescriptions.get(qualifiedClassName);
//            System.out.println(classDescription);

            if (classDescription != null) {
                if (title != null) {
                    classDescription.setTitle(title);
                } 

                if (description != null) {
                    classDescription.setDescription(description);
                }
            }               
        }
    }
    
    private Map<String, ClassDescription> getModelDescription(String modelName, String themeRootDirectory) throws Ili2cException, IOException {
        String localRepo = Paths.get(themeRootDirectory, ILI_DIR_NAME).toFile().getAbsolutePath();
        
        TransferDescription td = getTransferDescriptionFromModelName(modelName, localRepo);
        
        Map<String, ClassDescription> classTypes = new HashMap<>();

        for (Model model : td.getModelsFromLastFile()) {
            if (!model.getName().equalsIgnoreCase(modelName)) {
                continue;
            }

            Iterator<Element> modeli = model.iterator();
            while (modeli.hasNext()) {
                Object tObj = modeli.next();

                if (tObj instanceof Domain) {
                    // Falls ich die Werte will.
                } else if (tObj instanceof Table) {
                    Table tableObj = (Table) tObj;
                    // https://github.com/claeis/ili2c/blob/ccb1331428/ili2c-core/src/main/java/ch/interlis/ili2c/metamodel/Table.java#L30
                    if (tableObj.isIdentifiable()) {
                        // Abstrakte Klasse:
                        // Kann ich ignorieren, da alles was ich wissen will (? Attribute und
                        // Beschreibung) in den spezialisieren Klassen vorhanden ist.
                    } else {
                        // Struktur:
                        // Momentan nicht von Interesse.
                    }
                } else if (tObj instanceof Topic) {
                    Topic topic = (Topic) tObj;
                    Iterator<?> iter = topic.getViewables().iterator();

                    while (iter.hasNext()) {
                        Object obj = iter.next();

                        // Viewable waere "alles". Was ist sinnvoll/notwendig fuer unseren Usecase?
                        // Domains?
                        // Momentan nur Table beruecksichtigen.
                        if (obj instanceof Table) {
                            Table table = (Table) obj;

                            // Abstrakte Klasse oder Struktur:
                            // Abstrakte Klasse interessiert uns nicht, da alle
                            // Attribute in der spezialisierte Klass vorhanden sind.
                            // Struktur interessiert uns vielleicht spaeter aber
                            // zum jetzigen Zeitpunkt brauchen wir dieses Wissen
                            // nicht.
                            if (table.isAbstract() || !table.isIdentifiable()) {
                                continue;
                            }

                            ClassDescription classType = new ClassDescription();
                            classType.setName(table.getName());
                            classType.setTitle(table.getMetaValue("title"));
                            classType.setDescription(table.getDocumentation());
                            classType.setModelName(modelName);
                            classType.setTopicName(topic.getName());

                            Iterator<?> attri = table.getAttributes();

                            List<AttributeDescription> attributes = new ArrayList<>();
                            while (attri.hasNext()) {
                                Object aObj = attri.next();
                                AttributeDescription attributeType = new AttributeDescription();

                                if (aObj instanceof AttributeDef) {
                                    AttributeDef attr = (AttributeDef) aObj;
                                    attributeType.setName(attr.getName());
                                    attributeType.setDescription(attr.getDocumentation());

                                    Type type = attr.getDomainResolvingAll();
                                    attributeType.setMandatory(type.isMandatory() ? true : false);

                                    if (type instanceof TextType) {
                                        TextType t = (TextType) type; 
                                        attributeType.setDataType(t.isNormalized() ? DataType.TEXT : DataType.MTEXT);
                                    } else if (type instanceof NumericType) {
                                        NumericType n = (NumericType) type;
                                        attributeType.setDataType(n.getMinimum().getAccuracy() == 0 ? DataType.INTEGER : DataType.DOUBLE);
                                    } else if (type instanceof EnumerationType) {
                                        EnumerationType e = (EnumerationType) type;
                                        // Wenn man selber BOOLEAN definiert muss man hier nachziehen. Dann muessen
                                        // wohl die Werte ausgelesen werden e.getEnumeration() 
                                        if (attr.isDomainBoolean()) {
                                            attributeType.setDataType(DataType.BOOLEAN);
                                        } else {
                                            attributeType.setDataType(DataType.ENUMERATION);
                                        }
                                    } else if (type instanceof SurfaceOrAreaType) {
                                        SurfaceOrAreaType s = (SurfaceOrAreaType) type;
                                        attributeType.setDataType(DataType.POLYGON);
                                    } else if (type instanceof PolylineType) {
                                        PolylineType p = (PolylineType) type;
                                        attributeType.setDataType(DataType.LINESTRING);
                                    } else if (type instanceof CoordType) {
                                        CoordType c = (CoordType) type;
                                        attributeType.setDataType(DataType.POINT);
                                    } else if (type instanceof FormattedType) {
                                        FormattedType f = (FormattedType) type;
                                        String format = f.getFormat();
                                        if (format.contains("Year") && !format.contains("Hours")) {
                                            attributeType.setDataType(DataType.DATE);
                                        } else if (format.contains("Year") && format.contains("Hours")) {
                                            attributeType.setDataType(DataType.DATETIME);
                                        }
                                        // else if...
                                    } else if (type instanceof CompositionType) {
                                        CompositionType c = (CompositionType) type;

                                        if (attr.getMetaValue(IliMetaAttrNames.METAATTR_MAPPING)!= null && attr.getMetaValue(IliMetaAttrNames.METAATTR_MAPPING).equals(IliMetaAttrNames.METAATTR_MAPPING_JSON)) {
                                            attributeType.setDataType(DataType.JSON_TEXT);
                                        } else {
                                            Table struct = c.getComponentType();

                                            // Wenn es keine richtigen Multigeometrie-Datentypen
                                            // gibt, geht es nicht 100% robust.
                                            if (c.getCardinality().getMaximum() != 1) {
                                                attributeType.setDataType(DataType.UNDEFINED); // oder DataType.STRUCTURE ?
                                            } else {
                                                if (Ili2cUtility.isPureChbaseMultiSuface(td, attr)) {
                                                    attributeType.setDataType(DataType.MULTIPOLYGON);
                                                } else if (Ili2cUtility.isPureChbaseMultiLine(td, attr)) {
                                                    attributeType.setDataType(DataType.MULTILINESTRING);
                                                } else {
                                                    String metaValue = struct.getMetaValue(IliMetaAttrNames.METAATTR_MAPPING);

                                                    if (metaValue == null) {
                                                        attributeType.setDataType(DataType.UNDEFINED);
                                                    } else if (metaValue
                                                            .equals(IliMetaAttrNames.METAATTR_MAPPING_MULTISURFACE)) {
                                                        attributeType.setDataType(DataType.MULTIPOLYGON);
                                                    } else if (metaValue
                                                            .equals(IliMetaAttrNames.METAATTR_MAPPING_MULTILINE)) {
                                                        attributeType.setDataType(DataType.MULTILINESTRING);
                                                    } else if (metaValue
                                                            .equals(IliMetaAttrNames.METAATTR_MAPPING_MULTIPOINT)) {
                                                        attributeType.setDataType(DataType.MULTIPOINT);
                                                    } else {
                                                        attributeType.setDataType(DataType.UNDEFINED);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                attributes.add(attributeType);
                            }
                            classType.setAttributes(attributes);
                            classTypes.put(classType.getQualifiedName(), classType);
                        } // else if... DOMAIN, etc.? DOMAIN nur, falls ich die Werte wirklich ausweisen will.
                    }
                }
            }
        }
        return classTypes;
    }

    private TransferDescription getTransferDescriptionFromModelName(String modelName, String localRepo) throws  IOException, Ili2cException {
        IliManager manager = new IliManager();
        File ilicacheFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), ".ilicache_").toFile();        
        manager.setCache(ilicacheFolder);
        String repositories[] = new String[] { localRepo, "http://models.interlis.ch/" };
        manager.setRepositories(repositories);
        ArrayList<String> modelNames = new ArrayList<String>();
        modelNames.add(modelName);
        Configuration config;
        try {
            config = manager.getConfig(modelNames, 2.3);
        } catch (Ili2cException e) {
            config = manager.getConfig(modelNames, 1.0); // bit of a hack
        }
        
        TransferDescription td = Ili2c.runCompiler(config);

        if (td == null) {
            throw new IllegalArgumentException("INTERLIS compiler failed"); 
        }
        
        return td;
    }
}
