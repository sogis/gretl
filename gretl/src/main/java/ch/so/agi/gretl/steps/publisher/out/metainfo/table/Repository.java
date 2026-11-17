package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Persists publisher metadata in the fixed publisher metadata schema. */
public class Repository {
    private static final Set<String> EXPORTED_FORMATS = Set.of("xtf", "itf", "gpkg", "shp", "dxf", "dxf_geobau");

    public void write(Connection connection, String schema, Mapper.PublicationTree publicationTree) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(publicationTree, "publicationTree");
        String checkedSchema = requiredSchema(schema);
        LocalDate publicationDate = Objects.requireNonNull(publicationTree.getPublicationDate(), "publicationDate");
        validateFormats(publicationTree.getArtifactTypes());

        Long existingPublicationId = findPublicationId(connection, checkedSchema, publicationTree.getPublicationIdent(), publicationDate);
        if (existingPublicationId != null) deletePublication(connection, checkedSchema, existingPublicationId.longValue());

        long publicationId = insertPublication(connection, checkedSchema, publicationTree.getPublicationIdent(), publicationDate);
        if (publicationTree.getParts().size() > 1) {
            for (Mapper.PartRow part : publicationTree.getParts()) {
                insertPart(connection, checkedSchema, part.getPartIdent(), publicationId);
            }
        }
        for (String format : publicationTree.getArtifactTypes()) insertFormat(connection, checkedSchema, format, publicationId);
    }

    /** Retention is not part of the publication-state model yet. */
    public List<String> findPublicationIdsToDelete(Connection connection, int maxPublicationRecords) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        if (maxPublicationRecords < 0) throw new IllegalArgumentException("maxPublicationRecords must not be negative");
        throw new UnsupportedOperationException("Publication metadata retention is not implemented yet");
    }

    /** Retention is not part of the publication-state model yet. */
    public void deletePublicationTree(Connection connection, String publicationIdent) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(publicationIdent, "publicationIdent");
        throw new UnsupportedOperationException("Publication metadata retention is not implemented yet");
    }

    private static Long findPublicationId(Connection connection, String schema, String dataIdent, LocalDate pubDate) throws SQLException {
        String sql = "SELECT \"t_id\" FROM " + table(schema, "publicationrun") + " WHERE \"dataident\"=? AND \"pubdate\"=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dataIdent);
            statement.setDate(2, Date.valueOf(pubDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return null;
                long id = resultSet.getLong(1);
                if (resultSet.next()) throw new SQLException("metadata schema contains duplicate publicationrun rows for " + dataIdent + " / " + pubDate);
                return Long.valueOf(id);
            }
        }
    }

    private static void deletePublication(Connection connection, String schema, long publicationId) throws SQLException {
        deleteByPublicationId(connection, table(schema, "part"), publicationId);
        deleteByPublicationId(connection, table(schema, "exportedformat"), publicationId);
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table(schema, "publicationrun") + " WHERE \"t_id\"=?")) {
            statement.setLong(1, publicationId);
            statement.executeUpdate();
        }
    }

    private static void deleteByPublicationId(Connection connection, String table, long publicationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE \"publication_r\"=?")) {
            statement.setLong(1, publicationId);
            statement.executeUpdate();
        }
    }

    private static long insertPublication(Connection connection, String schema, String dataIdent, LocalDate pubDate) throws SQLException {
        return insertReturningId(connection, "INSERT INTO " + table(schema, "publicationrun")
                + " (\"dataident\", \"pubdate\") VALUES (?, ?)", statement -> {
                    statement.setString(1, dataIdent);
                    statement.setDate(2, Date.valueOf(pubDate));
                });
    }

    private static void insertPart(Connection connection, String schema, String partIdent, long publicationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table(schema, "part")
                + " (\"partident\", \"publication_r\") VALUES (?, ?)")) {
            statement.setString(1, partIdent);
            statement.setLong(2, publicationId);
            statement.executeUpdate();
        }
    }

    private static void insertFormat(Connection connection, String schema, String format, long publicationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table(schema, "exportedformat")
                + " (\"aformat\", \"publication_r\") VALUES (?, ?)")) {
            statement.setString(1, format);
            statement.setLong(2, publicationId);
            statement.executeUpdate();
        }
    }

    private static long insertReturningId(Connection connection, String sql, StatementBinder binder) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql + " RETURNING \"t_id\"")) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new SQLException("metadata insert did not return t_id");
                return resultSet.getLong(1);
            }
        }
    }

    private static void validateFormats(List<String> formats) {
        for (String format : formats) {
            if (!EXPORTED_FORMATS.contains(format)) throw new IllegalArgumentException("unsupported exported format: " + format);
        }
    }

    private static String requiredSchema(String schema) {
        if (schema == null || schema.trim().isEmpty()) throw new IllegalArgumentException("metadataSchema must not be blank");
        return schema.trim();
    }

    private static String table(String schema, String table) { return quote(schema) + "." + quote(table); }

    private static String quote(String identifier) { return "\"" + identifier.replace("\"", "\"\"") + "\""; }

    @FunctionalInterface
    private interface StatementBinder { void bind(PreparedStatement statement) throws SQLException; }
}
