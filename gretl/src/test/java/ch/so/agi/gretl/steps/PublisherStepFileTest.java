package ch.so.agi.gretl.steps;

import java.io.File;

import org.interlis2.validator.Validator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class PublisherStepFileTest {
    private String SRC_TEST_DATA = "src/test/resources/data/publisher";
    final String SRC_DATA_IDENT = "ch.so.agi.vermessung.edit";
    final String SRC_DATA_DATE_0="2021-12-02";
    final String SRC_DATA_FILENAME="av_test.itf";
    final String SRC_ILI_FILENAME="DM.01-AV-CH_LV95_24d_ili1.ili";
    final File sourcePath = new File(SRC_TEST_DATA,"files/"+SRC_DATA_FILENAME);
    final String ILI_DIRS=new File(SRC_TEST_DATA,"ili").getAbsolutePath();
    private GretlLogger log;
    public File testOut = new File("build/out");
    public PublisherStepFileTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    @Test
    public void file2local_allNew() throws Exception {
        final String SRC_DATA_DATE=SRC_DATA_DATE_0;
        File targetFolder=new File(testOut,SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(targetFolder.exists()) {
                PublisherStep.deleteFileTree(targetFolder.toPath());
            }
        }
        String targetPath = testOut.getAbsolutePath();
        File sourcePath = new File(SRC_TEST_DATA,"files/"+SRC_DATA_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,null,null,null,null,true,null,settings);
        // verify
        {
            Assert.assertTrue(targetFolder.exists());
            final File targetFolderAktuell = new File(targetFolder,PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(targetFolderAktuell.exists());
            Assert.assertFalse(new File(targetFolder,PublisherStep.PATH_ELE_HISTORY).exists());
            Assert.assertTrue(new File(targetFolderAktuell,SRC_DATA_FILENAME+".zip").exists());
            final File targetFolderAktuellMeta = new File(targetFolderAktuell,PublisherStep.PATH_ELE_META);
            Assert.assertTrue(targetFolderAktuellMeta.exists());
            Assert.assertTrue(new File(targetFolderAktuellMeta,SRC_ILI_FILENAME).exists());
            Assert.assertTrue(new File(targetFolderAktuellMeta,PublisherStep.PATH_ELE_PUBLISHDATE_JSON).exists());
            Assert.assertEquals(SRC_DATA_DATE, PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file2local_firstHistory() throws Exception {
        File targetFolder=new File(testOut,SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(targetFolder.exists()) {
                PublisherStep.deleteFileTree(targetFolder.toPath());
            }
        }
        file2local_allNew();
        final String SRC_DATA_DATE="2021-12-03";
        final String targetPath = testOut.getAbsolutePath();
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,null,null,null,null,true,null,settings);
        // verify
        {
            Assert.assertTrue(targetFolder.exists());
            final File targetFolderAktuell = new File(targetFolder,PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(targetFolderAktuell.exists());
            Assert.assertTrue(new File(targetFolder,PublisherStep.PATH_ELE_HISTORY).exists());
            Assert.assertTrue(new File(targetFolderAktuell,SRC_DATA_FILENAME+".zip").exists());
            final File targetFolderAktuellMeta = new File(targetFolderAktuell,PublisherStep.PATH_ELE_META);
            Assert.assertTrue(targetFolderAktuellMeta.exists());
            Assert.assertTrue(new File(targetFolderAktuellMeta,SRC_ILI_FILENAME).exists());
            Assert.assertTrue(new File(targetFolderAktuellMeta,PublisherStep.PATH_ELE_PUBLISHDATE_JSON).exists());
            Assert.assertEquals(SRC_DATA_DATE, PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            Assert.assertTrue(targetFolder.exists());
            final File targetFolderHistoryRoot = new File(targetFolder,PublisherStep.PATH_ELE_HISTORY);
            final File targetFolderHistory = new File(targetFolderHistoryRoot,SRC_DATA_DATE_0);
            Assert.assertTrue(targetFolderHistory.exists());
            Assert.assertTrue(new File(targetFolderHistory,SRC_DATA_FILENAME+".zip").exists());
            final File targetFolderAktuellMeta = new File(targetFolderHistory,PublisherStep.PATH_ELE_META);
            Assert.assertTrue(targetFolderAktuellMeta.exists());
            Assert.assertTrue(new File(targetFolderAktuellMeta,SRC_ILI_FILENAME).exists());
            Assert.assertTrue(new File(targetFolderAktuellMeta,PublisherStep.PATH_ELE_PUBLISHDATE_JSON).exists());
            Assert.assertEquals(SRC_DATA_DATE_0, PublisherStep.readPublishDate(targetFolderHistory));
        }
    }
    @Test
    public void file2local_overwriteAktuell() throws Exception {
        File targetFolder=new File(testOut,SRC_DATA_IDENT);
        final File targetFolderAktuell = new File(targetFolder,PublisherStep.PATH_ELE_AKTUELL);
        final File unexpectedTargetFile=new File(targetFolderAktuell,SRC_DATA_FILENAME+".gugus");
        // prepare
        {
            // delete output folder
            if(targetFolder.exists()) {
                PublisherStep.deleteFileTree(targetFolder.toPath());
            }
        }
        file2local_allNew();
        {
           // daten file loeschen
            new File(targetFolderAktuell,SRC_DATA_FILENAME+".zip").delete();
           // anderes file erzeugen
            unexpectedTargetFile.createNewFile();
        }
        final String SRC_DATA_DATE=SRC_DATA_DATE_0;
        final String targetPath = testOut.getAbsolutePath();
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,null,null,null,null,true,null,settings);
        // verify
        {
            Assert.assertTrue(targetFolder.exists());
            Assert.assertTrue(targetFolderAktuell.exists());
            Assert.assertFalse(new File(targetFolder,PublisherStep.PATH_ELE_HISTORY).exists());
            Assert.assertTrue(new File(targetFolderAktuell,SRC_DATA_FILENAME+".zip").exists());
            Assert.assertFalse(unexpectedTargetFile.exists());
            final File targetFolderAktuellMeta = new File(targetFolderAktuell,PublisherStep.PATH_ELE_META);
            Assert.assertTrue(targetFolderAktuellMeta.exists());
            Assert.assertTrue(new File(targetFolderAktuellMeta,SRC_ILI_FILENAME).exists());
            Assert.assertTrue(new File(targetFolderAktuellMeta,PublisherStep.PATH_ELE_PUBLISHDATE_JSON).exists());
            Assert.assertEquals(SRC_DATA_DATE, PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file2local_newExistingHistory_fail() throws Exception {
        File targetFolder=new File(testOut,SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(targetFolder.exists()) {
                PublisherStep.deleteFileTree(targetFolder.toPath());
            }
        }
        file2local_firstHistory();
        final String SRC_DATA_DATE=SRC_DATA_DATE_0;
        final String targetPath = testOut.getAbsolutePath();
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        try {
            step.publishFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,null,null,null,null,true,null,settings);
            Assert.fail();
        }catch(IllegalArgumentException ex) {
            Assert.assertEquals("neuer Stand (2021-12-02) existiert auch schon als History",ex.getMessage());
        }
    }
}
