package ch.so.agi.gretl.steps.publisher.in.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

public final class ExporterParameters implements OperationParameters {
    private final DataSelection selectionToExport;
    private final Connection connection;
    private final String dbSchema;
    private final boolean mergeToSingleXtf;
    private final Path exportDirectory;

    private ExporterParameters(DataSelection selectionToExport, Connection connection, String dbSchema,
            boolean mergeToSingleXtf, Path exportDirectory) {
        this.selectionToExport = Objects.requireNonNull(selectionToExport, "selectionToExport must not be null");
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.dbSchema = requireText(dbSchema, "dbSchema");
        this.mergeToSingleXtf = mergeToSingleXtf;
        this.exportDirectory = Objects.requireNonNull(exportDirectory, "exportDirectory must not be null");
    }

    public static ExporterParameters of(DataSelection selectionToExport, Connection connection, String dbSchema,
            boolean mergeToSingleXtf, Path exportDirectory) {
        return new ExporterParameters(selectionToExport, connection, dbSchema, mergeToSingleXtf, exportDirectory);
    }

    public DataSelection getSelectionToExport() {
        return selectionToExport;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public boolean isMergeToSingleXtf() {
        return mergeToSingleXtf;
    }

    public Path getExportDirectory() {
        return exportDirectory;
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
