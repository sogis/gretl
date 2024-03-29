package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.interlis2.validator.Validator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.robtimus.filesystems.sftp.SFTPEnvironment;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.SimiSvcClientMock;

public abstract class AbstractPublisherStepTest {
    final public static String SRC_TEST_DATA = "src/test/resources/data/publisher";
    final public static String SRC_TEST_DATA_FILES = SRC_TEST_DATA+"/files";
    final public static String SRC_TEST_DATA_ILI = SRC_TEST_DATA+"/ili";
    final protected static String SRC_DATA_IDENT = "ch.so.agi.vermessung.edit";
    final protected static Date SRC_DATA_DATE_0=new GregorianCalendar(2021,11,02).getTime();
    final protected static Date SRC_DATA_DATE_1=new GregorianCalendar(2021,11,03).getTime();
    final protected static Date SRC_DATA_DATE_2=new GregorianCalendar(2021,11,04).getTime();
    final protected static String SRC_DATA_SIMPLE_FILENAME="SimpleCoord23a.xtf";
    final protected static String SRC_DATA_AV_FILENAME="av_test.itf";
    final protected static String SRC_GROOM_FILENAME="groomTest.json";
    final protected static String SRC_ILI_SIMPLE_FILENAME="SimpleCoord23.ili";
    final protected static String SRC_ILI_AV_FILENAME="DM.01-AV-CH_LV95_24d_ili1.ili";
    final protected static String ILI_DIRS=new File(SRC_TEST_DATA_ILI).getAbsolutePath();
    final protected Path localTestOut = Paths.get("build").resolve("out");

    protected GretlLogger log;
    public AbstractPublisherStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
//        java.util.logging.Logger logger = java.util.logging.Logger.getGlobal().getParent();
//        logger.setLevel(java.util.logging.Level.ALL);
//        java.util.logging.Handler handler = new java.util.logging.ConsoleHandler();
//        handler.setLevel(java.util.logging.Level.ALL);
//        logger.addHandler(handler);        
    }
    protected abstract Path getTargetPath();
    @Test
    public void file_allNew() throws Exception {
        final Date SRC_DATA_DATE=SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        SimiSvcClientMock simiSvc=new SimiSvcClientMock();
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,false,targetPath,null,null,null,null,null,settings,localTestOut, simiSvc);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_LEAFLET_HTML)));
            Assert.assertEquals(SRC_DATA_IDENT, simiSvc.getNotifiedDataIdent());
            Assert.assertEquals(SRC_DATA_DATE, simiSvc.getNotifiedPublishDate());
            Assert.assertNull(simiSvc.getNotifiedRegions());
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file_allNew_userFormat() throws Exception {
        final Date SRC_DATA_DATE=SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        SimiSvcClientMock simiSvc=new SimiSvcClientMock();
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,true,targetPath,null,null,null,null,null,settings,localTestOut, simiSvc);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".dxf.zip")));
            Assert.assertFalse(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".shp.zip")));
            Assert.assertFalse(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".gpkg.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_LEAFLET_HTML)));
            Assert.assertEquals(SRC_DATA_IDENT, simiSvc.getNotifiedDataIdent());
            Assert.assertEquals(SRC_DATA_DATE, simiSvc.getNotifiedPublishDate());
            Assert.assertNull(simiSvc.getNotifiedRegions());
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file_allNew_groom() throws Exception {
        final Date SRC_DATA_DATE=SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        final Path groomFile = Paths.get(SRC_TEST_DATA).resolve(SRC_GROOM_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        SimiSvcClientMock simiSvc=new SimiSvcClientMock();
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,false,targetPath,null,null,null,null,groomFile,settings,localTestOut, simiSvc);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_LEAFLET_HTML)));
            Assert.assertEquals(SRC_DATA_IDENT, simiSvc.getNotifiedDataIdent());
            Assert.assertEquals(SRC_DATA_DATE, simiSvc.getNotifiedPublishDate());
            Assert.assertNull(simiSvc.getNotifiedRegions());
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file_allNew_noGroomFile_fail() throws Exception {
        final Date SRC_DATA_DATE=SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        final Path groomFile = Paths.get(SRC_TEST_DATA).resolve(SRC_GROOM_FILENAME+"-missing");
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        SimiSvcClientMock simiSvc=new SimiSvcClientMock();
        try {
            step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,false,targetPath,null,null,null,null,groomFile,settings,localTestOut, simiSvc);
            Assert.fail();
        }catch(IOException ex){
            ; // ok, expected
            log.error("file_allNew_noGroomFile_fail", ex);
        }
    }
    @Test
    public void file_regionsRegEx() throws Exception {
        final Date SRC_DATA_DATE=SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        List<String> publishedRegions=new ArrayList<String>();
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,false,targetPath,"[0-9][0-9][0-9][0-9]",null,publishedRegions,null,null,settings,localTestOut, null);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertEquals(2,publishedRegions.size());
            for(String controlRegion:new String[] {"2501","2502"}) {
                Assert.assertTrue(publishedRegions.contains(controlRegion));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+SRC_DATA_IDENT+".itf.zip")));
            }
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file_regionsList() throws Exception {
        final Date SRC_DATA_DATE=SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        List<String> regions=new ArrayList<String>();
        regions.add("2501");
        List<String> publishedRegions=new ArrayList<String>();
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,false,targetPath,null,regions,publishedRegions,null,null,settings,localTestOut, null);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertEquals(1,publishedRegions.size());
            for(String controlRegion:new String[] {"2501"}) {
                Assert.assertTrue(publishedRegions.contains(controlRegion));
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+SRC_DATA_IDENT+".itf.zip")));
            }
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file_regionsUpdate() throws Exception {
        final Date SRC_DATA_DATE_INITIAL=SRC_DATA_DATE_0;
        final Date SRC_DATA_DATE_UPDATE=SRC_DATA_DATE_1;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        // first publication
        {
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            List<String> regions=new ArrayList<String>();
            regions.add("2501");
            List<String> publishedRegions=new ArrayList<String>();
            step.publishDatasetFromFile(SRC_DATA_DATE_INITIAL,SRC_DATA_IDENT,sourcePath,false,targetPath,null,regions,publishedRegions,null,null,settings,localTestOut, null);
            Assert.assertEquals(1,publishedRegions.size());
        }
        // incremental second publication
        List<String> publishedRegions=new ArrayList<String>();
        {
            PublisherStep step=new PublisherStep();
            Settings settings=new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            List<String> regions=new ArrayList<String>();
            regions.add("2502");
            step.publishDatasetFromFile(SRC_DATA_DATE_UPDATE,SRC_DATA_IDENT,sourcePath,false,targetPath,null,regions,publishedRegions,null,null,settings,localTestOut, null);
        }
        
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertEquals(1,publishedRegions.size()); // nur die neu publizierte Region! (Nicht: alle nun publizierten Regionen)
            Assert.assertEquals("2502",publishedRegions.get(0));
            for(String controlRegion:new String[] {"2501","2502"}) {
                Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+SRC_DATA_IDENT+".itf.zip")));
            }
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_1), PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_0));
            Assert.assertTrue(Files.exists(targetFolderHistory));
            Assert.assertTrue(Files.exists(targetFolderHistory.resolve("2501."+SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderHistory));
        }
    }
    
    @Test
    public void file_firstHistory() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        file_allNew();
        final Date SRC_DATA_DATE=SRC_DATA_DATE_1;
        final Path targetPath = getTargetPath().toAbsolutePath();
        final Path sourceFile = Paths.get(SRC_TEST_DATA_FILES).resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourceFile,false,targetPath,null,null,null,null,null,settings,localTestOut, null);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_0));
            Assert.assertTrue(Files.exists(targetFolderHistory));
            Assert.assertTrue(Files.exists(targetFolderHistory.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderHistory));
        }
    }
    @Test
    public void file_groomHistory() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        file_firstHistory();
        final Date SRC_DATA_DATE=SRC_DATA_DATE_2;
        final Path targetPath = getTargetPath().toAbsolutePath();
        final Path sourceFile = Paths.get(SRC_TEST_DATA_FILES).resolve(SRC_DATA_AV_FILENAME);
        final Path groomFile = Paths.get(SRC_TEST_DATA).resolve(SRC_GROOM_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourceFile,false,targetPath,null,null,null,null,groomFile,settings,localTestOut, null);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory1 = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_1));
            Assert.assertFalse(Files.exists(targetFolderHistory1));
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_0));
            Assert.assertTrue(Files.exists(targetFolderHistory));
            Assert.assertTrue(Files.exists(targetFolderHistory.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderHistory));
        }
    }
    @Test
    public void file_overwriteAktuell() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        final Path unexpectedTargetFile=targetFolderAktuell.resolve(SRC_DATA_AV_FILENAME+".gugus");
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        file_allNew();
        {
           // daten file loeschen
            Files.delete(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip"));
           // anderes file erzeugen
            Files.createFile(unexpectedTargetFile);
        }
        final Date SRC_DATA_DATE=SRC_DATA_DATE_0;
        final Path targetPath = getTargetPath().toAbsolutePath();
        final Path sourceFile = Paths.get(SRC_TEST_DATA_FILES).resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourceFile,false,targetPath,null,null,null,null,null,settings,localTestOut, null);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            Assert.assertFalse(Files.exists(unexpectedTargetFile));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file_newExistingHistory_fail() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        file_firstHistory();
        final Date SRC_DATA_DATE=SRC_DATA_DATE_0;
        final Path targetPath = getTargetPath().toAbsolutePath();
        final Path sourceFile = Paths.get(SRC_TEST_DATA_FILES).resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        try {
            step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourceFile,false,targetPath,null,null,null,null,null,settings,localTestOut, null);
            Assert.fail();
        }catch(IllegalArgumentException ex) {
            Assert.assertEquals("neuer Stand (2021-12-02) existiert auch schon als History",ex.getMessage());
        }
    }
}
