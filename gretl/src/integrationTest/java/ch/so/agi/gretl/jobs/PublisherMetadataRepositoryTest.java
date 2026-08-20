package ch.so.agi.gretl.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ch.so.agi.gretl.steps.publisher.out.metainfo.table.Mapper;
import ch.so.agi.gretl.steps.publisher.out.metainfo.table.Repository;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

/** Verifies the writer against the ili2pg schema downloaded from the versioned model URL. */
@Testcontainers
public class PublisherMetadataRepositoryTest {
    private static final String SCHEMA = "publisher_meta";
    private static final String MODEL_PREFIX = "SO_AGI_Publisher_Meta_20260623.Publisher.";

    @Container
    public static PostgreSQLContainer<?> postgres = (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS);

    @Test
    public void replacesSameDaySnapshotInDownloadedModelSchema() throws Exception {
        File jobDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/PublisherMetadataSchema");
        IntegrationTestUtil.executeTestRunner(jobDirectory,
                new GradleVariable[] { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI,
                        postgres.getJdbcUrl()) });

        try (Connection connection = IntegrationTestUtilSql.connectPG(postgres)) {
            Repository repository = new Repository();
            Mapper mapper = new Mapper();
            repository.write(connection, SCHEMA, mapper.map("ch.so.agi.demo", LocalDate.of(2026, 6, 23),
                    List.of("north", "south"), List.of("xtf", "gpkg")));
            connection.commit();

            repository.write(connection, SCHEMA, mapper.map("ch.so.agi.demo", LocalDate.of(2026, 6, 23),
                    List.of("north"), List.of("xtf", "dxf_geobau")));
            connection.commit();

            assertEquals(1, count(connection, MODEL_PREFIX + "Publication"));
            assertEquals(1, count(connection, MODEL_PREFIX + "Part"));
            assertEquals(2, count(connection, MODEL_PREFIX + "ExportedFormat"));
        }
    }

    private static int count(Connection connection, String iliClassName) throws Exception {
        String table = tableName(connection, iliClassName);
        try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM \"" + SCHEMA + "\".\""
                + table.replace("\"", "\"\"") + "\"" ); ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static String tableName(Connection connection, String iliClassName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT sqlname FROM \"" + SCHEMA
                + "\".\"t_ili2db_classname\" WHERE iliname=?")) {
            statement.setString(1, iliClassName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getString(1);
            }
        }
    }
}
