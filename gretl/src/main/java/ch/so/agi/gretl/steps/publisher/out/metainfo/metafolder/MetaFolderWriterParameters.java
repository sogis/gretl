package ch.so.agi.gretl.steps.publisher.out.metainfo.metafolder;

import java.nio.file.Path;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

/** Input for recreating a publication's legacy {@code meta} folder. */
public final class MetaFolderWriterParameters implements OperationParameters {
    private final Path rawRoot;
    private final Path publicationRoot;
    private final String ident;
    private final String customModelDir;
    private final Path validationConfig;
    private final String jsonmetaAddress;
    private final String jsonmetaBucket;
    private final String jsonmetaFileName;

    private MetaFolderWriterParameters(Path rawRoot, Path publicationRoot, String ident, String customModelDir, Path validationConfig,
            String jsonmetaAddress, String jsonmetaBucket, String jsonmetaFileName) {
        this.rawRoot = Objects.requireNonNull(rawRoot, "rawRoot must not be null");
        this.publicationRoot = Objects.requireNonNull(publicationRoot, "publicationRoot must not be null");
        this.ident = requireText(ident, "ident");
        this.customModelDir = customModelDir;
        this.validationConfig = validationConfig;
        this.jsonmetaAddress = jsonmetaAddress;
        this.jsonmetaBucket = jsonmetaBucket;
        this.jsonmetaFileName = jsonmetaFileName;
        boolean hasJsonSource = jsonmetaAddress != null || jsonmetaBucket != null || jsonmetaFileName != null;
        if (hasJsonSource && (jsonmetaAddress == null || jsonmetaBucket == null || jsonmetaFileName == null)) {
            throw new IllegalArgumentException("all JSON metadata source properties must be provided together");
        }
    }

    public static MetaFolderWriterParameters of(Path rawRoot, Path publicationRoot, String ident, String customModelDir,
            Path validationConfig, String jsonmetaAddress, String jsonmetaBucket, String jsonmetaFileName) {
        return new MetaFolderWriterParameters(rawRoot, publicationRoot, ident, customModelDir, validationConfig, jsonmetaAddress,
                jsonmetaBucket, jsonmetaFileName);
    }

    /** @deprecated Use the explicit raw and publication roots overload. */
    @Deprecated
    public static MetaFolderWriterParameters of(Path cacheRoot, String ident, String customModelDir,
            Path validationConfig, String jsonmetaAddress, String jsonmetaBucket, String jsonmetaFileName) {
        return of(cacheRoot, cacheRoot, ident, customModelDir, validationConfig, jsonmetaAddress, jsonmetaBucket,
                jsonmetaFileName);
    }

    public Path getRawRoot() { return rawRoot; }
    public Path getPublicationRoot() { return publicationRoot; }
    public String getIdent() { return ident; }
    public String getCustomModelDir() { return customModelDir; }
    public Path getValidationConfig() { return validationConfig; }
    public boolean shouldWriteJson() { return jsonmetaAddress != null; }
    public String getJsonmetaAddress() { return jsonmetaAddress; }
    public String getJsonmetaBucket() { return jsonmetaBucket; }
    public String getJsonmetaFileName() { return jsonmetaFileName; }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
