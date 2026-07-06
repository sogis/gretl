package ch.so.agi.gretl.steps.publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivedFormat;

/**
 * DTO representing the flat publisher inputs exactly as provided.
 */
class RawPublisherArgs {
    private final String dbDatabase;
    private final String dbSchema;
    private final String dbIliIdent_Type;
    private final List<String> dbIliIdent_Values;
    private final String dbIliIdent_RegEx;
    private final Boolean dbMergeToSingleXtf;
    private final String xtfFile_FolderPath;
    private final String xtfFilename_Regex;
    private final List<String> xtfFilename_List;
    private final Endpoint outBasePath;
    private final String outDataIdent;
    private final Object outValidationConfig;
    private final List<DerivedFormat> outDerivedFormats;
    private final Date depVersion;

    private PublishMode publishMode;
    private IliIdentType dbIliIdent_TypeEnum;

    enum PublishMode {
        dbIdentvaluesList,
        dbIdentvaluesRegex,
        xtfFilesList,
        xtfFilesRegex
    }

    enum IliIdentType {
        model,
        topic,
        basket,
        dataset
    }

    RawPublisherArgs(String dbDatabase, String dbSchema, String dbIliIdent_Type,
            ArrayList<String> dbIliIdent_Values, String dbIliIdent_RegEx, Boolean dbMergeToSingleXtf,
            String xtfFile_FolderPath, String xtfFilename_Regex, ArrayList<String> xtfFilename_List) {
        this(dbDatabase, dbSchema, dbIliIdent_Type, dbIliIdent_Values, dbIliIdent_RegEx, dbMergeToSingleXtf,
                xtfFile_FolderPath, xtfFilename_Regex, xtfFilename_List, null, null, null, null, null);
    }

    RawPublisherArgs(String dbDatabase, String dbSchema, String dbIliIdent_Type,
            ArrayList<String> dbIliIdent_Values, String dbIliIdent_RegEx, Boolean dbMergeToSingleXtf,
            String xtfFile_FolderPath, String xtfFilename_Regex, ArrayList<String> xtfFilename_List,
            List<DerivedFormat> outDerivedFormats) {
        this(dbDatabase, dbSchema, dbIliIdent_Type, dbIliIdent_Values, dbIliIdent_RegEx, dbMergeToSingleXtf,
                xtfFile_FolderPath, xtfFilename_Regex, xtfFilename_List, null, null, null, outDerivedFormats, null);
    }

    RawPublisherArgs(String dbDatabase, String dbSchema, String dbIliIdent_Type,
            ArrayList<String> dbIliIdent_Values, String dbIliIdent_RegEx, Boolean dbMergeToSingleXtf,
            String xtfFile_FolderPath, String xtfFilename_Regex, ArrayList<String> xtfFilename_List,
            Endpoint outBasePath, String outDataIdent, Object outValidationConfig,
            List<DerivedFormat> outDerivedFormats, Date depVersion) {
        this.dbDatabase = normalize(dbDatabase);
        this.dbSchema = normalize(dbSchema);
        this.dbIliIdent_Type = normalize(dbIliIdent_Type);
        this.dbIliIdent_Values = copyNormalized(dbIliIdent_Values);
        this.dbIliIdent_RegEx = normalize(dbIliIdent_RegEx);
        this.dbMergeToSingleXtf = dbMergeToSingleXtf;
        this.xtfFile_FolderPath = normalize(xtfFile_FolderPath);
        this.xtfFilename_Regex = normalize(xtfFilename_Regex);
        this.xtfFilename_List = copyNormalized(xtfFilename_List);
        this.outBasePath = outBasePath;
        this.outDataIdent = normalize(outDataIdent);
        this.outValidationConfig = outValidationConfig;
        this.outDerivedFormats = normalizeDerivedFormats(outDerivedFormats);
        this.depVersion = depVersion != null ? new Date(depVersion.getTime()) : new Date();

        validateArgumentCombination();
    }

    private void validateArgumentCombination() {
        validateOutputArgs();

        boolean sourceXtf = xtfFile_FolderPath != null;
        if (sourceXtf) {
            assertAllDbArgsNull();
            assertValidXtfArgs();
        } else {
            assertAllXtfArgsNull();
            assertValidDbArgs();
        }

        assignPublisherMode(sourceXtf);
    }

    private void validateOutputArgs() {
        List<String> missingArgs = new ArrayList<>();
        if (outBasePath == null) {
            missingArgs.add("outBasePath");
        }
        if (outDataIdent == null) {
            missingArgs.add("outDataIdent");
        }
        if (!missingArgs.isEmpty()) {
            throw new IllegalArgumentException("Missing mandatory arguments: " + String.join(", ", missingArgs));
        }
    }

    private void assignPublisherMode(boolean sourceIsXtf) {
        if (sourceIsXtf) {
            publishMode = xtfFilename_List.isEmpty() ? PublishMode.xtfFilesRegex : PublishMode.xtfFilesList;
            return;
        }
        publishMode = dbIliIdent_Values.isEmpty() ? PublishMode.dbIdentvaluesRegex : PublishMode.dbIdentvaluesList;
    }

    private void assertValidDbArgs() {
        List<String> missingArgs = new ArrayList<>();
        if (dbDatabase == null) {
            missingArgs.add("dbDatabase");
        }
        if (dbSchema == null) {
            missingArgs.add("dbSchema");
        }
        if (dbMergeToSingleXtf == null) {
            missingArgs.add("dbMergeToSingleXtf");
        }
        if (dbIliIdent_Type == null) {
            missingArgs.add("dbIliIdent_Type");
        }
        if (!missingArgs.isEmpty()) {
            throw new IllegalArgumentException("Missing mandatory arguments: " + String.join(", ", missingArgs));
        }

        assertDbEitherOr();
        assignIliIdentifierType();
    }

    private void assignIliIdentifierType() {
        try {
            dbIliIdent_TypeEnum = IliIdentType.valueOf(dbIliIdent_Type);
        } catch (Exception e) {
            String allowedValues = Arrays.stream(IliIdentType.values()).map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("dbIliIdent_Type must be one of " + allowedValues);
        }
    }

    private void assertDbEitherOr() {
        boolean hasRegex = dbIliIdent_RegEx != null;
        boolean hasValueList = !dbIliIdent_Values.isEmpty();
        if (hasRegex && hasValueList) {
            throw new IllegalArgumentException("Setting both dbIliIdent_RegEx and dbIliIdent_Values is invalid");
        }
        if (!hasRegex && !hasValueList) {
            throw new IllegalArgumentException("Either dbIliIdent_RegEx or dbIliIdent_Values must be set");
        }
    }

    private void assertValidXtfArgs() {
        if (xtfFile_FolderPath == null) {
            throw new IllegalArgumentException("Missing mandatory arguments: xtfFile_FolderPath");
        }
        assertXtfEitherOr();
    }

    private void assertXtfEitherOr() {
        boolean hasRegex = xtfFilename_Regex != null;
        boolean hasValueList = !xtfFilename_List.isEmpty();
        if (hasRegex && hasValueList) {
            throw new IllegalArgumentException("Setting both xtfFilename_Regex and xtfFilename_List is invalid");
        }
        if (!hasRegex && !hasValueList) {
            throw new IllegalArgumentException("Either xtfFilename_Regex or xtfFilename_List must be set");
        }
    }

    private void assertAllXtfArgsNull() {
        List<String> errors = new ArrayList<>();
        if (xtfFile_FolderPath != null) {
            errors.add("xtfFile_FolderPath");
        }
        if (!xtfFilename_List.isEmpty()) {
            errors.add("xtfFilename_List");
        }
        if (xtfFilename_Regex != null) {
            errors.add("xtfFilename_Regex");
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Publisher is in db mode. These xtf arguments must be null: " + String.join(", ", errors));
        }
    }

    private void assertAllDbArgsNull() {
        List<String> errors = new ArrayList<>();
        if (dbMergeToSingleXtf != null) {
            errors.add("dbMergeToSingleXtf");
        }
        if (dbDatabase != null) {
            errors.add("dbDatabase");
        }
        if (dbSchema != null) {
            errors.add("dbSchema");
        }
        if (dbIliIdent_Type != null) {
            errors.add("dbIliIdent_Type");
        }
        if (dbIliIdent_RegEx != null) {
            errors.add("dbIliIdent_RegEx");
        }
        if (!dbIliIdent_Values.isEmpty()) {
            errors.add("dbIliIdent_Values");
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Publisher is in file mode. These db arguments must be null: " + String.join(", ", errors));
        }
    }

    public String getDbDatabase() {
        return dbDatabase;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public String getDbIliIdent_Type() {
        return dbIliIdent_Type;
    }

    public List<String> getDbIliIdent_Values() {
        return dbIliIdent_Values;
    }

    public String getDbIliIdent_RegEx() {
        return dbIliIdent_RegEx;
    }

    public Boolean getDbMergeToSingleXtf() {
        return dbMergeToSingleXtf;
    }

    public IliIdentType getDbIliIdentType() {
        return dbIliIdent_TypeEnum;
    }

    public String getXtfFile_FolderPath() {
        return xtfFile_FolderPath;
    }

    public String getXtfFilename_Regex() {
        return xtfFilename_Regex;
    }

    public List<String> getXtfFilename_List() {
        return xtfFilename_List;
    }

    public Endpoint getOutBasePath() {
        return outBasePath;
    }

    public String getOutDataIdent() {
        return outDataIdent;
    }

    public Object getOutValidationConfig() {
        return outValidationConfig;
    }

    public List<DerivedFormat> getOutDerivedFormats() {
        return outDerivedFormats;
    }

    public Date getDepVersion() {
        return new Date(depVersion.getTime());
    }

    public PublishMode getPublishMode() {
        return publishMode;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<String> copyNormalized(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            String normalizedValue = normalize(value);
            if (normalizedValue != null) {
                normalized.add(normalizedValue);
            }
        }
        return Collections.unmodifiableList(normalized);
    }

    private static List<DerivedFormat> normalizeDerivedFormats(List<DerivedFormat> derivedFormats) {
        if (derivedFormats == null || derivedFormats.isEmpty()) {
            return Collections.emptyList();
        }
        List<DerivedFormat> normalized = new ArrayList<>();
        for (DerivedFormat derivedFormat : derivedFormats) {
            if (derivedFormat == null) {
                throw new IllegalArgumentException("outDerivedFormats must not contain null values");
            }
            if (!normalized.contains(derivedFormat)) {
                normalized.add(derivedFormat);
            }
        }
        return Collections.unmodifiableList(normalized);
    }
}
