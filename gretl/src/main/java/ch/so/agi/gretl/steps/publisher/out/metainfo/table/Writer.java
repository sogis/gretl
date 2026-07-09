package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.Operation;

/**
 * Writes one publication payload into the metadata tables.
 *
 * <p>This class coordinates the persistence of the publication root row and
 * its dependent part and artifact-type rows. It owns the write flow but does
 * not contain retention logic.</p>
 */
public class Writer implements Operation<WriterParameters> {
    private final Repository repository;
    private final Mapper mapper;

    public Writer() {
        this(new Repository(), new Mapper());
    }

    public Writer(Repository repository, Mapper mapper) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public void execute(WriterParameters operationParameters) throws SQLException {
        write(operationParameters);
    }

    void write(WriterParameters operationParameters) throws SQLException {
        /*
        Mapper.PublicationTree publicationTree = mapper.map(operationParameters.getPublicationIdent(),
                operationParameters.getCachedTransferFiles());
        repository.write(operationParameters.getConnection(), publicationTree);
        */
    }
}
