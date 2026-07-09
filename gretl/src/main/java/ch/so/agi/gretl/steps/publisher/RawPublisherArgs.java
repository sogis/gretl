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
public class RawPublisherArgs {
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
    private final Boolean outIsolatedMode;
    private final String outWriteToThisLocalFolderOnly;
    private final String outCustomGroomingConfFilePath;
    private final String outValidationConfigFilePath;
    private final String customModelDir;
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

    public static Builder builder() {
        return new Builder();
    }

    RawPublisherArgs(String dbDatabase, String dbSchema, String dbIliIdent_Type,
            ArrayList<String> dbIliIdent_Values, String dbIliIdent_RegEx, Boolean dbMergeToSingleXtf,
            String xtfFile_FolderPath, String xtfFilename_Regex, ArrayList<String> xtfFilename_List) {
        this(canonicalArgs(dbDatabase, dbSchema, dbIliIdent_Type, dbIliIdent_Values, dbIliIdent_RegEx,
                dbMergeToSingleXtf, xtfFile_FolderPath, xtfFilename_Regex, xtfFilename_List, null, null, null, null,
                null, null, null, null, null));
    }

    RawPublisherArgs(String dbDatabase, String dbSchema, String dbIliIdent_Type,
            ArrayList<String> dbIliIdent_Values, String dbIliIdent_RegEx, Boolean dbMergeToSingleXtf,
            String xtfFile_FolderPath, String xtfFilename_Regex, ArrayList<String> xtfFilename_List,
            List<DerivedFormat> outDerivedFormats) {
        this(canonicalArgs(dbDatabase, dbSchema, dbIliIdent_Type, dbIliIdent_Values, dbIliIdent_RegEx,
                dbMergeToSingleXtf, xtfFile_FolderPath, xtfFilename_Regex, xtfFilename_List, null, null, null, null,
                null, null, null, outDerivedFormats, null));
    }

    RawPublisherArgs(String dbDatabase, String dbSchema, String dbIliIdent_Type,
            ArrayList<String> dbIliIdent_Values, String dbIliIdent_RegEx, Boolean dbMergeToSingleXtf,
            String xtfFile_FolderPath, String xtfFilename_Regex, ArrayList<String> xtfFilename_List,
            Endpoint outBasePath, String outDataIdent, Boolean outIsolatedMode, String outWriteToThisLocalFolderOnly,
            String outCustomGroomingConfFilePath, String outValidationConfigFilePath, String customModelDir,
            List<DerivedFormat> outDerivedFormats, Date depVersion) {
        this(canonicalArgs(dbDatabase, dbSchema, dbIliIdent_Type, dbIliIdent_Values, dbIliIdent_RegEx,
                dbMergeToSingleXtf, xtfFile_FolderPath, xtfFilename_Regex, xtfFilename_List, outBasePath,
                outDataIdent, outIsolatedMode, outWriteToThisLocalFolderOnly, outCustomGroomingConfFilePath,
                outValidationConfigFilePath, customModelDir, outDerivedFormats, depVersion));
    }

    private RawPublisherArgs(CanonicalArgs args) {
        this.dbDatabase = normalize(args.dbDatabase);
        this.dbSchema = normalize(args.dbSchema);
        this.dbIliIdent_Type = normalize(args.dbIliIdent_Type);
        this.dbIliIdent_Values = copyNormalized(args.dbIliIdent_Values);
        this.dbIliIdent_RegEx = normalize(args.dbIliIdent_RegEx);
        this.dbMergeToSingleXtf = args.dbMergeToSingleXtf;
        this.xtfFile_FolderPath = normalize(args.xtfFile_FolderPath);
        this.xtfFilename_Regex = normalize(args.xtfFilename_Regex);
        this.xtfFilename_List = copyNormalized(args.xtfFilename_List);
        this.outBasePath = args.outBasePath;
        this.outDataIdent = normalize(args.outDataIdent);
        this.outIsolatedMode = args.outIsolatedMode;
        this.outWriteToThisLocalFolderOnly = normalize(args.outWriteToThisLocalFolderOnly);
        this.outCustomGroomingConfFilePath = normalize(args.outCustomGroomingConfFilePath);
        this.outValidationConfigFilePath = normalize(args.outValidationConfigFilePath);
        this.customModelDir = normalize(args.customModelDir);
        this.outDerivedFormats = normalizeDerivedFormats(args.outDerivedFormats);
        this.depVersion = args.depVersion != null ? new Date(args.depVersion.getTime()) : new Date();

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

    public Boolean getOutIsolatedMode() {
        return outIsolatedMode;
    }

    public String getOutWriteToThisLocalFolderOnly() {
        return outWriteToThisLocalFolderOnly;
    }

    public String getOutCustomGroomingConfFilePath() {
        return outCustomGroomingConfFilePath;
    }

    public String getOutValidationConfigFilePath() {
        return outValidationConfigFilePath;
    }

    public String getCustomModelDir() {
        return customModelDir;
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

    private static CanonicalArgs canonicalArgs(String dbDatabase, String dbSchema, String dbIliIdent_Type,
            List<String> dbIliIdent_Values, String dbIliIdent_RegEx, Boolean dbMergeToSingleXtf,
            String xtfFile_FolderPath, String xtfFilename_Regex, List<String> xtfFilename_List, Endpoint outBasePath,
            String outDataIdent, Boolean outIsolatedMode, String outWriteToThisLocalFolderOnly,
            String outCustomGroomingConfFilePath, String outValidationConfigFilePath, String customModelDir,
            List<DerivedFormat> outDerivedFormats, Date depVersion) {
        CanonicalArgs args = new CanonicalArgs();
        args.dbDatabase = dbDatabase;
        args.dbSchema = dbSchema;
        args.dbIliIdent_Type = dbIliIdent_Type;
        args.dbIliIdent_Values = dbIliIdent_Values;
        args.dbIliIdent_RegEx = dbIliIdent_RegEx;
        args.dbMergeToSingleXtf = dbMergeToSingleXtf;
        args.xtfFile_FolderPath = xtfFile_FolderPath;
        args.xtfFilename_Regex = xtfFilename_Regex;
        args.xtfFilename_List = xtfFilename_List;
        args.outBasePath = outBasePath;
        args.outDataIdent = outDataIdent;
        args.outIsolatedMode = outIsolatedMode;
        args.outWriteToThisLocalFolderOnly = outWriteToThisLocalFolderOnly;
        args.outCustomGroomingConfFilePath = outCustomGroomingConfFilePath;
        args.outValidationConfigFilePath = outValidationConfigFilePath;
        args.customModelDir = customModelDir;
        args.outDerivedFormats = outDerivedFormats;
        args.depVersion = depVersion;
        return args;
    }

    private static final class CanonicalArgs {
        private String dbDatabase;
        private String dbSchema;
        private String dbIliIdent_Type;
        private List<String> dbIliIdent_Values;
        private String dbIliIdent_RegEx;
        private Boolean dbMergeToSingleXtf;
        private String xtfFile_FolderPath;
        private String xtfFilename_Regex;
        private List<String> xtfFilename_List;
        private Endpoint outBasePath;
        private String outDataIdent;
        private Boolean outIsolatedMode;
        private String outWriteToThisLocalFolderOnly;
        private String outCustomGroomingConfFilePath;
        private String outValidationConfigFilePath;
        private String customModelDir;
        private List<DerivedFormat> outDerivedFormats;
        private Date depVersion;
    }

    public static final class Builder {
        private final CanonicalArgs args = new CanonicalArgs();

        public Builder dbValuesSource(String dbDatabase, String dbSchema, IliIdentType dbIliIdentType,
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

        public Builder dbRegexSource(String dbDatabase, String dbSchema, IliIdentType dbIliIdentType,
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

        public Builder output(Endpoint outBasePath, String outDataIdent) {
            args.outBasePath = outBasePath;
            args.outDataIdent = outDataIdent;
            return this;
        }

        public Builder isolatedMode(Boolean outIsolatedMode) {
            args.outIsolatedMode = outIsolatedMode;
            return this;
        }

        public Builder localFolderOnly(String outWriteToThisLocalFolderOnly) {
            args.outWriteToThisLocalFolderOnly = outWriteToThisLocalFolderOnly;
            return this;
        }

        public Builder groomingConfig(String outCustomGroomingConfFilePath) {
            args.outCustomGroomingConfFilePath = outCustomGroomingConfFilePath;
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

        public Builder derivedFormats(List<DerivedFormat> outDerivedFormats) {
            args.outDerivedFormats = outDerivedFormats;
            return this;
        }

        public Builder depVersion(Date depVersion) {
            args.depVersion = depVersion;
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
