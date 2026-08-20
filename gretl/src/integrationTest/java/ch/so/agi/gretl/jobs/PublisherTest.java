package ch.so.agi.gretl.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/** End-to-end coverage of the canonical Publisher task's selectable source modes. */
@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("publisher-integration-postgres")
class PublisherTest {
    private static final String SOURCE_SCHEMA = "Beispiel2";
    private static final String SIMPLE_SOURCE_SCHEMA = "SimpleCoord23";
    private static final String METADATA_SCHEMA = "publisher_meta";
    private static final String METADATA_MODEL_PREFIX = "SO_AGI_Publisher_Meta_20260623.Publisher.";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS);

    private final Path jobDirectory = Path.of(System.getProperty("user.dir"), "src", "integrationTest", "jobs", "Publisher");

    @BeforeEach
    void prepareJob() throws Exception {
        deleteTree(jobDirectory.resolve("build"));
        copyFixture("integrationTest/jobs/Ili2pgExportDatasets/Beispiel2.ili", "Beispiel2.ili");
        copyFixture("integrationTest/jobs/Ili2pgExportDatasets/Beispiel2a.xtf", "Beispiel2a.xtf");
        copyFixture("integrationTest/jobs/Ili2pgExportDatasets/Beispiel2b.xtf", "Beispiel2b.xtf");
        copyFixture("test/resources/data/publisher/ili/SimpleCoord23.ili", "SimpleCoord23.ili");
        copyFixture("test/resources/data/publisher/files/SimpleCoord23a.xtf", "SimpleCoord23a.xtf");
        copyFixture("test/resources/data/publisher/files/SimpleCoord23b.xtf", "SimpleCoord23b.xtf");
        dropSchema(SOURCE_SCHEMA);
        dropSchema(SIMPLE_SOURCE_SCHEMA);
        dropSchema(METADATA_SCHEMA);
    }

    @Test
    void xtfListPublishesOneTransferToLocalFolder() throws Exception {
        run("publishXtfList");

        Path publication = localPublication("publishXtfList");
        assertArchives(publication, 1, ".xtf.zip");
        assertLocalMetadata(publication);
    }

    @Test
    void xtfRegexPublishesMatchingTransfersToLocalFolder() throws Exception {
        run("publishXtfRegex");

        Path publication = localPublication("publishXtfRegex");
        assertArchives(publication, 2, ".xtf.zip");
        assertLocalMetadata(publication);
    }

    @Test
    void dbValuesModelPublishesToLocalFolder() throws Exception {
        assertDbValuesPublication("publishDbModel");
    }

    @Test
    void dbValuesTopicPublishesToLocalFolder() throws Exception {
        assertDbValuesPublication("publishDbTopic");
    }

    @Test
    void dbValuesBasketPublishesToLocalFolder() throws Exception {
        run("setupSourceData", databaseVariables());
        assignBasketIdentifiers();
        run("publishDbBasket", concat(databaseVariables(), new GradleVariable[] {
                GradleVariable.newGradleProperty("publisherBasket", firstBasketIdentifier()) }));
        Path publication = localPublication("publishDbBasket");
        assertArchives(publication, 1, ".xtf.zip");
        assertLocalMetadata(publication);
    }

    @Test
    void dbValuesDatasetPublishesToLocalFolder() throws Exception {
        assertDbValuesPublication("publishDbDataset");
    }

    @Test
    void dbRegexDatasetPublishesMatchingDatasetsToLocalFolder() throws Exception {
        run("publishDbDatasetRegex", databaseVariables());

        Path publication = localPublication("publishDbDatasetRegex");
        assertArchives(publication, 2, ".xtf.zip");
        assertLocalMetadata(publication);
    }

    @Test
    void xtfRegexRemotePublicationWritesGpkgAndMetadata() throws Exception {
        importMetadataSchema();
        try (MockWebServer jsonServer = new MockWebServer()) {
            jsonServer.start();
            jsonServer.enqueue(new MockResponse().setResponseCode(200)
                    .setBody("[{\"ident\":\"ch.so.agi.publisher.remote\",\"title\":\"Publisher integration\"}]"));
            Path remoteRoot = jobDirectory.resolve("build/remote-target");
            GradleVariable[] variables = concat(databaseVariables(), new GradleVariable[] {
                    GradleVariable.newGradleProperty("pubDateDbUrl", POSTGRES.getJdbcUrl()),
                    GradleVariable.newGradleProperty("pubDateDbSchema", METADATA_SCHEMA),
                    GradleVariable.newGradleProperty("pubDateDbUser", POSTGRES.getUsername()),
                    GradleVariable.newGradleProperty("pubDateDbPass", POSTGRES.getPassword()),
                    GradleVariable.newGradleProperty("pupFolderPath", remoteRoot.toString()),
                    GradleVariable.newGradleProperty("pupFolderUser", "ignored"),
                    GradleVariable.newGradleProperty("pupFolderPass", "ignored"),
                    GradleVariable.newGradleProperty("jsonmetaAddress", jsonServer.url("/").toString()),
                    GradleVariable.newGradleProperty("jsonmetaBucket", "publication"),
                    GradleVariable.newGradleProperty("jsonmetaFileName", "metadata.json"),
                    GradleVariable.newGradleProperty("modelDir", jobDirectory.toString()),
                    GradleVariable.newGradleProperty("groomingConfigFilePath", jobDirectory.resolve("unused.json").toString()) });
            run("publishXtfRegexRemote", variables);

            Path publication = remoteRoot.resolve("ch.so.agi.publisher.remote/aktuell");
            assertArchives(publication, 2, ".xtf.zip");
            assertArchives(publication, 1, "gpkg.zip");
            Path meta = publication.resolve("meta");
            assertTrue(Files.isRegularFile(meta.resolve("SimpleCoord23.ili")));
            JsonNode json = new ObjectMapper().readTree(Files.readString(meta.resolve("metainfo.json")));
            assertEquals("Publisher integration", json.path("title").asText());
            assertFalse(Files.exists(meta.resolve("publishdate.json")));
            assertFalse(Files.exists(meta.resolve("datenbeschreibung.html")));
            assertEquals(1, metadataCount("Publication"));
            assertEquals(2, metadataCount("Part"));
            assertEquals(2, metadataCount("ExportedFormat"));
        }
    }

    private void assertDbValuesPublication(String taskName) throws Exception {
        run(taskName, databaseVariables());
        Path publication = localPublication(taskName);
        assertArchives(publication, 1, ".xtf.zip");
        assertLocalMetadata(publication);
    }

    private void assertLocalMetadata(Path publication) throws IOException {
        Path meta = publication.resolve("meta");
        assertTrue(Files.isRegularFile(meta.resolve("SimpleCoord23.ili"))
                || Files.isRegularFile(meta.resolve("Beispiel2.ili")));
        assertFalse(Files.exists(meta.resolve("metainfo.json")));
        assertFalse(Files.exists(meta.resolve("publishdate.json")));
        assertFalse(Files.exists(meta.resolve("datenbeschreibung.html")));
    }

    private Path localPublication(String taskName) {
        return jobDirectory.resolve("build/local").resolve(taskName).resolve("ch.so.agi.publisher." + taskName)
                .resolve("aktuell");
    }

    private void assertArchives(Path publication, int expectedCount, String suffix) throws IOException {
        try (var files = Files.list(publication)) {
            assertEquals(expectedCount, files.filter(path -> path.getFileName().toString().endsWith(suffix)).count());
        }
    }

    private void importMetadataSchema() throws IOException {
        File metadataJob = Path.of(System.getProperty("user.dir"), "src", "integrationTest", "jobs",
                "PublisherMetadataSchema").toFile();
        IntegrationTestUtil.executeTestRunner(metadataJob,
                new GradleVariable[] { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI,
                        POSTGRES.getJdbcUrl()) });
    }

    private GradleVariable[] databaseVariables() {
        return new GradleVariable[] { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI,
                POSTGRES.getJdbcUrl()) };
    }

    private int metadataCount(String classSuffix) throws Exception {
        try (Connection connection = IntegrationTestUtilSql.connectPG(POSTGRES);
                PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM \"" + METADATA_SCHEMA
                        + "\".\"" + tableName(connection, METADATA_MODEL_PREFIX + classSuffix) + "\"")) {
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private String firstBasketIdentifier() throws Exception {
        try (Connection connection = IntegrationTestUtilSql.connectPG(POSTGRES);
                PreparedStatement statement = connection.prepareStatement("SELECT t_ili_tid FROM " + SOURCE_SCHEMA
                        + ".t_ili2db_basket ORDER BY t_ili_tid LIMIT 1");
                ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getString(1);
        }
    }

    private void assignBasketIdentifiers() throws Exception {
        try (Connection connection = IntegrationTestUtilSql.connectPG(POSTGRES);
                PreparedStatement statement = connection.prepareStatement("UPDATE " + SOURCE_SCHEMA
                        + ".t_ili2db_basket SET t_ili_tid='basket-' || t_id WHERE t_ili_tid IS NULL")) {
            statement.executeUpdate();
            connection.commit();
        }
    }

    private String tableName(Connection connection, String iliClassName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT sqlname FROM \"" + METADATA_SCHEMA
                + "\".\"t_ili2db_classname\" WHERE iliname=?")) {
            statement.setString(1, iliClassName);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getString(1).replace("\"", "\"\"");
            }
        }
    }

    private void run(String taskName) throws IOException {
        run(taskName, null);
    }

    private void run(String taskName, GradleVariable[] variables) throws IOException {
        IntegrationTestUtil.executeTestRunner(jobDirectory.toFile(), variables, taskName);
    }

    private void copyFixture(String relativeSource, String targetName) throws IOException {
        Path source = Path.of(System.getProperty("GRETL_PROJECT_ABS_PATH"), "src").resolve(relativeSource);
        Files.copy(source, jobDirectory.resolve(targetName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private void dropSchema(String schema) throws Exception {
        try (Connection connection = IntegrationTestUtilSql.connectPG(POSTGRES);
                PreparedStatement statement = connection.prepareStatement("DROP SCHEMA IF EXISTS " + schema + " CASCADE")) {
            statement.execute();
            connection.commit();
        }
    }

    private static GradleVariable[] concat(GradleVariable[] left, GradleVariable[] right) {
        GradleVariable[] result = new GradleVariable[left.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }

    private static void deleteTree(Path path) throws IOException {
        if (Files.notExists(path)) {
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult postVisitDirectory(Path directory, IOException exception) throws IOException {
                Files.delete(directory); return FileVisitResult.CONTINUE;
            }
        });
    }
}
