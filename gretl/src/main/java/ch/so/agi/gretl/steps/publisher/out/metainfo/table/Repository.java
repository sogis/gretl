package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Provides low-level database access for publication metadata.
 *
 * <p>This class is shared by the writer and the groomer and hides the JDBC or
 * SQL details of the publication, part, and artifact-type tables.</p>
 */
public class Repository {
    public void write(Connection connection, Mapper.PublicationTree publicationTree) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(publicationTree, "publicationTree");
        throw new UnsupportedOperationException("Publication metadata persistence is not implemented yet");
    }

    public List<String> findPublicationIdsToDelete(Connection connection, int maxPublicationRecords) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        if (maxPublicationRecords < 0) {
            throw new IllegalArgumentException("maxPublicationRecords must not be negative");
        }
        throw new UnsupportedOperationException("Publication metadata retention is not implemented yet");
    }

    public void deletePublicationTree(Connection connection, String publicationIdent) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(publicationIdent, "publicationIdent");
        throw new UnsupportedOperationException("Publication metadata deletion is not implemented yet");
    }
}
