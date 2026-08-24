package ch.so.agi.gretl.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.gretl.tasks.Ili2pgImportSchema;
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
    private static final List<String> PUBLISHER_JOBS = List.of("PublisherXtfList", "PublisherXtfRegex",
            "PublisherDbModel", "PublisherDbTopic", "PublisherDbBasket", "PublisherDbDataset",
            "PublisherDbDatasetRegex", "PublisherXtfRegexRemote");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS);

    private final Path jobsDirectory = Path.of(System.getProperty("user.dir"), "src", "integrationTest", "jobs");

    @BeforeEach
    void prepareJobs() throws Exception {
        for (String jobName : PUBLISHER_JOBS) {
            Path jobDirectory = jobDirectory(jobName);
            deleteTree(jobDirectory.resolve("build"));
            copyFixture(jobDirectory, "integrationTest/jobs/Ili2pgExportDatasets/Beispiel2.ili", "Beispiel2.ili");
            copyFixture(jobDirectory, "integrationTest/jobs/Ili2pgExportDatasets/Beispiel2a.xtf", "Beispiel2a.xtf");
            copyFixture(jobDirectory, "integrationTest/jobs/Ili2pgExportDatasets/Beispiel2b.xtf", "Beispiel2b.xtf");
            copyFixture(jobDirectory, "test/resources/data/publisher/ili/SimpleCoord23.ili", "SimpleCoord23.ili");
            copyFixture(jobDirectory, "test/resources/data/publisher/files/SimpleCoord23a.xtf", "SimpleCoord23a.xtf");
            copyFixture(jobDirectory, "test/resources/data/publisher/files/SimpleCoord23b.xtf", "SimpleCoord23b.xtf");
        }
        dropSchema(SOURCE_SCHEMA);
        dropSchema(SIMPLE_SOURCE_SCHEMA);
        dropSchema(METADATA_SCHEMA);
    }

    @Test
    void xtfListPublishesOneTransferToLocalFolder() throws Exception {
        run("PublisherXtfList", "publish", null);

        Path publication = localPublication("PublisherXtfList", "ch.so.agi.publisher.xtf-list");
        assertArchives(publication, 1, ".xtf.zip");
        assertLocalMetadata(publication);
    }

    @Test
    void xtfRegexPublishesMatchingTransfersToLocalFolder() throws Exception {
        run("PublisherXtfRegex", "publish", null);

        Path publication = localPublication("PublisherXtfRegex", "ch.so.agi.publisher.xtf-regex");
        assertArchives(publication, 2, ".xtf.zip");
        assertLocalMetadata(publication);
    }

    @Test
    void dbValuesModelPublishesToLocalFolder() throws Exception {
        assertDbValuesPublication("PublisherDbModel", "ch.so.agi.publisher.db-model");
    }

    @Test
    void dbValuesTopicPublishesToLocalFolder() throws Exception {
        assertDbValuesPublication("PublisherDbTopic", "ch.so.agi.publisher.db-topic");
    }

    @Test
    @Disabled("Won't fix: third-party ili2db/ilivalidator INFO logging is not controlled by Publisher.")
    void dbTopicPublisherInfoOutputContainsOnlyPublisherProgressSummaries() throws Exception {
        String output = IntegrationTestUtil.executeTestRunnerAndCaptureOutput(jobDirectory("PublisherDbTopic").toFile(),
                databaseVariables(), "publish", "INFO");
        String publishOutput = output.substring(output.indexOf("> Task :publish"));

        assertTrue(publishOutput.contains("Database selection export: exported 10 object(s)"));
        assertFalse(publishOutput.contains("Info: ili2pg-"));
        assertFalse(publishOutput.contains("Info: compile models..."));
        assertFalse(publishOutput.contains("Info: ...export done"));
        assertFalse(publishOutput.contains("Info: ilivalidator-"));
    }

    @Test
    void dbValuesBasketPublishesToLocalFolder() throws Exception {
        run("PublisherDbBasket", "setupData", databaseVariables());
        assignBasketIdentifiers();
        run("PublisherDbBasket", "publish", concat(databaseVariables(), new GradleVariable[] {
                GradleVariable.newGradleProperty("publisherBasket", firstBasketIdentifier()) }));
        Path publication = localPublication("PublisherDbBasket", "ch.so.agi.publisher.db-basket");
        assertArchives(publication, 1, ".xtf.zip");
        assertLocalMetadata(publication);
    }

    @Test
    void dbValuesDatasetPublishesToLocalFolder() throws Exception {
        assertDbValuesPublication("PublisherDbDataset", "ch.so.agi.publisher.db-dataset");
    }

    @Test
    void dbRegexDatasetPublishesMatchingDatasetsToLocalFolder() throws Exception {
        run("PublisherDbDatasetRegex", "publish", databaseVariables());

        Path publication = localPublication("PublisherDbDatasetRegex", "ch.so.agi.publisher.db-dataset-regex");
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
            Path jobDirectory = jobDirectory("PublisherXtfRegexRemote");
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
            run("PublisherXtfRegexRemote", "publish", variables);

            Path publication = remoteRoot.resolve("ch.so.agi.publisher.remote/aktuell");
            assertArchives(publication, 2, ".xtf.zip");
            assertArchives(publication, 2, "gpkg.zip");
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

    private void assertDbValuesPublication(String jobName, String dataIdent) throws Exception {
        run(jobName, "publish", databaseVariables());
        Path publication = localPublication(jobName, dataIdent);
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

    private Path localPublication(String jobName, String dataIdent) {
        return jobDirectory(jobName).resolve("build/local").resolve(dataIdent).resolve("aktuell");
    }

    private void assertArchives(Path publication, int expectedCount, String suffix) throws IOException {
        try (var files = Files.list(publication)) {
            assertEquals(expectedCount, files.filter(path -> path.getFileName().toString().endsWith(suffix)).count());
        }
    }

    private void importMetadataSchema() throws IOException {
        Project project = ProjectBuilder.builder().build();
        Ili2pgImportSchema task = project.getTasks().create("importMetadataSchema", Ili2pgImportSchema.class);
        task.getDatabase().set(List.of(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
        task.getDbschema().set(METADATA_SCHEMA);
        task.getModels().set("SO_AGI_Publisher_Meta_20260623");
        task.getCreateBasketCol().set(true);
        task.importSchema();
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

    private void run(String jobName, String taskName, GradleVariable[] variables) throws IOException {
        IntegrationTestUtil.executeTestRunner(jobDirectory(jobName).toFile(), variables, taskName);
    }

    private Path jobDirectory(String jobName) {
        return jobsDirectory.resolve(jobName);
    }

    private void copyFixture(Path jobDirectory, String relativeSource, String targetName) throws IOException {
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
