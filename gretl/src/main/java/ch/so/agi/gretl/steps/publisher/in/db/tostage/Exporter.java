package ch.so.agi.gretl.steps.publisher.in.db.tostage;

import java.sql.Connection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.fromili.TransferFromIli;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgCustomStrategy;
import ch.ehi.ili2pg.PgMain;
import ch.interlis.iom_j.itf.ItfReader;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.ilirepository.IliFiles;

/**
 * Responsibility: encapsulate ili2db export configuration and execution when a
 * database publication part is exported as an XTF or ITF transfer file
 * and exported to the cache.
 */
public class Exporter {
    private final DataSelection selectionToExport;
    private final Connection conn;
    private final String dbSchema;
    private final boolean mergeToSingleXtf;

    public Exporter(DataSelection selectionToExport, Connection conn, String dbSchema, boolean mergeToSingleXtf) {
        this.selectionToExport = selectionToExport;
        this.conn = conn;
        this.dbSchema = dbSchema;
        this.mergeToSingleXtf = mergeToSingleXtf;
    }

    /**
     * Exports to the given directory returning the number of the exported objects.
     */
    public int export(Path exportDirectory) throws Exception {
        validateExportInputs(exportDirectory);

        boolean itfTransferFile = isItfTransferFile();
        if (mergeToSingleXtf) {
            Path exportFile = exportDirectory.resolve("export" + getTransferFileExtension(itfTransferFile));
            return exportSelection(selectionToExport.getKeyValues(), exportFile, itfTransferFile);
        }

        int objectCount = 0;
        Set<Path> exportFiles = new HashSet<Path>();
        for (String keyValue : selectionToExport.getKeyValues()) {
            Path exportFile = exportDirectory
                    .resolve(sanitizeFileName(keyValue) + getTransferFileExtension(itfTransferFile));
            if (!exportFiles.add(exportFile)) {
                throw new IllegalArgumentException("duplicate export file <" + exportFile + ">");
            }
            objectCount += exportSelection(Collections.singletonList(keyValue), exportFile, itfTransferFile);
        }
        return objectCount;
    }

    private int exportSelection(List<String> keyValues, Path exportFile, boolean itfTransferFile) throws Exception {
        Config config = createConfig();
        config.setXtffile(exportFile.toString());
        config.setModeldir(Ili2db.ILI_FROM_DB);
        config.setFunction(Config.FC_EXPORT);
        config.setValidation(false);
        config.setItfTransferfile(itfTransferFile);
        applySelection(config, keyValues);
        readSettingsFromDb(config);
        validateConfig(config);
        runIli2db(config);
        return countExportedObjects(exportFile, itfTransferFile);
    }

    private void applySelection(Config config, List<String> keyValues) {
        String joinedKeyValues = joinKeyValues(keyValues);
        switch (selectionToExport.getKeyType()) {
        case dataset:
            config.setDatasetName(joinedKeyValues);
            break;
        case model:
            config.setModels(joinedKeyValues);
            break;
        case topic:
            config.setTopics(joinedKeyValues);
            break;
        case basket:
            config.setBaskets(joinedKeyValues);
            break;
        default:
            throw new IllegalArgumentException("unsupported keyType <" + selectionToExport.getKeyType() + ">");
        }
    }

    private String joinKeyValues(List<String> keyValues) {
        return String.join(String.valueOf(ch.interlis.ili2c.Main.MODELS_SEPARATOR), keyValues);
    }

    private void validateExportInputs(Path exportDirectory) {
        if (selectionToExport == null) {
            throw new IllegalArgumentException("selectionToExport must be set");
        }
        selectionToExport.validateResolved();
        if (exportDirectory == null) {
            throw new IllegalArgumentException("exportDirectory must be set");
        }
        if (!Files.isDirectory(exportDirectory)) {
            throw new IllegalArgumentException("exportDirectory <" + exportDirectory + "> must be an existing directory");
        }
    }

    private void validateConfig(Config config) {
        if (DataSelection.KeyType.model.equals(selectionToExport.getKeyType())
                && Config.BASKET_HANDLING_READWRITE.equals(config.getBasketHandling())) {
            throw new IllegalArgumentException("models can only be used with simple models");
        }
    }

    private String getTransferFileExtension(boolean itfTransferFile) {
        return itfTransferFile ? ".itf" : ".xtf";
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    Config createConfig() {
        Config config = new Config();
        new PgMain().initConfig(config);
        config.setDbschema(dbSchema);
        config.setJdbcConnection(conn);
        return config;
    }

    boolean isItfTransferFile() throws Exception {
        Config config = createConfig();
        IliFiles iliFiles = TransferFromIli.readIliFiles(conn, config.getDbschema(), new PgCustomStrategy(),
                config.isVer3_export());
        ch.interlis.ili2c.modelscan.IliFile iliFile = iliFiles.iteratorFile().next();
        return iliFile.getIliVersion() < 2.0;
    }

    void readSettingsFromDb(Config config) throws Exception {
        Ili2db.readSettingsFromDb(config);
    }

    void runIli2db(Config config) throws Exception {
        Ili2db.run(config, null);
    }

    int countExportedObjects(Path exportFile, boolean itfTransferFile) throws Exception {
        IoxReader reader = itfTransferFile ? new ItfReader(exportFile.toFile()) : new XtfReader(exportFile.toFile());
        try {
            int objectCount = 0;
            IoxEvent event = reader.read();
            while (event != null) {
                if (event instanceof ObjectEvent) {
                    objectCount++;
                }
                event = reader.read();
            }
            return objectCount;
        } finally {
            reader.close();
        }
    }
}
