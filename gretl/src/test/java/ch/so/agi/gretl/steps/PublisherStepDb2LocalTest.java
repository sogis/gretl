package ch.so.agi.gretl.steps;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.interlis2.validator.Validator;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.ehi.basics.settings.Settings;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.testutil.DbTest;
import ch.so.agi.gretl.testutil.TestUtil;

public class PublisherStepDb2LocalTest {

    private static final String DM01AVCH24LV95D="DM01AVCH24LV95D";
    private static final Path localTestOut = Paths.get("build").resolve("out");
    
    @ClassRule
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

    public PublisherStepDb2LocalTest() {
        this.dbUrl = System.getProperty("dbUrl", postgres != null ? postgres.getJdbcUrl() : null);
        this.dbUser = System.getProperty("dbusr", TestUtil.PG_DDLUSR_USR);
        this.dbPassword = System.getProperty("dbPassword", TestUtil.PG_DDLUSR_PWD);
        this.dbSchema = "publisher";
    }

    @Category(DbTest.class)
    @Test
    public void db_allNew() throws Exception {
        final Date SRC_DATA_DATE = AbstractPublisherStepTest.SRC_DATA_DATE_0;
        
        Path targetFolder = localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        String datasetName="av";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dbUrl);
                    config.setDbusr(dbUser);
                    config.setDbpwd(dbPassword);
                    config.setDbschema(dbSchema);
                    config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(AbstractPublisherStepTest.SRC_DATA_AV_FILENAME).toString());
                    if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                        config.setItfTransferfile(true);
                    }
                    config.setFunction(Config.FC_IMPORT);
                    config.setDatasetName(datasetName);
                    config.setDoImplicitSchemaImport(true);
                    config.setCreateFk(Config.CREATE_FK_YES);
                    config.setTidHandling(Config.TID_HANDLING_PROPERTY);
                    config.setImportTid(true);
                    config.setDefaultSrsCode("2056");
                    config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                    Ili2db.readSettingsFromDb(config);
                    Ili2db.run(config,null);
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,datasetName,null,null,false, targetPath,null,null,null,null,null,settings,localTestOut,null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_allNew_modelsToPublish() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dbUrl);
                    config.setDbusr(dbUser);
                    config.setDbpwd(dbPassword);
                    config.setDbschema(dbSchema);
                    config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(AbstractPublisherStepTest.SRC_DATA_AV_FILENAME).toString());
                    if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                        config.setItfTransferfile(true);
                    }
                    config.setFunction(Config.FC_IMPORT);
                    config.setDoImplicitSchemaImport(true);
                    config.setCreateFk(Config.CREATE_FK_YES);
                    config.setTidHandling(Config.TID_HANDLING_PROPERTY);
                    config.setImportTid(true);
                    config.setDefaultSrsCode("2056");
                    config.setBasketHandling(null);
                    Ili2db.readSettingsFromDb(config);
                    Ili2db.run(config,null);
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,null,DM01AVCH24LV95D,null,false, targetPath,null,null,null,null,null,settings,localTestOut,null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_allNew_modelsToPublish_NotSimple_Fail() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dbUrl);
                    config.setDbusr(dbUser);
                    config.setDbpwd(dbPassword);
                    config.setDbschema(dbSchema);
                    config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(AbstractPublisherStepTest.SRC_DATA_AV_FILENAME).toString());
                    if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                        config.setItfTransferfile(true);
                    }
                    config.setFunction(Config.FC_IMPORT);
                    config.setDoImplicitSchemaImport(true);
                    config.setCreateFk(Config.CREATE_FK_YES);
                    config.setTidHandling(Config.TID_HANDLING_PROPERTY);
                    config.setImportTid(true);
                    config.setDefaultSrsCode("2056");
                    config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                    Ili2db.readSettingsFromDb(config);
                    Ili2db.run(config,null);
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            try {
                step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,null,DM01AVCH24LV95D,null,false, targetPath,null,null,null,null,null,settings,localTestOut,null);
                Assert.fail();
            }catch(IllegalArgumentException ex) {
                Assert.assertEquals("modelsToPublish <DM01AVCH24LV95D> can only be used with simple models", ex.getMessage());
            }
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_UserFormats_allNew() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        final String datasetName="simple";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dbUrl);
                    config.setDbusr(dbUser);
                    config.setDbpwd(dbPassword);
                    config.setDbschema(dbSchema);
                    config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(AbstractPublisherStepTest.SRC_DATA_SIMPLE_FILENAME).toString());
                    if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                        config.setItfTransferfile(true);
                    }
                    config.setFunction(Config.FC_IMPORT);
                    config.setDatasetName(datasetName);
                    config.setDoImplicitSchemaImport(true);
                    config.setCreateFk(Config.CREATE_FK_YES);
                    config.setTidHandling(Config.TID_HANDLING_PROPERTY);
                    config.setImportTid(true);
                    config.setDefaultSrsCode("2056");
                    config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                    Ili2db.readSettingsFromDb(config);
                    Ili2db.run(config,null);
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,datasetName,null,null,true, targetPath,null,null,null,null,null,settings,localTestOut, null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".xtf.zip")));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_SIMPLE_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_UserFormats_firstHistory() throws Exception {
        {
            db_UserFormats_allNew();
        }
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_1;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        final String datasetName="simple";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,datasetName,null,null,true, targetPath,null,null,null,null,null,settings,localTestOut, null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".xtf.zip")));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_SIMPLE_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(AbstractPublisherStepTest.SRC_DATA_DATE_0));
            Assert.assertTrue(Files.exists(targetFolderHistory));
            Assert.assertTrue(Files.exists(targetFolderHistory.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".xtf.zip")));
            Assert.assertFalse(Files.exists(targetFolderHistory.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
            Assert.assertFalse(Files.exists(targetFolderHistory.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            Assert.assertFalse(Files.exists(targetFolderHistory.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));
            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_SIMPLE_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(AbstractPublisherStepTest.SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderHistory));
        }
        
    }
    @Category(DbTest.class)
    @Test
    public void db_UserFormats_ModelDir_AV() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        String datasetName="av";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dbUrl);
                    config.setDbusr(dbUser);
                    config.setDbpwd(dbPassword);
                    config.setDbschema(dbSchema);
                    config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(AbstractPublisherStepTest.SRC_DATA_AV_FILENAME).toString());
                    if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                        config.setItfTransferfile(true);
                    }
                    config.setFunction(Config.FC_IMPORT);
                    config.setDatasetName(datasetName);
                    config.setDoImplicitSchemaImport(true);
                    config.setCreateFk(Config.CREATE_FK_YES);
                    config.setTidHandling(Config.TID_HANDLING_PROPERTY);
                    config.setImportTid(true);
                    config.setDefaultSrsCode("2056");
                    config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                    Ili2db.readSettingsFromDb(config);
                    Ili2db.run(config,null);
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,datasetName,null,null,true, targetPath,null,null,null,null,null,settings,localTestOut, null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_UserFormats_NoModelDir_AV() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        String datasetName="av";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dbUrl);
                    config.setDbusr(dbUser);
                    config.setDbpwd(dbPassword);
                    config.setDbschema(dbSchema);
                    config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(AbstractPublisherStepTest.SRC_DATA_AV_FILENAME).toString());
                    if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                        config.setItfTransferfile(true);
                    }
                    config.setFunction(Config.FC_IMPORT);
                    config.setDatasetName(datasetName);
                    config.setDoImplicitSchemaImport(true);
                    config.setCreateFk(Config.CREATE_FK_YES);
                    config.setTidHandling(Config.TID_HANDLING_PROPERTY);
                    config.setImportTid(true);
                    config.setDefaultSrsCode("2056");
                    config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                    Ili2db.readSettingsFromDb(config);
                    Ili2db.run(config,null);
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            //settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,datasetName,null,null,true, targetPath,null,null,null,null,null,settings,localTestOut, null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_regionsRegEx() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        List<String> publishedRegions=new ArrayList<String>();
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    for(String datasetName:new String[] {"2501","2502"}) {
                        Config config=new Config();
                        new ch.ehi.ili2pg.PgMain().initConfig(config);
                        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                        config.setDburl(dbUrl);
                        config.setDbusr(dbUser);
                        config.setDbpwd(dbPassword);
                        config.setDbschema(dbSchema);
                        config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(datasetName+".itf").toString());
                        if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                            config.setItfTransferfile(true);
                        }
                        config.setFunction(Config.FC_IMPORT);
                        config.setDatasetName(datasetName);
                        config.setDoImplicitSchemaImport(true);
                        config.setCreateFk(Config.CREATE_FK_YES);
                        config.setDefaultSrsCode("2056");
                        config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                        Ili2db.readSettingsFromDb(config);
                        Ili2db.run(config,null);
                    }
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,null,null,null,false, targetPath,"[0-9][0-9][0-9][0-9]",null,publishedRegions,null,null,settings,localTestOut, null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertEquals(2,publishedRegions.size());
            for(String controlRegion:new String[] {"2501","2502"}) {
                Assert.assertTrue(publishedRegions.contains(controlRegion));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
            }
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_regionsList() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        List<String> publishedRegions=new ArrayList<String>();
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    for(String datasetName:new String[] {"2501","2502"}) {
                        Config config=new Config();
                        new ch.ehi.ili2pg.PgMain().initConfig(config);
                        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                        config.setDburl(dbUrl);
                        config.setDbusr(dbUser);
                        config.setDbpwd(dbPassword);
                        config.setDbschema(dbSchema);
                        config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(datasetName+".itf").toString());
                        if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                            config.setItfTransferfile(true);
                        }
                        config.setFunction(Config.FC_IMPORT);
                        config.setDatasetName(datasetName);
                        config.setDoImplicitSchemaImport(true);
                        config.setCreateFk(Config.CREATE_FK_YES);
                        config.setDefaultSrsCode("2056");
                        config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                        Ili2db.readSettingsFromDb(config);
                        Ili2db.run(config,null);
                    }
                }
            }
            List<String> regions=new ArrayList<String>();
            regions.add("2501");
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,null,null,null,false, targetPath,null,regions,publishedRegions,null,null,settings,localTestOut, null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertEquals(1,publishedRegions.size());
            for(String controlRegion:new String[] {"2501"}) {
                Assert.assertTrue(publishedRegions.contains(controlRegion));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
            }
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_regionsRegEx_UserFormats() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        List<String> publishedRegions=new ArrayList<String>();
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    for(String datasetName:new String[] {"SimpleCoord23a","SimpleCoord23b"}) {
                        Config config=new Config();
                        new ch.ehi.ili2pg.PgMain().initConfig(config);
                        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                        config.setDburl(dbUrl);
                        config.setDbusr(dbUser);
                        config.setDbpwd(dbPassword);
                        config.setDbschema(dbSchema);
                        config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(datasetName+".xtf").toString());
                        if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                            config.setItfTransferfile(true);
                        }
                        config.setFunction(Config.FC_IMPORT);
                        config.setDatasetName(datasetName);
                        config.setDoImplicitSchemaImport(true);
                        config.setCreateFk(Config.CREATE_FK_YES);
                        config.setDefaultSrsCode("2056");
                        config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                        Ili2db.readSettingsFromDb(config);
                        Ili2db.run(config,null);
                    }
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,null,null,null,true, targetPath,"SimpleCoord23[a-z]",null,publishedRegions,null,null,settings,localTestOut, null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertEquals(2,publishedRegions.size());
            for(String controlRegion:new String[] {"SimpleCoord23a","SimpleCoord23b"}) {
                Assert.assertTrue(publishedRegions.contains(controlRegion));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".xtf.zip")));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            }
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_SIMPLE_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_regionsRegEx_UserFormats_AV() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=localTestOut.resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        List<String> publishedRegions=new ArrayList<String>();
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+dbSchema+" CASCADE");
                    for(String datasetName:new String[] {"2501","2502"}) {
                        Config config=new Config();
                        new ch.ehi.ili2pg.PgMain().initConfig(config);
                        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                        config.setDburl(dbUrl);
                        config.setDbusr(dbUser);
                        config.setDbpwd(dbPassword);
                        config.setDbschema(dbSchema);
                        config.setXtffile(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA).resolve("files").resolve(datasetName+".itf").toString());
                        if(config.getXtffile()!=null && Ili2db.isItfFilename(config.getXtffile())){
                            config.setItfTransferfile(true);
                        }
                        config.setFunction(Config.FC_IMPORT);
                        config.setDatasetName(datasetName);
                        config.setDoImplicitSchemaImport(true);
                        config.setCreateFk(Config.CREATE_FK_YES);
                        config.setDefaultSrsCode("2056");
                        config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                        Ili2db.readSettingsFromDb(config);
                        Ili2db.run(config,null);
                    }
                }
            }
            Path targetPath = localTestOut.toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,dbSchema,null,null,null,true, targetPath,"[0-9][0-9][0-9][0-9]",null,publishedRegions,null,null,settings,localTestOut, null);
        }finally{
            if(jdbcConnection!=null) {
                jdbcConnection.close();
                jdbcConnection=null;
            }
            
        }
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertEquals(2,publishedRegions.size());
            for(String controlRegion:new String[] {"2501","2502"}) {
                Assert.assertTrue(publishedRegions.contains(controlRegion));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".itf.zip")));
                Assert.assertFalse(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));
                Assert.assertFalse(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".dxf.zip")));
            }
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
}
