package ch.so.agi.gretl.steps.publisher.in.db;

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
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.PublisherIli2dbRunner;

/**
 * Responsibility: encapsulate ili2db export configuration and execution when a
 * database publication part is exported as an XTF or ITF transfer file
 * and exported to the cache.
 */
public class Exporter implements Operation {
    private ExporterParameters parameters;
    private int exportedObjectCount;

    public Exporter(ExporterParameters parameters) {
        this.parameters = java.util.Objects.requireNonNull(parameters, "parameters must not be null");
    }

    /** @deprecated Use {@link #Exporter(ExporterParameters)}. */
    @Deprecated protected Exporter() { }

    /** @deprecated Use constructor-owned parameters and {@link #execute()}. */
    @Deprecated public void execute(ExporterParameters parameters) throws Exception { this.parameters = parameters; execute(); }

    @Override
    public void execute() throws Exception {
        exportedObjectCount = export(parameters);
    }

    @Override
    public String getHumanReadableName() {
        return "Database selection export";
    }

    @Override
    public String getSuccessLogDetail() {
        return "exported " + exportedObjectCount + " object(s) for "
                + parameters.getSelectionToExport().getKeyValues() + " from schema " + parameters.getDbSchema();
    }

    /**
     * Exports to the given directory returning the number of the exported objects.
     */
    int export(ExporterParameters operationParameters) throws Exception {
        validateExportInputs(operationParameters);

        boolean itfTransferFile = isItfTransferFile(operationParameters);
        if (operationParameters.isMergeToSingleXtf()) {
            Path exportFile = operationParameters.getExportDirectory().resolve("export" + getTransferFileExtension(itfTransferFile));
            return exportSelection(operationParameters, operationParameters.getSelectionToExport().getKeyValues(), exportFile,
                    itfTransferFile);
        }

        int objectCount = 0;
        Set<Path> exportFiles = new HashSet<Path>();
        for (String keyValue : operationParameters.getSelectionToExport().getKeyValues()) {
            Path exportFile = operationParameters.getExportDirectory()
                    .resolve(sanitizeFileName(keyValue) + getTransferFileExtension(itfTransferFile));
            if (!exportFiles.add(exportFile)) {
                throw new IllegalArgumentException("duplicate export file <" + exportFile + ">");
            }
            objectCount += exportSelection(operationParameters, Collections.singletonList(keyValue), exportFile,
                    itfTransferFile);
        }
        return objectCount;
    }

    private int exportSelection(ExporterParameters operationParameters, List<String> keyValues, Path exportFile,
            boolean itfTransferFile) throws Exception {
        Config config = createConfig(operationParameters);
        config.setXtffile(exportFile.toString());
        config.setModeldir(Ili2db.ILI_FROM_DB);
        config.setFunction(Config.FC_EXPORT);
        config.setValidation(false);
        config.setItfTransferfile(itfTransferFile);
        applySelection(operationParameters, config, keyValues);
        readSettingsFromDb(operationParameters, config);
        validateConfig(operationParameters, config);
        runIli2db(config, operationParameters.getIli2dbWorkDirectory());
        return countExportedObjects(exportFile, itfTransferFile);
    }

    private void applySelection(ExporterParameters operationParameters, Config config, List<String> keyValues) {
        String joinedKeyValues = joinKeyValues(keyValues);
        switch (operationParameters.getSelectionToExport().getKeyType()) {
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
            throw new IllegalArgumentException(
                    "unsupported keyType <" + operationParameters.getSelectionToExport().getKeyType() + ">");
        }
    }

    private String joinKeyValues(List<String> keyValues) {
        return String.join(String.valueOf(ch.interlis.ili2c.Main.MODELS_SEPARATOR), keyValues);
    }

    private void validateExportInputs(ExporterParameters operationParameters) throws java.io.IOException {
        operationParameters.getSelectionToExport().validateResolved();
        Path exportDirectory = operationParameters.getExportDirectory();
        if (Files.exists(exportDirectory) && !Files.isDirectory(exportDirectory)) {
            throw new IllegalArgumentException(
                    "exportDirectory <" + exportDirectory + "> must be an existing directory");
        }
        Files.createDirectories(exportDirectory);
    }

    private void validateConfig(ExporterParameters operationParameters, Config config) {
        if (DataSelection.KeyType.model.equals(operationParameters.getSelectionToExport().getKeyType())
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

    Config createConfig(ExporterParameters operationParameters) {
        Config config = new Config();
        new PgMain().initConfig(config);
        config.setDbschema(operationParameters.getDbSchema());
        config.setJdbcConnection(operationParameters.getConnection());
        return config;
    }

    boolean isItfTransferFile(ExporterParameters operationParameters) throws Exception {
        Config config = createConfig(operationParameters);
        IliFiles iliFiles = TransferFromIli.readIliFiles(operationParameters.getConnection(), config.getDbschema(),
                new PgCustomStrategy(),
                config.isVer3_export());
        ch.interlis.ili2c.modelscan.IliFile iliFile = iliFiles.iteratorFile().next();
        return iliFile.getIliVersion() < 2.0;
    }

    void readSettingsFromDb(ExporterParameters operationParameters, Config config) throws Exception {
        Ili2db.readSettingsFromDb(config);
    }

    void runIli2db(Config config, Path workDirectory) throws Exception {
        PublisherIli2dbRunner.run(config, workDirectory);
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
