package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestTags;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@Tag(TestTags.DB_TEST)
public class DatabaseDocumentExportStepTest {

    @Container
    public PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                .withDatabaseName(TestUtil.PG_DB_NAME)
                .withUsername(TestUtil.PG_DDLUSR_USR)
                .withPassword(TestUtil.PG_DDLUSR_PWD)
                .withInitScript(TestUtil.PG_INIT_SCRIPT_PATH)
                .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    private final GretlLogger log;
    private final String schemaName;
    private final String tableName;
    private final String columnName;
    private Connector connector;

    @TempDir
    public Path folder;

    public DatabaseDocumentExportStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
        this.schemaName = "ada_denkmalschutz";
        this.tableName = "fachapplikation_rechtsvorschrift_link";
        this.columnName = "multimedia_link";
    }

    // TODO: 
    // Die mühsamen self-signed Zertifikate des AIO können schlecht getestet werden. A) Nur intern B) Filenamen können sich ändern.
    // https://artplus.verw.rootso.org/MpWeb-apSolothurnDenkmal/download/2W8v0qRZQBC0ahDnZGut3Q?mode=gis
    // Manuell getestet...

    @BeforeEach
    public void before() throws Exception {
        this.connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);
    }

    @AfterEach
    public void after() throws Exception {
        if (!this.connector.isClosed()) {
            this.connector.close();
        }
    }
    
    @Test
    public void exportDocuments_Ok() throws Exception {
        try (Connection con = connector.connect(); Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            initializeSchema(con, stmt);
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
            con.commit();

            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, folder.toAbsolutePath().toString(), "ada_", "pdf");

            File resultFile = Paths.get(folder.toAbsolutePath().toString(), "ada_ilimodels.xml.pdf").toFile();
            assertTrue(resultFile.exists());
            assertTrue(resultFile.length() > 60L);

            String content = new String(Files.readAllBytes(Paths.get(resultFile.getAbsolutePath())));
            assertTrue(content.contains("IliRepository"));
        }
    }

    // It does not work if there is a redirect from http to https. The response code will be 302.
    // https://stackoverflow.com/questions/1884230/httpurlconnection-doesnt-follow-redirect-from-http-to-https
    @Test
    public void exportDocuments_Fail() throws Exception {
        try (Connection con = connector.connect(); Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            initializeSchema(con, stmt);
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('https://geo.so.ch/fubar');");
            con.commit();

            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, folder.toAbsolutePath().toString(), "ada_", "pdf");

            File resultFile = Paths.get(folder.toAbsolutePath().toString(), "ada_ilimodels.xml.pdf").toFile();
            assertFalse(resultFile.exists());
        }
    }
    
    @Test
    public void exportDocuments_WithoutPrefix_Ok() throws Exception {
        log.debug("targetDir: " + folder.toAbsolutePath());

        try (Connection con = connector.connect(); Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            initializeSchema(con, stmt);

            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
            con.commit();

            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, folder.toAbsolutePath().toString(), null, "pdf");

            File resultFile = Paths.get(folder.toAbsolutePath().toString(), "ilimodels.xml.pdf").toFile();
            assertTrue(resultFile.exists());
            assertTrue(resultFile.length() > 60L);

            String content = new String(Files.readAllBytes(Paths.get(resultFile.getAbsolutePath())));
            assertTrue(content.contains("IliRepository"));
        }
    }
    
    @Test
    public void exportDocuments_WithoutFileNameExtension_Ok() throws Exception {
        log.debug("targetDir: " + folder.toAbsolutePath());

        try (Connection con = connector.connect(); Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            initializeSchema(con, stmt);
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
            con.commit();

            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, folder.toAbsolutePath().toString(), null, null);

            File resultFile = Paths.get(folder.toAbsolutePath().toString(), "ilimodels.xml").toFile();
            assertTrue(resultFile.exists());
            assertTrue(resultFile.length() > 60L);

            String content = new String(Files.readAllBytes(Paths.get(resultFile.getAbsolutePath())));
            assertTrue(content.contains("IliRepository"));
        }
    }

    /**
     * Creates a schema and facghapplikation table to test against
     * @param connection the db connection
     * @param statement the statement
     * @throws Exception if execution fails
     *
     * <ul>
     * <li><a href="https://artplus.verw.rootso.org/MpWeb-apSolothurnDenkmal/download/2W8v0qRZQBC0ahDnZGut3Q?mode=gis">Docs</a></li>
     * <li><a href="http://geo.so.ch/models/ilimodels.xml">Docs</a></li>
     * <li><a href="http://models.geo.admin.ch/ilimodels.xml">Docs</a></li>
     * </ul>
     */
    private void initializeSchema(Connection connection, Statement statement) throws Exception {
        statement.execute("DROP SCHEMA IF EXISTS " + this.schemaName + " CASCADE;");
        statement.execute("CREATE SCHEMA " + this.schemaName + ";");
        statement.execute("CREATE TABLE " + this.schemaName + "." + this.tableName + " (id serial, " + this.columnName + " text);");
        connection.commit();
    }
}
