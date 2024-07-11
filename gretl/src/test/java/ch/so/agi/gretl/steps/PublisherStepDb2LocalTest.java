package ch.so.agi.gretl.steps;

import ch.ehi.basics.settings.Settings;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;
import ch.so.agi.gretl.testutil.TestUtil;
import org.interlis2.validator.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static ch.ehi.ili2db.gui.Config.BASKET_HANDLING_READWRITE;
import static ch.so.agi.gretl.steps.AbstractPublisherStepTest.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class PublisherStepDb2LocalTest {

    private static final String DM01AVCH24LV95D = "DM01AVCH24LV95D";
    private static final Path localTestOut = Paths.get("build").resolve("out");
    
    @Container
    public static PostgreSQLContainer<?> postgres = System.getProperty("dbUrl") == null
            ? (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                .withDatabaseName(TestUtil.PG_DB_NAME)
                .withUsername(TestUtil.PG_DDLUSR_USR)
                .withPassword(TestUtil.PG_DDLUSR_PWD)
                .withInitScript(TestUtil.PG_INIT_SCRIPT_PATH)
                .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2))
            : null;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String dbSchema;
    private Config config;

    public PublisherStepDb2LocalTest() {
        this.dbUrl = System.getProperty("dbUrl", postgres != null ? postgres.getJdbcUrl() : null);
        this.dbUser = System.getProperty("dbusr", TestUtil.PG_DDLUSR_USR);
        this.dbPassword = System.getProperty("dbPassword", TestUtil.PG_DDLUSR_PWD);
        this.dbSchema = "publisher";
    }

    @BeforeEach
    public void before() {
        this.config = new Config();
        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR + AbstractPublisherStepTest.ILI_DIRS);
        config.setDburl(dbUrl);
        config.setDbusr(dbUser);
        config.setDbpwd(dbPassword);
        config.setDbschema(dbSchema);
        config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(AbstractPublisherStepTest.SRC_DATA_AV_FILENAME).toString());

        if (config.getXtffile() != null && Ili2db.isItfFilename(config.getXtffile())){
            config.setItfTransferfile(true);
        }

        config.setFunction(Config.FC_IMPORT);
        config.setDoImplicitSchemaImport(true);
        config.setCreateFk(Config.CREATE_FK_YES);
        config.setTidHandling(Config.TID_HANDLING_PROPERTY);
        config.setImportTid(true);
        config.setDefaultSrsCode("2056");
    }

    @AfterEach
    public void after() {
        this.config = null;
    }

    @Test
    @Tag("dbTest")
    public void db_allNew() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        String datasetName = "av";

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement()
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);
            initConfig(datasetName, BASKET_HANDLING_READWRITE, null);
            publishDataset(jdbcConnection, SRC_DATA_DATE_0, datasetName, null, false, null, null, null);
        }

        // verify
        {
            assertTrue(Files.exists(targetFolder));

            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));

            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }

    @Test
    @Tag("dbTest")
    public void db_allNew_modelsToPublish() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt=jdbcConnection.createStatement()
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);
            initConfig();
            publishDataset(jdbcConnection, SRC_DATA_DATE_0, null, DM01AVCH24LV95D, false, null, null, null);
        }

        // verify
        {
            assertTrue(Files.exists(targetFolder));

            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));

            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }

    @Test
    @Tag("dbTest")
    public void db_allNew_modelsToPublish_NotSimple_Fail() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement()
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);
            initConfig(null, BASKET_HANDLING_READWRITE, null);

            try {
                publishDataset(jdbcConnection, SRC_DATA_DATE_0, null, DM01AVCH24LV95D, false, null, null, null);
                fail("Expected IllegalArgumentException to be thrown");
            } catch (IllegalArgumentException ex) {
                assertEquals("modelsToPublish <DM01AVCH24LV95D> can only be used with simple models", ex.getMessage());
            }
        }
    }

    @Test
    @Tag("dbTest")
    public void db_UserFormats_allNew() throws Exception {
        final String datasetName = "simple";
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement()
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);
            initConfig(datasetName, BASKET_HANDLING_READWRITE, Arrays.asList("files", SRC_DATA_SIMPLE_FILENAME));
            publishDataset(jdbcConnection, SRC_DATA_DATE_0, datasetName, null, false, null, null, null);
        }
    }

    @Test
    @Tag("dbTest")
    public void db_UserFormats_firstHistory() throws Exception {
        {
            db_UserFormats_allNew();
        }

        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);

        final String datasetName="simple";

        try (Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            publishDataset(jdbcConnection, SRC_DATA_DATE_1, datasetName, null, true, null, null, null);
        }

        // verify
        {
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".xtf.zip")));
            assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
            assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));

            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_SIMPLE_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_1), PublisherStep.readPublishDate(targetFolderAktuell));
        }

        // verify history
        {
            assertTrue(Files.exists(targetFolder));

            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_0));
            assertTrue(Files.exists(targetFolderHistory));
            assertTrue(Files.exists(targetFolderHistory.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".xtf.zip")));
            assertFalse(Files.exists(targetFolderHistory.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
            assertFalse(Files.exists(targetFolderHistory.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            assertFalse(Files.exists(targetFolderHistory.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));

            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_SIMPLE_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderHistory));
        }

    }

    @Test
    @Tag("dbTest")
    public void db_UserFormats_ModelDir_AV() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        String datasetName="av";

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement();
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);
            initConfig(datasetName, BASKET_HANDLING_READWRITE, null);
            publishDataset(jdbcConnection, SRC_DATA_DATE_0, datasetName, null, true, null, null, null);
        }

        // verify
        {
            assertTrue(Files.exists(targetFolder));

            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));

            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }

    @Test
    @Tag("dbTest")
    public void db_UserFormats_NoModelDir_AV() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        String datasetName = "av";

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement();
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);
            initConfig(datasetName, BASKET_HANDLING_READWRITE, null);
            publishDataset(jdbcConnection, SRC_DATA_DATE_0, datasetName, null, true, null, null, null);
        }

        // verify
        {
            assertTrue(Files.exists(targetFolder));

            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));

            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }

    @Test
    @Tag("dbTest")
    public void db_regionsRegEx() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        List<String> publishedRegions = new ArrayList<>();

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement();
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);

            for (String datasetName : new String[] {"2501","2502"}) {
                initConfig(datasetName, BASKET_HANDLING_READWRITE, Arrays.asList("files", datasetName + ".itf"));
            }

            publishDataset(jdbcConnection, SRC_DATA_DATE_0, null, null, false, "[0-9][0-9][0-9][0-9]", null, publishedRegions);
        }

        // verify
        assertTrue(Files.exists(targetFolder));

        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        assertTrue(Files.exists(targetFolderAktuell));
        assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
        assertEquals(2,publishedRegions.size());
        for (String controlRegion:new String[] {"2501","2502"}) {
            assertTrue(publishedRegions.contains(controlRegion));
            assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
        }

        final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
        assertTrue(Files.exists(targetFolderAktuellMeta));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
        assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
    }

    @Test
    @Tag("dbTest")
    public void db_regionsList() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        List<String> publishedRegions = new ArrayList<>();

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement();
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);

            for (String datasetName : new String[] {"2501","2502"}) {
                initConfig(datasetName, BASKET_HANDLING_READWRITE, Arrays.asList("files", datasetName + ".itf"));
            }

            List<String> regions = Collections.singletonList("2501");
            publishDataset(jdbcConnection, SRC_DATA_DATE_0, null, null, false, null, regions, publishedRegions);
        }

        assertTrue(Files.exists(targetFolder));

        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        assertTrue(Files.exists(targetFolderAktuell));
        assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
        assertEquals(1,publishedRegions.size());
        for (String controlRegion : new String[] {"2501"}) {
            assertTrue(publishedRegions.contains(controlRegion));
            assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
        }

        final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
        assertTrue(Files.exists(targetFolderAktuellMeta));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
        assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
    }


    @Test
    @Tag("dbTest")
    public void db_regionsRegEx_UserFormats() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        List<String> publishedRegions=new ArrayList<>();

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement();
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);

            for (String datasetName : new String[] {"SimpleCoord23a","SimpleCoord23b"}) {
                initConfig(datasetName, BASKET_HANDLING_READWRITE, Arrays.asList("files", datasetName + ".xtf"));
            }

            publishDataset(jdbcConnection, SRC_DATA_DATE_0, null, null, true, "SimpleCoord23[a-z]", null, publishedRegions);
        }

        // verify
        {
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertEquals(2,publishedRegions.size());

            for (String controlRegion : new String[] {"SimpleCoord23a","SimpleCoord23b"}) {
                assertTrue(publishedRegions.contains(controlRegion));
                assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".xtf.zip")));
                assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));
                assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
                assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            }

            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_SIMPLE_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }

    @Test
    @Tag("dbTest")
    public void db_regionsRegEx_UserFormats_AV() throws Exception {
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        List<String> publishedRegions = new ArrayList<>();

        try (
            Connection jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = jdbcConnection.createStatement();
        ) {
            deleteOutputFolder(targetFolder);
            dropDbSchema(stmt);

            PgMain pgMain = new PgMain();
            pgMain.initConfig(config);

            for (String datasetName : new String[] {"2501","2502"}) {
                initConfig(datasetName, BASKET_HANDLING_READWRITE, Arrays.asList("files", datasetName + ".itf"));
            }

            publishDataset(jdbcConnection, SRC_DATA_DATE_0, null, null, true, "[0-9][0-9][0-9][0-9]", null, publishedRegions);
        }

        // verify
        {
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertEquals(2,publishedRegions.size());
            for(String controlRegion:new String[] {"2501","2502"}) {
                assertTrue(publishedRegions.contains(controlRegion));
                assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
                assertFalse(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));
                assertFalse(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
                assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            }
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }

    private void deleteOutputFolder(Path targetFolder) throws IOException {
        Objects.requireNonNull(targetFolder);
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }
    }

    private void initConfig() throws Ili2dbException {
        initConfig(null, null, null);
    }

    private void initConfig(String datasetName, String basketHandlingType, List<String> filePaths) throws Ili2dbException {
        PgMain pgMain = new PgMain();
        pgMain.initConfig(config);

        if (filePaths != null && !filePaths.isEmpty()) {
            Path resultPath = resolvePath(SRC_TEST_DATA, filePaths);
            config.setXtffile(resultPath.toString());

            if (config.getXtffile() != null && Ili2db.isItfFilename(config.getXtffile())){
                config.setItfTransferfile(true);
            }
        }

        if (datasetName != null) {
            config.setDatasetName(datasetName);
        }

        config.setBasketHandling(basketHandlingType);

        Ili2db.readSettingsFromDb(config);
        Ili2db.run(config, null);
    }

    public static Path resolvePath(String basePath, List<String> elements) {
        Path path = Paths.get(basePath);
        for (String element : elements) {
            path = path.resolve(element);
        }
        return path;
    }

    private void publishDataset(Connection jdbcConnection, Date dataIdent, String datasetName, String modelsToPublish, boolean userFormats, String regionRegex, List<String> regionsToPublish, List<String> publishedRegions) throws Exception {
        Path targetPath = localTestOut.toAbsolutePath();
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);

        PublisherStep step = new PublisherStep();
        step.publishDatasetFromDb(
                dataIdent,
                AbstractPublisherStepTest.SRC_DATA_IDENT,
                jdbcConnection,
                dbSchema,
                datasetName,
                modelsToPublish,
                null,
                userFormats,
                targetPath,
                regionRegex,
                regionsToPublish,
                publishedRegions,
                null,
                null,
                settings,
                localTestOut,
                null
        );
    }

    private void dropDbSchema(Statement statement) throws SQLException {
        statement.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
    }
}
