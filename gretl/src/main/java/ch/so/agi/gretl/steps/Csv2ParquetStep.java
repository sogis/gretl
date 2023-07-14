package ch.so.agi.gretl.steps;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.security.UserGroupInformation;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ilirepository.IliManager;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.iox.IoxException;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.interlis.ioxwkf.parquet.ParquetAttributeDescriptor;
import ch.interlis.ioxwkf.parquet.ParquetWriter;
import ch.interlis.ili2c.config.Configuration;
import org.interlis2.validator.Validator;

import ch.interlis.iox.IoxEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.ObjectEvent;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class Csv2ParquetStep {
    private GretlLogger log;
    private String taskName;

    public Csv2ParquetStep() {
        this(null);
        
        // https://stackoverflow.com/questions/41864985/hadoop-ioexception-failure-to-login
        UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("hduser"));
    }
    
    public Csv2ParquetStep(String taskName) {
        if (taskName == null) {
            this.taskName = Csv2ParquetStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    public void execute(Path csvPath, Path outputPath, Settings config) throws IOException {
        log.lifecycle(String.format("Start Csv2ParquetStep(Name: %s csvPath: %s outputPath: %s config: %s)", taskName,
                csvPath, outputPath, config));

        String csvBaseName = FilenameUtils.getBaseName(csvPath.getFileName().toString());
        ParquetWriter writer = null;
        try {
            writer = new ParquetWriter(Paths.get(outputPath.toString(), csvBaseName + ".parquet").toFile());
        } catch (IoxException e) {
            throw new IOException(e.getMessage());
        }
        
        CsvReader reader = null;
        try {
            reader = new CsvReader(csvPath.toFile(), config); // config notwendig, wegen encoding, das im Reader gesetzt wird.
        } catch (IoxException e) {
            throw new IOException(e.getMessage());
        }
        
        boolean firstLineIsHeader = true;
        if(config.getValue(IoxWkfConfig.SETTING_FIRSTLINE) != null) {
            firstLineIsHeader=config.getValue(IoxWkfConfig.SETTING_FIRSTLINE).equals(IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER);
        } 
        reader.setFirstLineIsHeader(firstLineIsHeader);
        log.lifecycle("first line is "+(firstLineIsHeader?"header":"data"));

        String valueDelimiter=config.getValue(IoxWkfConfig.SETTING_VALUEDELIMITER);
        if(valueDelimiter != null) {
            reader.setValueDelimiter(valueDelimiter.charAt(0));
            log.lifecycle("valueDelimiter <"+valueDelimiter+">.");
        } 

        String valueSeparator=config.getValue(IoxWkfConfig.SETTING_VALUESEPARATOR);
        if(valueSeparator != null) {
            reader.setValueSeparator(valueSeparator.charAt(0));
            log.lifecycle("valueSeparator <"+valueSeparator+">.");
        } else {
            reader.setValueSeparator(IoxWkfConfig.SETTING_VALUESEPARATOR_DEFAULT);
            log.lifecycle("valueSeparator <"+IoxWkfConfig.SETTING_VALUESEPARATOR_DEFAULT+">.");
        }
                
        if (config.getValue(Validator.SETTING_MODELNAMES) != null) {
            TransferDescription td = null;
            try {
                td = getTransferDescriptionFromModelName(config.getValue(Validator.SETTING_MODELNAMES), csvPath.getParent());
            } catch (Ili2cException e) {
                throw new IOException(e.getMessage());
            }
            reader.setModel(td);
            writer.setModel(td);
        }
        
        String[] attrs = null;

        try {
            IoxEvent event = reader.read();

            while (event instanceof IoxEvent) {
                //event = reader.read();
                if (event instanceof ObjectEvent) {
                    if (attrs == null) {
                        attrs = reader.getAttributes();
                        
                        // Funktioniert, falls die Reihenfolge garantiert ist.
                        // Man muss die Attribute explizit setzen. Sonst kann 
                        // passieren, dass Attribute komplett fehlen, weil das
                        // erste Objekt analysiert wird und einige Attribute davon
                        // null sind und im IomObjekt nicht vorkommen.
                        // Und funktioniert nur, falls Header-Zeile vorhanden.
                        if (firstLineIsHeader && config.getValue(Validator.SETTING_MODELNAMES) == null) {
                            List<ParquetAttributeDescriptor> attrDescs = new ArrayList<>();
                            for(String attrName : attrs) {                        
                                ParquetAttributeDescriptor attrDesc = new ParquetAttributeDescriptor();
                                attrDesc.setAttributeName(attrName);
                                attrDesc.setBinding(String.class);
                                attrDescs.add(attrDesc);
                            }
                            writer.setAttributeDescriptors(attrDescs);                        
                        }
                    }                    
                    writer.write(event);
                }
                event = reader.read();
            }

            writer.write(new EndBasketEvent());
            writer.write(new EndTransferEvent());

            if (writer != null) {
                writer.close();
                writer = null;
            }
            
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IoxException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }
        
    private TransferDescription getTransferDescriptionFromModelName(String iliModelName, Path additionalRepository) throws Ili2cException {
        IliManager manager = new IliManager();        
        String ilidirs = IoxWkfConfig.SETTING_ILIDIRS_DEFAULT + ";" + additionalRepository;
        String repositories[] = ilidirs.split(";");
        manager.setRepositories(repositories);
        ArrayList<String> modelNames = new ArrayList<String>();
        modelNames.add(iliModelName);
        Configuration config = manager.getConfig(modelNames, 2.3);
        TransferDescription td = Ili2c.runCompiler(config);

        if (td == null) {
            throw new IllegalArgumentException("INTERLIS compiler failed"); // TODO: can this be tested?
        }
        
        return td;
    }
}
