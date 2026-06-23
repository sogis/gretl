package ch.so.agi.gretl.steps.publisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.Endpoint;

public final class PublisherStepParameters {
    private final Connector dbDatabase;
    private final String dbSchema;
    private final String dbIliIdentType;
    private final List<String> dbIliIdentValues;
    private final String dbIliIdentRegEx;
    private final boolean dbMergeToSingleXtf;
    private final Object xtfFilePath;
    private final String xtfFilenameRegex;
    private final List<String> xtfFilenameList;
    private final Endpoint outBasePath;
    private final String outDataIdent;
    private final boolean outWriteUserFormats;
    private final Object outGroomingConf;
    private final Object outValidationConfig;
    private final List<String> outPublishedRegions;
    private final String globModeldir;
    private final String globProxy;
    private final Integer globProxyPort;
    private final String depExportModels;
    private final Date depVersion;

    private PublisherStepParameters(Builder builder) {
        this.dbDatabase = builder.dbDatabase;
        this.dbSchema = normalize(builder.dbSchema);
        this.dbIliIdentType = normalize(builder.dbIliIdentType);
        this.dbIliIdentValues = copyNormalized(builder.dbIliIdentValues);
        this.dbIliIdentRegEx = normalize(builder.dbIliIdentRegEx);
        this.dbMergeToSingleXtf = Boolean.TRUE.equals(builder.dbMergeToSingleXtf);
        this.xtfFilePath = builder.xtfFilePath;
        this.xtfFilenameRegex = normalize(builder.xtfFilenameRegex);
        this.xtfFilenameList = copyNormalized(builder.xtfFilenameList);
        this.outBasePath = builder.outBasePath;
        this.outDataIdent = normalize(builder.outDataIdent);
        this.outWriteUserFormats = Boolean.TRUE.equals(builder.outWriteUserFormats);
        this.outGroomingConf = builder.outGroomingConf;
        this.outValidationConfig = builder.outValidationConfig;
        this.outPublishedRegions = copyNormalized(builder.outPublishedRegions);
        this.globModeldir = normalize(builder.globModeldir);
        this.globProxy = normalize(builder.globProxy);
        this.globProxyPort = builder.globProxyPort;
        this.depExportModels = normalize(builder.depExportModels);
        this.depVersion = builder.depVersion != null ? new Date(builder.depVersion.getTime()) : new Date();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Connector getDbDatabase() {
        return dbDatabase;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public String getDbIliIdentType() {
        return dbIliIdentType;
    }

    public List<String> getDbIliIdentValues() {
        return dbIliIdentValues;
    }

    public String getDbIliIdentRegEx() {
        return dbIliIdentRegEx;
    }

    public boolean isDbMergeToSingleXtf() {
        return dbMergeToSingleXtf;
    }

    public Object getXtfFilePath() {
        return xtfFilePath;
    }

    public String getXtfFilenameRegex() {
        return xtfFilenameRegex;
    }

    public List<String> getXtfFilenameList() {
        return xtfFilenameList;
    }

    public Endpoint getOutBasePath() {
        return outBasePath;
    }

    public String getOutDataIdent() {
        return outDataIdent;
    }

    public boolean isOutWriteUserFormats() {
        return outWriteUserFormats;
    }

    public Object getOutGroomingConf() {
        return outGroomingConf;
    }

    public Object getOutValidationConfig() {
        return outValidationConfig;
    }

    public List<String> getOutPublishedRegions() {
        return outPublishedRegions;
    }

    public String getGlobModeldir() {
        return globModeldir;
    }

    public String getGlobProxy() {
        return globProxy;
    }

    public Integer getGlobProxyPort() {
        return globProxyPort;
    }

    public String getDepExportModels() {
        return depExportModels;
    }

    public Date getDepVersion() {
        return new Date(depVersion.getTime());
    }

    public static final class Builder {
        private Connector dbDatabase;
        private String dbSchema;
        private String dbIliIdentType;
        private List<String> dbIliIdentValues;
        private String dbIliIdentRegEx;
        private Boolean dbMergeToSingleXtf;
        private Object xtfFilePath;
        private String xtfFilenameRegex;
        private List<String> xtfFilenameList;
        private Endpoint outBasePath;
        private String outDataIdent;
        private Boolean outWriteUserFormats;
        private Object outGroomingConf;
        private Object outValidationConfig;
        private List<String> outPublishedRegions;
        private String globModeldir;
        private String globProxy;
        private Integer globProxyPort;
        private String depExportModels;
        private Date depVersion;

        public Builder dbDatabase(Connector dbDatabase) {
            this.dbDatabase = dbDatabase;
            return this;
        }

        public Builder dbSchema(String dbSchema) {
            this.dbSchema = dbSchema;
            return this;
        }

        public Builder dbIliIdentType(String dbIliIdentType) {
            this.dbIliIdentType = dbIliIdentType;
            return this;
        }

        public Builder dbIliIdentValues(List<String> dbIliIdentValues) {
            this.dbIliIdentValues = dbIliIdentValues;
            return this;
        }

        public Builder dbIliIdentRegEx(String dbIliIdentRegEx) {
            this.dbIliIdentRegEx = dbIliIdentRegEx;
            return this;
        }

        public Builder dbMergeToSingleXtf(Boolean dbMergeToSingleXtf) {
            this.dbMergeToSingleXtf = dbMergeToSingleXtf;
            return this;
        }

        public Builder xtfFilePath(Object xtfFilePath) {
            this.xtfFilePath = xtfFilePath;
            return this;
        }

        public Builder xtfFilenameRegex(String xtfFilenameRegex) {
            this.xtfFilenameRegex = xtfFilenameRegex;
            return this;
        }

        public Builder xtfFilenameList(List<String> xtfFilenameList) {
            this.xtfFilenameList = xtfFilenameList;
            return this;
        }

        public Builder outBasePath(Endpoint outBasePath) {
            this.outBasePath = outBasePath;
            return this;
        }

        public Builder outDataIdent(String outDataIdent) {
            this.outDataIdent = outDataIdent;
            return this;
        }

        public Builder outWriteUserFormats(Boolean outWriteUserFormats) {
            this.outWriteUserFormats = outWriteUserFormats;
            return this;
        }

        public Builder outGroomingConf(Object outGroomingConf) {
            this.outGroomingConf = outGroomingConf;
            return this;
        }

        public Builder outValidationConfig(Object outValidationConfig) {
            this.outValidationConfig = outValidationConfig;
            return this;
        }

        public Builder outPublishedRegions(List<String> outPublishedRegions) {
            this.outPublishedRegions = outPublishedRegions;
            return this;
        }

        public Builder globModeldir(String globModeldir) {
            this.globModeldir = globModeldir;
            return this;
        }

        public Builder globProxy(String globProxy) {
            this.globProxy = globProxy;
            return this;
        }

        public Builder globProxyPort(Integer globProxyPort) {
            this.globProxyPort = globProxyPort;
            return this;
        }

        public Builder depExportModels(String depExportModels) {
            this.depExportModels = depExportModels;
            return this;
        }

        public Builder depVersion(Date depVersion) {
            this.depVersion = depVersion != null ? new Date(depVersion.getTime()) : null;
            return this;
        }

        public PublisherStepParameters build() {
            PublisherStepParameters parameters = new PublisherStepParameters(this);
            List<String> violations = parameters.validate(dbMergeToSingleXtf);
            if (!violations.isEmpty()) {
                throw new IllegalArgumentException("Invalid Publisher configuration:\n- " + String.join("\n- ", violations));
            }
            return parameters;
        }
    }

    private List<String> validate(Boolean configuredDbMergeToSingleXtf) {
        List<String> violations = new ArrayList<>();

        if (outBasePath == null) {
            violations.add("outBasePath must be set");
        }
        if (!hasText(outDataIdent)) {
            violations.add("outDataIdent must be set");
        }

        boolean dbIliValuesSet = !dbIliIdentValues.isEmpty();
        boolean dbIliRegexSet = hasText(dbIliIdentRegEx);
        boolean xtfFilenameRegexSet = hasText(xtfFilenameRegex);
        boolean xtfFilenameListSet = !xtfFilenameList.isEmpty();
        boolean dbPropertiesSet = dbDatabase != null
                || hasText(dbSchema)
                || hasText(dbIliIdentType)
                || dbIliValuesSet
                || dbIliRegexSet
                || Boolean.TRUE.equals(configuredDbMergeToSingleXtf);
        boolean xtfPropertiesSet = xtfFilePath != null || xtfFilenameRegexSet || xtfFilenameListSet;

        if (dbDatabase == null && xtfFilePath == null) {
            violations.add("exactly one source must be configured: either dbDatabase or xtfFilePath");
        } else if (dbDatabase != null && xtfFilePath != null) {
            violations.add("exactly one source must be configured: either dbDatabase or xtfFilePath");
        }

        if (dbPropertiesSet && xtfPropertiesSet) {
            violations.add("DB and XTF source properties must not be mixed");
        }

        if (dbDatabase != null) {
            if (!hasText(dbSchema)) {
                violations.add("dbSchema must be set when dbDatabase is set");
            }
            if (!hasText(dbIliIdentType)) {
                violations.add("dbIliIdentType must be set when dbDatabase is set");
            }
            if (dbIliValuesSet == dbIliRegexSet) {
                violations.add("exactly one DB ILI selection must be set: dbIliIdentValues or dbIliIdentRegEx");
            }
        } else if (dbPropertiesSet) {
            violations.add("dbDatabase must be set when DB source properties are set");
        }

        if (xtfFilePath != null) {
            if (xtfFilenameRegexSet && xtfFilenameListSet) {
                violations.add("xtfFilenameRegex and xtfFilenameList are mutually exclusive");
            }
        } else if (xtfPropertiesSet) {
            violations.add("xtfFilePath must be set when XTF source properties are set");
        }

        return violations;
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
            String trimmed = normalize(value);
            if (trimmed != null) {
                normalized.add(trimmed);
            }
        }
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(normalized);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
