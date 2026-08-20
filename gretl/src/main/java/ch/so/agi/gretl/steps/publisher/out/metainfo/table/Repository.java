package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Persists publisher metadata in an ili2pg-generated PostgreSQL schema. */
public class Repository {
    private static final String MODEL = "SO_AGI_Publisher_Meta_20260623.Publisher.";
    private static final String PUBLICATION = MODEL + "Publication";
    private static final String PART = MODEL + "Part";
    private static final String PUBLICATION_PART = MODEL + "Publication_Part";
    private static final String EXPORTED_FORMAT = MODEL + "ExportedFormat";
    private static final String PUBLICATION_EXPORTED_FORMAT = MODEL + "Publication_ExportedFormat";
    private static final String TOPIC = "SO_AGI_Publisher_Meta_20260623.Publisher";

    public void write(Connection connection, String schema, Mapper.PublicationTree publicationTree) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(publicationTree, "publicationTree");
        LocalDate publicationDate = Objects.requireNonNull(publicationTree.getPublicationDate(), "publicationDate");
        SchemaMapping mapping = SchemaMapping.read(connection, schema);
        Long basketId = mapping.hasBasketColumn ? Long.valueOf(resolveBasketId(connection, mapping)) : null;

        Long existingPublicationId = findPublicationId(connection, mapping, publicationTree.getPublicationIdent(), publicationDate);
        if (existingPublicationId != null) {
            deletePublicationGraph(connection, mapping, existingPublicationId.longValue());
        }

        long publicationId = insertPublication(connection, mapping, publicationTree.getPublicationIdent(), publicationDate, basketId);
        for (Mapper.PartRow part : publicationTree.getParts()) {
            long partId = insertPart(connection, mapping, part.getPartIdent(), publicationDate,
                    mapping.publicationPartEmbedded ? Long.valueOf(publicationId) : null, basketId);
            if (!mapping.publicationPartEmbedded) {
                insertAssociation(connection, mapping.publicationPartTable, mapping.publicationPartPublicationColumn,
                        mapping.publicationPartPartColumn, publicationId, partId);
            }
        }
        for (String format : publicationTree.getArtifactTypes()) {
            long formatId = insertFormat(connection, mapping, format,
                    mapping.publicationFormatEmbedded ? Long.valueOf(publicationId) : null, basketId);
            if (!mapping.publicationFormatEmbedded) {
                insertAssociation(connection, mapping.publicationFormatTable, mapping.publicationFormatPublicationColumn,
                        mapping.publicationFormatFormatColumn, publicationId, formatId);
            }
        }
    }

    /** Retention is not part of the publication-state model yet. */
    public List<String> findPublicationIdsToDelete(Connection connection, int maxPublicationRecords) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        if (maxPublicationRecords < 0) {
            throw new IllegalArgumentException("maxPublicationRecords must not be negative");
        }
        throw new UnsupportedOperationException("Publication metadata retention is not implemented yet");
    }

    /** Retention is not part of the publication-state model yet. */
    public void deletePublicationTree(Connection connection, String publicationIdent) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(publicationIdent, "publicationIdent");
        throw new UnsupportedOperationException("Publication metadata retention is not implemented yet");
    }

    private Long findPublicationId(Connection connection, SchemaMapping mapping, String dataIdent, LocalDate pubDate)
            throws SQLException {
        String sql = "SELECT " + idColumn() + " FROM " + mapping.publicationTable + " WHERE "
                + mapping.publicationDataIdentColumn + "=? AND " + mapping.publicationDateColumn + "=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dataIdent);
            statement.setDate(2, Date.valueOf(pubDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                long id = resultSet.getLong(1);
                if (resultSet.next()) {
                    throw new SQLException("metadata schema contains duplicate Publication rows for " + dataIdent + " / "
                            + pubDate);
                }
                return Long.valueOf(id);
            }
        }
    }

    private void deletePublicationGraph(Connection connection, SchemaMapping mapping, long publicationId) throws SQLException {
        List<Long> partIds = mapping.publicationPartEmbedded
                ? findIdsByPublicationId(connection, mapping.partTable, mapping.publicationPartPublicationColumn, publicationId)
                : findLinkedIds(connection, mapping.publicationPartTable, mapping.publicationPartPartColumn,
                        mapping.publicationPartPublicationColumn, publicationId);
        List<Long> formatIds = mapping.publicationFormatEmbedded
                ? findIdsByPublicationId(connection, mapping.exportedFormatTable, mapping.publicationFormatPublicationColumn,
                        publicationId)
                : findLinkedIds(connection, mapping.publicationFormatTable, mapping.publicationFormatFormatColumn,
                        mapping.publicationFormatPublicationColumn, publicationId);
        deleteByPublicationId(connection, mapping.publicationPartEmbedded ? mapping.partTable : mapping.publicationPartTable,
                mapping.publicationPartPublicationColumn, publicationId);
        deleteByPublicationId(connection,
                mapping.publicationFormatEmbedded ? mapping.exportedFormatTable : mapping.publicationFormatTable,
                mapping.publicationFormatPublicationColumn, publicationId);
        deleteById(connection, mapping.publicationTable, publicationId);
        if (!mapping.publicationPartEmbedded) {
            deleteOrphanRows(connection, mapping.partTable, partIds);
        }
        if (!mapping.publicationFormatEmbedded) {
            deleteOrphanRows(connection, mapping.exportedFormatTable, formatIds);
        }
    }

    private List<Long> findLinkedIds(Connection connection, String associationTable, String targetColumn,
            String publicationColumn, long publicationId) throws SQLException {
        List<Long> ids = new ArrayList<Long>();
        String sql = "SELECT " + targetColumn + " FROM " + associationTable + " WHERE " + publicationColumn + "=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, publicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ids.add(Long.valueOf(resultSet.getLong(1)));
                }
            }
        }
        return ids;
    }

    private List<Long> findIdsByPublicationId(Connection connection, String table, String publicationColumn,
            long publicationId) throws SQLException {
        List<Long> ids = new ArrayList<Long>();
        String sql = "SELECT " + idColumn() + " FROM " + table + " WHERE " + publicationColumn + "=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, publicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ids.add(Long.valueOf(resultSet.getLong(1)));
                }
            }
        }
        return ids;
    }

    private void deleteByPublicationId(Connection connection, String table, String publicationColumn, long publicationId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + table + " WHERE " + publicationColumn + "=?")) {
            statement.setLong(1, publicationId);
            statement.executeUpdate();
        }
    }

    private void deleteById(Connection connection, String table, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE " + idColumn() + "=?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private void deleteOrphanRows(Connection connection, String table, List<Long> ids) throws SQLException {
        for (Long id : ids) {
            deleteById(connection, table, id.longValue());
        }
    }

    private long insertPublication(Connection connection, SchemaMapping mapping, String dataIdent, LocalDate pubDate, Long basketId)
            throws SQLException {
        String basketColumn = basketId == null ? "" : ", \"t_basket\"";
        String basketValue = basketId == null ? "" : ", ?";
        return insertReturningId(connection, "INSERT INTO " + mapping.publicationTable + " ("
                + mapping.publicationDataIdentColumn + ", " + mapping.publicationDateColumn + basketColumn + ") VALUES (?, ?" + basketValue + ")",
                statement -> {
                    statement.setString(1, dataIdent);
                    statement.setDate(2, Date.valueOf(pubDate));
                    if (basketId != null) statement.setLong(3, basketId.longValue());
                });
    }

    private long insertPart(Connection connection, SchemaMapping mapping, String partIdent, LocalDate pubDate, Long publicationId, Long basketId)
            throws SQLException {
        String columns = mapping.partIdentColumn + ", " + mapping.partDateColumn;
        String values = "?, ?";
        if (publicationId != null) {
            columns += ", " + mapping.publicationPartPublicationColumn;
            values += ", ?";
        }
        if (basketId != null) { columns += ", \"t_basket\""; values += ", ?"; }
        return insertReturningId(connection, "INSERT INTO " + mapping.partTable + " (" + columns + ") VALUES (" + values + ")", statement -> {
                    statement.setString(1, partIdent);
                    statement.setDate(2, Date.valueOf(pubDate));
                    int parameter = 3;
                    if (publicationId != null) statement.setLong(parameter++, publicationId.longValue());
                    if (basketId != null) statement.setLong(parameter, basketId.longValue());
                });
    }

    private long insertFormat(Connection connection, SchemaMapping mapping, String format, Long publicationId, Long basketId)
            throws SQLException {
        String columns = mapping.formatColumn;
        String values = "?";
        if (publicationId != null) {
            columns += ", " + mapping.publicationFormatPublicationColumn;
            values += ", ?";
        }
        if (basketId != null) { columns += ", \"t_basket\""; values += ", ?"; }
        return insertReturningId(connection, "INSERT INTO " + mapping.exportedFormatTable + " (" + columns + ") VALUES ("
                + values + ")", statement -> {
                    statement.setString(1, format);
                    int parameter = 2;
                    if (publicationId != null) statement.setLong(parameter++, publicationId.longValue());
                    if (basketId != null) statement.setLong(parameter, basketId.longValue());
                });
    }

    private void insertAssociation(Connection connection, String table, String publicationColumn, String targetColumn,
            long publicationId, long targetId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table + " (" + publicationColumn
                + ", " + targetColumn + ") VALUES (?, ?)")) {
            statement.setLong(1, publicationId);
            statement.setLong(2, targetId);
            statement.executeUpdate();
        }
    }

    private long insertReturningId(Connection connection, String sql, StatementBinder binder) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql + " RETURNING " + idColumn())) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("metadata insert did not return " + idColumn());
                }
                return resultSet.getLong(1);
            }
        }
    }

    private long resolveBasketId(Connection connection, SchemaMapping mapping) throws SQLException {
        String basketTable = qualified(mapping.schema, "t_ili2db_basket");
        try (PreparedStatement statement = connection.prepareStatement("SELECT " + idColumn() + " FROM " + basketTable + " WHERE \"topic\"=?")) {
            statement.setString(1, TOPIC);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getLong(1);
            }
        }
        String datasetTable = qualified(mapping.schema, "t_ili2db_dataset");
        long datasetId = nextId(connection, datasetTable);
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + datasetTable
                + " (\"t_id\", \"datasetname\") VALUES (?, ?)")) {
            statement.setLong(1, datasetId);
            statement.setString(2, "publisher-metadata");
            statement.executeUpdate();
        }
        long basketId = nextId(connection, basketTable);
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + basketTable
                + " (\"t_id\", \"dataset\", \"topic\", \"attachmentkey\") VALUES (?, ?, ?, ?)")) {
            statement.setLong(1, basketId);
            statement.setLong(2, datasetId);
            statement.setString(3, TOPIC);
            statement.setString(4, "publisher-metadata");
            statement.executeUpdate();
        }
        return basketId;
    }

    private long nextId(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(
                "SELECT COALESCE(MAX(" + idColumn() + "), 0) + 1 FROM " + table)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private static String idColumn() {
        return quote("t_id");
    }

    private static String qualified(String schema, String table) {
        return quote(schema) + "." + quote(table);
    }

    private static String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private static final class SchemaMapping {
        private final String publicationTable;
        private final String partTable;
        private final String publicationPartTable;
        private final String exportedFormatTable;
        private final String publicationFormatTable;
        private final String publicationDataIdentColumn;
        private final String publicationDateColumn;
        private final String partIdentColumn;
        private final String partDateColumn;
        private final String formatColumn;
        private final String publicationPartPublicationColumn;
        private final String publicationPartPartColumn;
        private final String publicationFormatPublicationColumn;
        private final String publicationFormatFormatColumn;
        private final boolean publicationPartEmbedded;
        private final boolean publicationFormatEmbedded;
        private final String schema;
        private final boolean hasBasketColumn;

        private SchemaMapping(Connection connection, String schema) throws SQLException {
            this.schema = schema;
            publicationTable = table(connection, schema, PUBLICATION);
            partTable = table(connection, schema, PART);
            // ili2pg stores these 1:n associations as foreign keys on their target
            // class tables, rather than creating the conceptual association tables.
            publicationPartTable = partTable;
            exportedFormatTable = table(connection, schema, EXPORTED_FORMAT);
            publicationFormatTable = exportedFormatTable;
            publicationDataIdentColumn = column(connection, schema, PUBLICATION + ".DataIdent");
            publicationDateColumn = column(connection, schema, PUBLICATION + ".PubDate");
            partIdentColumn = column(connection, schema, PART + ".PartIdent");
            partDateColumn = column(connection, schema, PART + ".PubDate");
            formatColumn = column(connection, schema, EXPORTED_FORMAT + ".Format");
            publicationPartPublicationColumn = associationColumn(connection, schema, publicationPartTable,
                    PUBLICATION_PART + ".Publication_R", "Publication_R");
            publicationPartEmbedded = publicationPartTable.equals(partTable);
            publicationPartPartColumn = publicationPartEmbedded ? null : associationColumn(connection, schema,
                    publicationPartTable, PUBLICATION_PART + ".Part_R", "Part_R");
            publicationFormatPublicationColumn = associationColumn(connection, schema, publicationFormatTable,
                    PUBLICATION_EXPORTED_FORMAT + ".Publication_R", "Publication_R");
            publicationFormatEmbedded = publicationFormatTable.equals(exportedFormatTable);
            publicationFormatFormatColumn = publicationFormatEmbedded ? null : associationColumn(connection, schema,
                    publicationFormatTable, PUBLICATION_EXPORTED_FORMAT + ".Format_R", "Format_R");
            hasBasketColumn = hasColumn(connection, schema, publicationTable, "t_basket");
        }

        private static SchemaMapping read(Connection connection, String schema) throws SQLException {
            return new SchemaMapping(connection, requiredSchema(schema));
        }

        private static String table(Connection connection, String schema, String iliName) throws SQLException {
            String sql = "SELECT " + quote("sqlname") + " FROM " + qualified(schema, "t_ili2db_classname")
                    + " WHERE " + quote("iliname") + "=?";
            return qualified(schema, queryName(connection, sql, iliName, "table"));
        }

        private static String column(Connection connection, String schema, String iliName) throws SQLException {
            String sql = "SELECT " + quote("sqlname") + " FROM " + qualified(schema, "t_ili2db_attrname")
                    + " WHERE " + quote("iliname") + "=?";
            return quote(queryName(connection, sql, iliName, "column"));
        }

        /**
         * ili2pg records the owning association role in t_ili2db_attrname, but not
         * necessarily its opposite role. Resolve the latter against the physical
         * association table produced by ili2pg instead of assuming a table name.
         */
        private static String associationColumn(Connection connection, String schema, String qualifiedTable,
                String iliName, String roleName) throws SQLException {
            try {
                return column(connection, schema, iliName);
            } catch (SQLException missingMapping) {
                String physicalName = roleName.toLowerCase(java.util.Locale.ROOT);
                String sql = "SELECT 1 FROM information_schema.columns WHERE table_schema=? AND table_name=? "
                        + "AND column_name=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, schema);
                    statement.setString(2, unquoteTableName(qualifiedTable));
                    statement.setString(3, physicalName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return quote(physicalName);
                        }
                    }
                }
                throw missingMapping;
            }
        }

        private static boolean hasColumn(Connection connection, String schema, String qualifiedTable, String column)
                throws SQLException {
            String sql = "SELECT 1 FROM information_schema.columns WHERE table_schema=? AND table_name=? AND column_name=?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                statement.setString(2, unquoteTableName(qualifiedTable));
                statement.setString(3, column);
                try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next(); }
            }
        }

        private static String unquoteTableName(String qualifiedTable) {
            int separator = qualifiedTable.lastIndexOf(".\"");
            return qualifiedTable.substring(separator + 2, qualifiedTable.length() - 1).replace("\"\"", "\"");
        }

        private static String queryName(Connection connection, String sql, String iliName, String kind) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, iliName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("ili2pg metadata has no " + kind + " mapping for " + iliName);
                    }
                    String value = resultSet.getString(1);
                    if (resultSet.next()) {
                        throw new SQLException("ili2pg metadata has multiple " + kind + " mappings for " + iliName);
                    }
                    return value;
                }
            }
        }

        private static String requiredSchema(String schema) {
            if (schema == null || schema.trim().isEmpty()) {
                throw new IllegalArgumentException("metadataSchema must not be blank");
            }
            return schema.trim();
        }
    }
}
