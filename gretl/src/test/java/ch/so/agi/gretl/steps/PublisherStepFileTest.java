package ch.so.agi.gretl.steps;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.interlis2.validator.Validator;
import org.junit.Assert;
import org.junit.Test;

import ch.ehi.basics.settings.Settings;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;

public class PublisherStepFileTest extends AbstractPublisherStepTest {
    public Path localTestOut = Paths.get("build").resolve("out");
    public PublisherStepFileTest() {
        super();
    }
    @Override
    protected Path getTargetPath() {
        return localTestOut;
    }
    String dburl=System.getProperty("dburl"); 
    String dbuser=System.getProperty("dbusr");
    String dbpwd=System.getProperty("dbpwd"); 
    final protected String DB_SCHEMA="publisher";
    @Test
    public void db_allNew() throws Exception {
        final String SRC_DATA_DATE=SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
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
                    config.setModeldir(Ili2db.ILI_FROM_DB+ch.interlis.ili2c.Main.ILIDIR_SEPARATOR+ILI_DIRS);
                    config.setDburl(dburl);
                    config.setDbusr(dbuser);
                    config.setDbpwd(dbpwd);
                    config.setDbschema(DB_SCHEMA);
                    config.setXtffile(Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_FILENAME).toString());
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
            settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            step.publishDatasetFromDb(SRC_DATA_DATE,SRC_DATA_IDENT,jdbcConnection,DB_SCHEMA,targetPath,datasetName,null,settings,localTestOut);
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
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(datasetName+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(SRC_DATA_DATE, PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
}
