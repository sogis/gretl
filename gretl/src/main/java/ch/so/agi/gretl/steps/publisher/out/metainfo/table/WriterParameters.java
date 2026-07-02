package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

public final class WriterParameters implements OperationParameters {
    private final Connection connection;
    private final String publicationIdent;
    private final List<Path> cachedTransferFiles;

    private WriterParameters(Connection connection, String publicationIdent, List<Path> cachedTransferFiles) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.publicationIdent = requireText(publicationIdent, "publicationIdent");
        Objects.requireNonNull(cachedTransferFiles, "cachedTransferFiles must not be null");
        if (cachedTransferFiles.isEmpty()) {
            throw new IllegalArgumentException("cachedTransferFiles must not be empty");
        }
        this.cachedTransferFiles = Collections.unmodifiableList(new ArrayList<Path>(cachedTransferFiles));
    }

    public static WriterParameters of(Connection connection, String publicationIdent, List<Path> cachedTransferFiles) {
        return new WriterParameters(connection, publicationIdent, cachedTransferFiles);
    }

    public Connection getConnection() {
        return connection;
    }

    public String getPublicationIdent() {
        return publicationIdent;
    }

    public List<Path> getCachedTransferFiles() {
        return cachedTransferFiles;
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
