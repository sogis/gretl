package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivedFormat;

public final class WriterParameters implements OperationParameters {
    private final Connection connection;
    private final String metadataSchema;
    private final String publicationIdent;
    private final LocalDate publicationDate;
    private final List<String> partIdentifiers;
    private final List<String> exportedFormats;
    private final List<DerivedFormat> requestedDerivedFormats;

    private WriterParameters(Connection connection, String metadataSchema, String publicationIdent, LocalDate publicationDate,
            List<String> partIdentifiers, List<String> exportedFormats) {
        this(connection, metadataSchema, publicationIdent, publicationDate, partIdentifiers, exportedFormats,
                Collections.emptyList());
    }

    private WriterParameters(Connection connection, String metadataSchema, String publicationIdent, LocalDate publicationDate,
            List<String> partIdentifiers, List<String> exportedFormats, List<DerivedFormat> requestedDerivedFormats) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.metadataSchema = requireText(metadataSchema, "metadataSchema");
        this.publicationIdent = requireText(publicationIdent, "publicationIdent");
        this.publicationDate = Objects.requireNonNull(publicationDate, "publicationDate must not be null");
        this.partIdentifiers = immutableRequiredValues(partIdentifiers, "partIdentifiers");
        this.exportedFormats = immutableRequiredValues(exportedFormats, "exportedFormats");
        this.requestedDerivedFormats = immutableDerivedFormats(requestedDerivedFormats);
    }

    public static WriterParameters of(Connection connection, String metadataSchema, String publicationIdent,
            LocalDate publicationDate, List<String> partIdentifiers, List<String> exportedFormats) {
        return new WriterParameters(connection, metadataSchema, publicationIdent, publicationDate, partIdentifiers,
                exportedFormats);
    }

    public static WriterParameters of(Connection connection, String metadataSchema, String publicationIdent,
            LocalDate publicationDate, List<String> partIdentifiers, List<String> exportedFormats,
            List<DerivedFormat> requestedDerivedFormats) {
        return new WriterParameters(connection, metadataSchema, publicationIdent, publicationDate, partIdentifiers,
                exportedFormats, requestedDerivedFormats);
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

    public List<String> getExportedFormats() {
        return exportedFormats;
    }

    public List<DerivedFormat> getRequestedDerivedFormats() {
        return requestedDerivedFormats;
    }

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

    private static List<DerivedFormat> immutableDerivedFormats(List<DerivedFormat> values) {
        Objects.requireNonNull(values, "requestedDerivedFormats must not be null");
        List<DerivedFormat> result = new ArrayList<DerivedFormat>();
        for (DerivedFormat value : values) {
            result.add(Objects.requireNonNull(value, "requestedDerivedFormats entry must not be null"));
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
