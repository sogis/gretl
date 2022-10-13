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
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    static final public String DM01AVCH24LV95D="DM01AVCH24LV95D";
    
    @ClassRule
    public static PostgreSQLContainer postgres = System.getProperty("dburl")==null?
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(TestUtil.PG_DDLUSR_USR)
        .withPassword(TestUtil.PG_DDLUSR_PWD)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2)):null;
    
    final protected Path localTestOut = Paths.get("build").resolve("out");
    public PublisherStepDb2LocalTest() {
        super();
    }
    protected Path getTargetPath() {
        return localTestOut;
    }
    String dburl=System.getProperty("dburl",postgres!=null?postgres.getJdbcUrl():null); 
    String dbuser=System.getProperty("dbusr",TestUtil.PG_DDLUSR_USR);
    String dbpwd=System.getProperty("dbpwd",TestUtil.PG_DDLUSR_PWD); 
    final protected String DB_SCHEMA="publisher";
    @Category(DbTest.class)
    @Test
    public void db_allNew() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        String datasetName="av";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dburl);
                    config.setDbusr(dbuser);
                    config.setDbpwd(dbpwd);
                    config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,datasetName,null,null,false, targetPath,null,null,null,null,null,settings,localTestOut,null);
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
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dburl);
                    config.setDbusr(dbuser);
                    config.setDbpwd(dbpwd);
                    config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,null,DM01AVCH24LV95D,null,false, targetPath,null,null,null,null,null,settings,localTestOut,null);
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
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dburl);
                    config.setDbusr(dbuser);
                    config.setDbpwd(dbpwd);
                    config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            try {
                step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,null,DM01AVCH24LV95D,null,false, targetPath,null,null,null,null,null,settings,localTestOut,null);
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
    public void db_UserFormats() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        final String datasetName="simple";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dburl);
                    config.setDbusr(dbuser);
                    config.setDbpwd(dbpwd);
                    config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,datasetName,null,null,true, targetPath,null,null,null,null,null,settings,localTestOut, null);
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
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(AbstractPublisherStepTest.SRC_ILI_SIMPLE_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Category(DbTest.class)
    @Test
    public void db_UserFormats_ModelDir_AV() throws Exception {
        final Date SRC_DATA_DATE=AbstractPublisherStepTest.SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        String datasetName="av";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dburl);
                    config.setDbusr(dbuser);
                    config.setDbpwd(dbpwd);
                    config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,datasetName,null,null,true, targetPath,null,null,null,null,null,settings,localTestOut, null);
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
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        String datasetName="av";
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    Config config=new Config();
                    new ch.ehi.ili2pg.PgMain().initConfig(config);
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                    config.setDburl(dburl);
                    config.setDbusr(dbuser);
                    config.setDbpwd(dbpwd);
                    config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            //settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,datasetName,null,null,true, targetPath,null,null,null,null,null,settings,localTestOut, null);
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
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        List<String> publishedRegions=new ArrayList<String>();
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    for(String datasetName:new String[] {"2501","2502"}) {
                        Config config=new Config();
                        new ch.ehi.ili2pg.PgMain().initConfig(config);
                        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                        config.setDburl(dburl);
                        config.setDbusr(dbuser);
                        config.setDbpwd(dbpwd);
                        config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,null,null,null,false, targetPath,"[0-9][0-9][0-9][0-9]",null,publishedRegions,null,null,settings,localTestOut, null);
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
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        List<String> publishedRegions=new ArrayList<String>();
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    for(String datasetName:new String[] {"2501","2502"}) {
                        Config config=new Config();
                        new ch.ehi.ili2pg.PgMain().initConfig(config);
                        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                        config.setDburl(dburl);
                        config.setDbusr(dbuser);
                        config.setDbpwd(dbpwd);
                        config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,null,null,null,false, targetPath,null,regions,publishedRegions,null,null,settings,localTestOut, null);
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
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        List<String> publishedRegions=new ArrayList<String>();
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    for(String datasetName:new String[] {"SimpleCoord23a","SimpleCoord23b"}) {
                        Config config=new Config();
                        new ch.ehi.ili2pg.PgMain().initConfig(config);
                        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                        config.setDburl(dburl);
                        config.setDbusr(dbuser);
                        config.setDbpwd(dbpwd);
                        config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,null,null,null,true, targetPath,"SimpleCoord23[a-z]",null,publishedRegions,null,null,settings,localTestOut, null);
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
        Path targetFolder=getTargetPath().resolve(AbstractPublisherStepTest.SRC_DATA_IDENT);
        Connection jdbcConnection=null;
        List<String> publishedRegions=new ArrayList<String>();
        try{
            // prepare
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            {
                // delete output folder
                if(Files.exists(targetFolder)) {
                    PublisherStep.deleteFileTree(targetFolder);
                }
                // import data into db
                {
                    Statement stmt=jdbcConnection.createStatement();
                    stmt.execute("DROP SCHEMA IF EXISTS "+DB_SCHEMA+" CASCADE");
                    for(String datasetName:new String[] {"2501","2502"}) {
                        Config config=new Config();
                        new ch.ehi.ili2pg.PgMain().initConfig(config);
                        config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+AbstractPublisherStepTest.ILI_DIRS);
                        config.setDburl(dburl);
                        config.setDbusr(dbuser);
                        config.setDbpwd(dbpwd);
                        config.setDbschema(DB_SCHEMA);
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
            Path targetPath = getTargetPath().toAbsolutePath();
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, AbstractPublisherStepTest.ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,AbstractPublisherStepTest.SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,null,null,null,true, targetPath,"[0-9][0-9][0-9][0-9]",null,publishedRegions,null,null,settings,localTestOut, null);
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
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".gpkg.zip")));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+AbstractPublisherStepTest.SRC_DATA_IDENT+".shp.zip")));
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
