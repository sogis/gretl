package ch.so.agi.gretl.tasks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.steps.publisher.DatabaseConfig;
import ch.so.agi.gretl.steps.publisher.PublisherStep;
import ch.so.agi.gretl.steps.publisher.RawPublisherArgs;
import ch.so.agi.gretl.steps.publisher.RawPublisherArgs.IliIdentType;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;
import ch.so.agi.gretl.tasks.impl.publisher.PropertiesReader;
import ch.so.agi.gretl.util.TaskUtil;

/**
 * Publisher task for the new publisher pipeline.
 *
 * <p>Legacy jobs must use {@link PublisherOld}. This task intentionally only
 * exposes the canonical db-, xtf-, and out-prefixed API.</p>
 */
public class Publisher extends DefaultTask {
    private Connector dbDatabase;
    private String dbSchema;
    private String dbIliIdent_Type;
    private final ListProperty<String> dbIliIdent_Values = getProject().getObjects().listProperty(String.class);
    private String dbIliIdent_RegEx;
    private Boolean dbMergeToSingleXtf;

    private String xtfFile_FolderPath;
    private String xtfFilename_Regex;
    private final ListProperty<String> xtfFilename_List = getProject().getObjects().listProperty(String.class);

    private Endpoint outFolderPath;
    private String outDataIdent;
    private Boolean outWriteMetadata = true;
    private String outGroomingConfigFilePath;
    private String outValidationConfigFilePath;
    private String customModelDir;
    private final ListProperty<OutputFormat> outFormats = getProject().getObjects().listProperty(OutputFormat.class);

    @Input @Optional public Connector getDbDatabase() { return dbDatabase; }
    @Input @Optional public String getDbSchema() { return dbSchema; }
    @Input @Optional public String getDbIliIdent_Type() { return dbIliIdent_Type; }
    @Input @Optional public ListProperty<String> getDbIliIdent_Values() { return dbIliIdent_Values; }
    @Input @Optional public String getDbIliIdent_RegEx() { return dbIliIdent_RegEx; }
    @Input @Optional public Boolean getDbMergeToSingleXtf() { return dbMergeToSingleXtf; }
    @Input @Optional public String getXtfFile_FolderPath() { return xtfFile_FolderPath; }
    @Input @Optional public String getXtfFilename_Regex() { return xtfFilename_Regex; }
    @Input @Optional public ListProperty<String> getXtfFilename_List() { return xtfFilename_List; }
    @Input @Optional public Endpoint getOutFolderPath() { return outFolderPath; }
    @Input public String getOutDataIdent() { return outDataIdent; }
    @Input public Boolean getOutWriteMetadata() { return outWriteMetadata; }
    @Input @Optional public String getOutGroomingConfigFilePath() { return outGroomingConfigFilePath; }
    @Input @Optional public String getOutValidationConfigFilePath() { return outValidationConfigFilePath; }
    @Input @Optional public String getCustomModelDir() { return customModelDir; }
    @Input public ListProperty<OutputFormat> getOutFormats() { return outFormats; }

    public void setDbDatabase(List<?> details) { dbDatabase = connector(details, "dbDatabase"); }
    public void setDbDatabase(Connector database) { dbDatabase = database; }
    public void setDbSchema(String value) { dbSchema = value; }
    public void setDbIliIdent_Type(String value) { dbIliIdent_Type = value; }
    public void setDbIliIdent_Values(List<String> values) { dbIliIdent_Values.set(values); }
    public void setDbIliIdent_RegEx(String value) { dbIliIdent_RegEx = value; }
    public void setDbMergeToSingleXtf(Boolean value) { dbMergeToSingleXtf = value; }
    public void setXtfFile_FolderPath(Object value) { xtfFile_FolderPath = path(value); }
    public void setXtfFilename_Regex(String value) { xtfFilename_Regex = value; }
    public void setXtfFilename_List(List<String> values) { xtfFilename_List.set(values); }
    public void setOutFolderPath(List<?> details) { outFolderPath = endpoint(details, "outFolderPath"); }
    public void setOutFolderPath(Endpoint value) { outFolderPath = value; }
    public void setOutFolderPath(Object value) { outFolderPath = value == null ? null : new Endpoint(path(value)); }
    public void setOutDataIdent(String value) { outDataIdent = value; }
    public void setOutWriteMetadata(Boolean value) { outWriteMetadata = value == null ? true : value; }
    public void setOutGroomingConfigFilePath(Object value) { outGroomingConfigFilePath = path(value); }
    public void setOutValidationConfigFilePath(Object value) { outValidationConfigFilePath = path(value); }
    public void setCustomModelDir(String value) { customModelDir = value; }
    public void setOutFormats(List<?> values) { outFormats.set(parseFormats(values)); }

    @TaskAction
    public void publishAll() {
        RawPublisherArgs rawArgs = buildRawArgs();
        PublisherEnv publisherEnv = rawArgs.getOutWriteMetadata()
                ? new PropertiesReader(getProject()).readProperties()
                : PublisherEnv.empty();

        try {
            Path cacheRoot = getProject().getBuildDir().toPath().resolve(getName());
            Files.createDirectories(cacheRoot);
            new PublisherStep(getName()).publish(rawArgs, publisherEnv, cacheRoot);
        } catch (Exception e) {
            throw TaskUtil.toGradleException(e);
        }
    }

    private RawPublisherArgs buildRawArgs() {
        RawPublisherArgs.Builder builder = RawPublisherArgs.builder()
                .output(outFolderPath, outDataIdent)
                .writeMetadata(outWriteMetadata)
                .groomingConfig(outGroomingConfigFilePath)
                .validationConfig(outValidationConfigFilePath)
                .customModelDir(customModelDir)
                .outFormats(outFormats.getOrElse(List.of()));

        if (dbDatabase != null) {
            IliIdentType identType = IliIdentType.valueOf(required(dbIliIdent_Type, "dbIliIdent_Type"));
            if (dbIliIdent_RegEx != null) {
                return builder.dbRegexSource(DatabaseConfig.from(dbDatabase), dbSchema, identType, dbMergeToSingleXtf,
                        dbIliIdent_RegEx).build();
            }
            return builder.dbValuesSource(DatabaseConfig.from(dbDatabase), dbSchema, identType, dbMergeToSingleXtf,
                    dbIliIdent_Values.getOrElse(List.of())).build();
        }
        if (xtfFilename_Regex != null) {
            return builder.xtfRegexSource(xtfFile_FolderPath, xtfFilename_Regex).build();
        }
        return builder.xtfListSource(xtfFile_FolderPath, xtfFilename_List.getOrElse(List.of())).build();
    }

    private String path(Object value) {
        return value == null ? null : getProject().file(value).toPath().toAbsolutePath().normalize().toString();
    }

    private static Connector connector(List<?> details, String name) {
        if (details == null || details.isEmpty() || details.size() > 3) {
            throw new IllegalArgumentException(name + " must contain a JDBC URL and optional user/password");
        }
        String url = String.valueOf(details.get(0));
        String user = details.size() > 1 ? String.valueOf(details.get(1)) : null;
        String password = details.size() > 2 ? String.valueOf(details.get(2)) : null;
        return new Connector(url, user, password);
    }

    private static Endpoint endpoint(List<?> details, String name) {
        if (details == null || details.isEmpty() || details.size() > 3) {
            throw new IllegalArgumentException(name + " must contain a path/URL and optional user/password");
        }
        String url = String.valueOf(details.get(0));
        String user = details.size() > 1 ? String.valueOf(details.get(1)) : null;
        String password = details.size() > 2 ? String.valueOf(details.get(2)) : null;
        return new Endpoint(url, user, password);
    }

    private static List<OutputFormat> parseFormats(List<?> values) {
        List<OutputFormat> formats = new ArrayList<>();
        for (Object value : values == null ? List.of() : values) {
            formats.add(OutputFormat.parse(value));
        }
        return formats;
    }

    private static String required(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must be set");
        }
        return value;
    }

}
