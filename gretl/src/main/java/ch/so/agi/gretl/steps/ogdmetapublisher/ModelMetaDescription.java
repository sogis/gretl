package ch.so.agi.gretl.steps.ogdmetapublisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cException;
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

public class ModelMetaDescription {
    
    public static Map<String, ClassDescription> getDescription(String modelName, File localRepo) throws IOException, Ili2cException {
        TransferDescription td = getTransferDescriptionFromModelName(modelName, localRepo.getAbsolutePath());
        
        Map<String, ClassDescription> classDescriptions = new HashMap<>();

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

                            ClassDescription classDescribtion = new ClassDescription();
                            classDescribtion.setName(table.getName());
                            classDescribtion.setTitle(table.getMetaValue("title"));
                            classDescribtion.setDescription(table.getDocumentation());
                            classDescribtion.setModelName(modelName);
                            classDescribtion.setTopicName(topic.getName());

                            Iterator<?> attri = table.getAttributes();

                            List<AttributeDescription> attributes = new ArrayList<>();
                            while (attri.hasNext()) {
                                Object aObj = attri.next();
                                AttributeDescription attributeDescription = new AttributeDescription();

                                if (aObj instanceof AttributeDef) {
                                    AttributeDef attr = (AttributeDef) aObj;
                                    attributeDescription.setName(attr.getName());                                    
                                    attributeDescription.setDescription(attr.getDocumentation());
                                    attributeDescription.setMandatory(attr.getDomain().isMandatory() ? true : false);

                                    Type type = attr.getDomainResolvingAll();
                                    if (type instanceof TextType) {
                                        TextType t = (TextType) type; 
                                        attributeDescription.setDataType(t.isNormalized() ? DataType.TEXT : DataType.MTEXT);
                                    } else if (type instanceof NumericType) {
                                        NumericType n = (NumericType) type;
                                        attributeDescription.setDataType(n.getMinimum().getAccuracy() == 0 ? DataType.INTEGER : DataType.DOUBLE);
                                    } else if (type instanceof EnumerationType) {
                                        EnumerationType e = (EnumerationType) type;
                                        // Wenn man selber BOOLEAN definiert muss man hier nachziehen. Dann muessen
                                        // wohl die Werte ausgelesen werden e.getEnumeration() 
                                        if (attr.isDomainBoolean()) {
                                            attributeDescription.setDataType(DataType.BOOLEAN);
                                        } else {
                                            attributeDescription.setDataType(DataType.ENUMERATION);
                                        }
                                    } else if (type instanceof FormattedType) {
                                        FormattedType f = (FormattedType) type;
                                        String format = f.getFormat();
                                        if (format.contains("Year") && !format.contains("Hours")) {
                                            attributeDescription.setDataType(DataType.DATE);
                                        } else if (format.contains("Year") && format.contains("Hours")) {
                                            attributeDescription.setDataType(DataType.DATETIME);
                                        }
                                        // else if...
                                    } 
                                }
                                attributes.add(attributeDescription);
                            }
                            classDescribtion.setAttributes(attributes);
                            classDescriptions.put(classDescribtion.getQualifiedName(), classDescribtion);
                        } // else if... DOMAIN, etc.? DOMAIN nur, falls ich die Werte wirklich ausweisen will.
                    }
                }
            }
        }
        
        //if (override) overrideModelDescription(classDescriptions, metaTomlResult);

        return classDescriptions;
    }

    private static TransferDescription getTransferDescriptionFromModelName(String modelName, String localRepo) throws  IOException, Ili2cException {
        IliManager manager = new IliManager();
        File ilicacheFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), ".ilicache_").toFile();        
        manager.setCache(ilicacheFolder);
        String repositories[] = new String[] { localRepo, "https://geo.so.ch/models", "http://models.interlis.ch/" };
        manager.setRepositories(repositories);
        ArrayList<String> modelNames = new ArrayList<String>();
        modelNames.add(modelName);
        Configuration config;
        config = manager.getConfig(modelNames, 2.3);
        TransferDescription td = Ili2c.runCompiler(config);

        if (td == null) {
            throw new IllegalArgumentException("INTERLIS compiler failed"); 
        }
        
        return td;
    }
}