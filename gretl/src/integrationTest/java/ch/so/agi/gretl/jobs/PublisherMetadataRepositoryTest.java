package ch.so.agi.gretl.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ch.so.agi.gretl.steps.publisher.out.metainfo.table.Mapper;
import ch.so.agi.gretl.steps.publisher.out.metainfo.table.Repository;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

/** Verifies the writer against the fixed publisher metadata schema. */
@Testcontainers
public class PublisherMetadataRepositoryTest {
    private static final String SCHEMA = "publisher_meta";

    @Container
    public static PostgreSQLContainer<?> postgres = (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS);

    @Test
    public void replacesSameDaySnapshotInFixedSchema() throws Exception {
        createMetadataSchema();

        try (Connection connection = IntegrationTestUtilSql.connectPG(postgres)) {
            Repository repository = new Repository();
            Mapper mapper = new Mapper();
            repository.write(connection, SCHEMA, mapper.map("ch.so.agi.demo", LocalDate.of(2026, 6, 23),
                    List.of("north", "south"), List.of("xtf", "gpkg")));
            connection.commit();

            repository.write(connection, SCHEMA, mapper.map("ch.so.agi.demo", LocalDate.of(2026, 6, 23),
                    List.of("north"), List.of("xtf", "dxf_geobau")));
            connection.commit();

            assertEquals(1, count(connection, "publicationrun"));
            assertEquals(0, count(connection, "part"));
            assertEquals(2, count(connection, "exportedformat"));
        }
    }

    @Test
    public void rejectsUnsupportedExportedFormat() throws Exception {
        createMetadataSchema();
        try (Connection connection = IntegrationTestUtilSql.connectPG(postgres)) {
            Repository repository = new Repository();
            Mapper mapper = new Mapper();

            assertThrows(IllegalArgumentException.class, () -> repository.write(connection, SCHEMA,
                    mapper.map("ch.so.agi.demo", LocalDate.of(2026, 6, 23), List.of("north"), List.of("csv"))));
        }
    }

    private void createMetadataSchema() throws Exception {
        try (Connection connection = IntegrationTestUtilSql.connectPG(postgres); Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
            statement.execute("CREATE SCHEMA " + SCHEMA);
            statement.execute("CREATE SEQUENCE " + SCHEMA + ".t_ili2db_seq");
            statement.execute("CREATE TABLE " + SCHEMA + ".publicationrun (t_id int8 DEFAULT nextval('" + SCHEMA
                    + ".t_ili2db_seq'::regclass) PRIMARY KEY, dataident varchar(256) NOT NULL, pubdate date NOT NULL)");
            statement.execute("CREATE TABLE " + SCHEMA + ".part (t_id int8 DEFAULT nextval('" + SCHEMA
                    + ".t_ili2db_seq'::regclass) PRIMARY KEY, partident varchar(256) NOT NULL, publication_r int8 NOT NULL REFERENCES "
                    + SCHEMA + ".publicationrun(t_id) DEFERRABLE INITIALLY DEFERRED)");
            statement.execute("CREATE TABLE " + SCHEMA + ".exportedformat (t_id int8 DEFAULT nextval('" + SCHEMA
                    + ".t_ili2db_seq'::regclass) PRIMARY KEY, aformat varchar(255) NOT NULL, publication_r int8 NOT NULL REFERENCES "
                    + SCHEMA + ".publicationrun(t_id) DEFERRABLE INITIALLY DEFERRED)");
            connection.commit();
        }
    }

    private static int count(Connection connection, String table) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM \"" + SCHEMA + "\".\""
                + table.replace("\"", "\"\"") + "\""); ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

}
