package ch.so.agi.gretl.steps.publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;

/**
 * DTO representing the flat publisher inputs exactly as provided.
 */
public class RawPublisherArgs {
    private final DatabaseConfig dbDatabase;
    private final String dbSchema;
    private final String dbIliIdent_Type;
    private final List<String> dbIliIdent_Values;
    private final String dbIliIdent_RegEx;
    private final Boolean dbMergeToSingleXtf;
    private final String xtfFile_FolderPath;
    private final String xtfFilename_Regex;
    private final List<String> xtfFilename_List;
    private final Endpoint outFolderPath;
    private final String outDataIdent;
    private final Boolean outWriteMetadata;
    private final String outGroomingConfigFilePath;
    private final String outValidationConfigFilePath;
    private final String customModelDir;
    private final List<OutputFormat> outFormats;

    private PublishMode publishMode;
    private IliIdentType dbIliIdent_TypeEnum;

    public enum PublishMode {
        dbIdentvaluesList,
        dbIdentvaluesRegex,
        xtfFilesList,
        xtfFilesRegex
    }

    public enum IliIdentType {
        model,
        topic,
        basket,
        dataset
    }

    public static Builder builder() {
        return new Builder();
    }

    private RawPublisherArgs(CanonicalArgs args) {
        this.dbDatabase = args.dbDatabase;
        this.dbSchema = normalize(args.dbSchema);
        this.dbIliIdent_Type = normalize(args.dbIliIdent_Type);
        this.dbIliIdent_Values = copyNormalized(args.dbIliIdent_Values);
        this.dbIliIdent_RegEx = normalize(args.dbIliIdent_RegEx);
        this.dbMergeToSingleXtf = args.dbMergeToSingleXtf;
        this.xtfFile_FolderPath = normalize(args.xtfFile_FolderPath);
        this.xtfFilename_Regex = normalize(args.xtfFilename_Regex);
        this.xtfFilename_List = copyNormalized(args.xtfFilename_List);
        this.outFolderPath = args.outFolderPath;
        this.outDataIdent = normalize(args.outDataIdent);
        this.outWriteMetadata = args.outWriteMetadata == null ? true : args.outWriteMetadata;
        this.outGroomingConfigFilePath = normalize(args.outGroomingConfigFilePath);
        this.outValidationConfigFilePath = normalize(args.outValidationConfigFilePath);
        this.customModelDir = normalize(args.customModelDir);
        this.outFormats = normalizeOutputFormats(args.outFormats);
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
        if (!outWriteMetadata && outFolderPath == null) {
            missingArgs.add("outFolderPath when outWriteMetadata is false");
        }
        if (outDataIdent == null) {
            missingArgs.add("outDataIdent");
        }
        if (outFormats.isEmpty()) {
            missingArgs.add("outFormats");
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
        if (dbDatabase == null || dbDatabase.getUrl() == null) {
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

    public DatabaseConfig getDbDatabase() {
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

    public Endpoint getOutFolderPath() {
        return outFolderPath;
    }

    public String getOutDataIdent() {
        return outDataIdent;
    }

    public Boolean getOutWriteMetadata() {
        return outWriteMetadata;
    }

    public String getOutGroomingConfigFilePath() {
        return outGroomingConfigFilePath;
    }

    public String getOutValidationConfigFilePath() {
        return outValidationConfigFilePath;
    }

    public String getCustomModelDir() {
        return customModelDir;
    }

    public List<OutputFormat> getOutFormats() { return outFormats; }

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

    private static List<OutputFormat> normalizeOutputFormats(List<OutputFormat> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<OutputFormat> normalized = new ArrayList<OutputFormat>();
        for (OutputFormat format : values) {
            if (format == null) {
                throw new IllegalArgumentException("outFormats must not contain null values");
            }
            if (!normalized.contains(format)) {
                normalized.add(format);
            }
        }
        return Collections.unmodifiableList(normalized);
    }

    private static final class CanonicalArgs {
        private DatabaseConfig dbDatabase;
        private String dbSchema;
        private String dbIliIdent_Type;
        private List<String> dbIliIdent_Values;
        private String dbIliIdent_RegEx;
        private Boolean dbMergeToSingleXtf;
        private String xtfFile_FolderPath;
        private String xtfFilename_Regex;
        private List<String> xtfFilename_List;
        private Endpoint outFolderPath;
        private String outDataIdent;
        private Boolean outWriteMetadata;
        private String outGroomingConfigFilePath;
        private String outValidationConfigFilePath;
        private String customModelDir;
        private List<OutputFormat> outFormats;
    }

    public static final class Builder {
        private final CanonicalArgs args = new CanonicalArgs();

        public Builder dbValuesSource(DatabaseConfig dbDatabase, String dbSchema, IliIdentType dbIliIdentType,
                Boolean dbMergeToSingleXtf, List<String> dbIliIdentValues) {
            clearXtfSource();
            args.dbDatabase = dbDatabase;
            args.dbSchema = dbSchema;
            args.dbIliIdent_Type = dbIliIdentType != null ? dbIliIdentType.name() : null;
            args.dbIliIdent_Values = dbIliIdentValues;
            args.dbIliIdent_RegEx = null;
            args.dbMergeToSingleXtf = dbMergeToSingleXtf;
            return this;
        }

        public Builder dbValuesSource(String dbDatabase, String dbSchema, IliIdentType dbIliIdentType,
                Boolean dbMergeToSingleXtf, List<String> dbIliIdentValues) {
            return dbValuesSource(new DatabaseConfig(dbDatabase, null, null), dbSchema, dbIliIdentType,
                    dbMergeToSingleXtf, dbIliIdentValues);
        }

        public Builder dbRegexSource(DatabaseConfig dbDatabase, String dbSchema, IliIdentType dbIliIdentType,
                Boolean dbMergeToSingleXtf, String dbIliIdentRegEx) {
            clearXtfSource();
            args.dbDatabase = dbDatabase;
            args.dbSchema = dbSchema;
            args.dbIliIdent_Type = dbIliIdentType != null ? dbIliIdentType.name() : null;
            args.dbIliIdent_Values = null;
            args.dbIliIdent_RegEx = dbIliIdentRegEx;
            args.dbMergeToSingleXtf = dbMergeToSingleXtf;
            return this;
        }

        public Builder dbRegexSource(String dbDatabase, String dbSchema, IliIdentType dbIliIdentType,
                Boolean dbMergeToSingleXtf, String dbIliIdentRegEx) {
            return dbRegexSource(new DatabaseConfig(dbDatabase, null, null), dbSchema, dbIliIdentType,
                    dbMergeToSingleXtf, dbIliIdentRegEx);
        }

        public Builder xtfListSource(String xtfFileFolderPath, List<String> xtfFilenameList) {
            clearDbSource();
            args.xtfFile_FolderPath = xtfFileFolderPath;
            args.xtfFilename_Regex = null;
            args.xtfFilename_List = xtfFilenameList;
            return this;
        }

        public Builder xtfRegexSource(String xtfFileFolderPath, String xtfFilenameRegex) {
            clearDbSource();
            args.xtfFile_FolderPath = xtfFileFolderPath;
            args.xtfFilename_Regex = xtfFilenameRegex;
            args.xtfFilename_List = null;
            return this;
        }

        public Builder output(Endpoint outFolderPath, String outDataIdent) {
            args.outFolderPath = outFolderPath;
            args.outDataIdent = outDataIdent;
            return this;
        }

        public Builder writeMetadata(Boolean outWriteMetadata) {
            args.outWriteMetadata = outWriteMetadata;
            return this;
        }

        public Builder groomingConfig(String outGroomingConfigFilePath) {
            args.outGroomingConfigFilePath = outGroomingConfigFilePath;
            return this;
        }

        public Builder validationConfig(String outValidationConfigFilePath) {
            args.outValidationConfigFilePath = outValidationConfigFilePath;
            return this;
        }

        public Builder customModelDir(String customModelDir) {
            args.customModelDir = customModelDir;
            return this;
        }

        public Builder outFormats(List<OutputFormat> outFormats) {
            args.outFormats = outFormats;
            return this;
        }

        public RawPublisherArgs build() {
            return new RawPublisherArgs(args);
        }

        private void clearDbSource() {
            args.dbDatabase = null;
            args.dbSchema = null;
            args.dbIliIdent_Type = null;
            args.dbIliIdent_Values = null;
            args.dbIliIdent_RegEx = null;
            args.dbMergeToSingleXtf = null;
        }

        private void clearXtfSource() {
            args.xtfFile_FolderPath = null;
            args.xtfFilename_Regex = null;
            args.xtfFilename_List = null;
        }
    }
}
