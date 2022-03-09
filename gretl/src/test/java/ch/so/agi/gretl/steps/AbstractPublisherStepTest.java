package ch.so.agi.gretl.steps;

import java.io.File;
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
    final protected static String SRC_DATA_SIMPLE_FILENAME="SimpleCoord23a.xtf";
    final protected static String SRC_DATA_AV_FILENAME="av_test.itf";
    final protected static String SRC_ILI_SIMPLE_FILENAME="SimpleCoord23.ili";
    final protected static String SRC_ILI_AV_FILENAME="DM.01-AV-CH_LV95_24d_ili1.ili";
    final protected static String ILI_DIRS=new File(SRC_TEST_DATA_ILI).getAbsolutePath();
    final protected Path localTestOut = Paths.get("build").resolve("out");

    protected GretlLogger log;
    public AbstractPublisherStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
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
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,null,null,null,null,settings,localTestOut, simiSvc);
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
    public void file_regions() throws Exception {
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
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,"[0-9][0-9][0-9][0-9]",publishedRegions,null,null,settings,localTestOut, null);
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
        final Date SRC_DATA_DATE=new GregorianCalendar(2021,11,03).getTime();
        final Path targetPath = getTargetPath().toAbsolutePath();
        final Path sourceFile = Paths.get(SRC_TEST_DATA_FILES).resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourceFile,targetPath,null,null,null,null,settings,localTestOut, null);
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
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourceFile,targetPath,null,null,null,null,settings,localTestOut, null);
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
            step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourceFile,targetPath,null,null,null,null,settings,localTestOut, null);
            Assert.fail();
        }catch(IllegalArgumentException ex) {
            Assert.assertEquals("neuer Stand (2021-12-02) existiert auch schon als History",ex.getMessage());
        }
    }
}
