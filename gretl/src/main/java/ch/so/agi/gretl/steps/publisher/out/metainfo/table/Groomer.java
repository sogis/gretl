package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Removes old publication metadata according to a fixed retention limit.
 *
 * <p>The metadata groomer keeps only the most recent configured number of
 * publication records and deletes the older publication trees.</p>
 */
public class Groomer {
    private final Repository repository;

    public Groomer(Repository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public void groom(Connection connection, int maxPublicationRecords) throws SQLException {
        if (maxPublicationRecords < 0) {
            throw new IllegalArgumentException("maxPublicationRecords must not be negative");
        }
        List<String> publicationIdsToDelete = repository.findPublicationIdsToDelete(connection, maxPublicationRecords);
        for (String publicationId : publicationIdsToDelete) {
            repository.deletePublicationTree(connection, publicationId);
        }
    }
}
