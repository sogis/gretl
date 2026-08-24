package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.sql.Connection;
import java.time.LocalDate;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;
public final class MetaTableWriterParameters implements OperationParameters {
    private final Connection connection;
    private final String metadataSchema;
    private final String publicationIdent;
    private final LocalDate publicationDate;
    private final List<String> partIdentifiers;
    private final Path cacheRoot;
    private final Path rawRoot;
    private MetaTableWriterParameters(Connection connection, String metadataSchema, String publicationIdent, LocalDate publicationDate,
            List<String> partIdentifiers, Path cacheRoot, Path rawRoot) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.metadataSchema = requireText(metadataSchema, "metadataSchema");
        this.publicationIdent = requireText(publicationIdent, "publicationIdent");
        this.publicationDate = Objects.requireNonNull(publicationDate, "publicationDate must not be null");
        this.partIdentifiers = immutableRequiredValues(partIdentifiers, "partIdentifiers");
        this.cacheRoot = Objects.requireNonNull(cacheRoot, "cacheRoot must not be null");
        this.rawRoot = rawRoot;
    }

    public static MetaTableWriterParameters of(Connection connection, String metadataSchema, String publicationIdent,
            LocalDate publicationDate, List<String> partIdentifiers, Path cacheRoot) {
        return new MetaTableWriterParameters(connection, metadataSchema, publicationIdent, publicationDate, partIdentifiers,
                cacheRoot, null);
    }

    /** Uses the raw workspace as the authoritative source of part identifiers. */
    public static MetaTableWriterParameters of(Connection connection, String metadataSchema, String publicationIdent,
            LocalDate publicationDate, Path rawRoot, Path publicationRoot) {
        return new MetaTableWriterParameters(connection, metadataSchema, publicationIdent, publicationDate,
                Collections.singletonList("workspace"), publicationRoot, Objects.requireNonNull(rawRoot, "rawRoot"));
    }

    public Connection getConnection() {
        return connection;
    }

    public String getPublicationIdent() {
        return publicationIdent;
    }

    public String getMetadataSchema() {
        return metadataSchema;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public List<String> getPartIdentifiers() {
        return partIdentifiers;
    }

    public Path getCacheRoot() {
        return cacheRoot;
    }
    public Path getRawRoot() { return rawRoot; }

    private static List<String> immutableRequiredValues(List<String> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + " must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            result.add(requireText(value, fieldName + " entry"));
        }
        return Collections.unmodifiableList(result);
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
