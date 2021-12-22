package ch.so.agi.gretl.steps;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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

public abstract class AbstractPublisherStepTest {
    private String SRC_TEST_DATA = "src/test/resources/data/publisher";
    final String SRC_DATA_IDENT = "ch.so.agi.vermessung.edit";
    final String SRC_DATA_DATE_0="2021-12-02";
    final String SRC_DATA_FILENAME="av_test.itf";
    final String SRC_ILI_FILENAME="DM.01-AV-CH_LV95_24d_ili1.ili";
    final Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_FILENAME);
    final String ILI_DIRS=new File(SRC_TEST_DATA,"ili").getAbsolutePath();
    private GretlLogger log;
    public AbstractPublisherStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    @Test
    public void file2local_allNew() throws Exception {
        final String SRC_DATA_DATE=SRC_DATA_DATE_0;
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,null,null,null,null,true,null,settings);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_FILENAME+".zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(SRC_DATA_DATE, PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    protected abstract Path getTargetPath();
    
    @Test
    public void file2local_firstHistory() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        file2local_allNew();
        final String SRC_DATA_DATE="2021-12-03";
        final Path targetPath = getTargetPath().toAbsolutePath();
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,null,null,null,null,true,null,settings);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_FILENAME+".zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(SRC_DATA_DATE, PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            Assert.assertTrue(Files.exists(targetFolder));
            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(SRC_DATA_DATE_0);
            Assert.assertTrue(Files.exists(targetFolderHistory));
            Assert.assertTrue(Files.exists(targetFolderHistory.resolve(SRC_DATA_FILENAME+".zip")));
            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(SRC_DATA_DATE_0, PublisherStep.readPublishDate(targetFolderHistory));
        }
    }
    @Test
    public void file2local_overwriteAktuell() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        final Path unexpectedTargetFile=targetFolderAktuell.resolve(SRC_DATA_FILENAME+".gugus");
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        file2local_allNew();
        {
           // daten file loeschen
            Files.delete(targetFolderAktuell.resolve(SRC_DATA_FILENAME+".zip"));
           // anderes file erzeugen
            Files.createFile(unexpectedTargetFile);
        }
        final String SRC_DATA_DATE=SRC_DATA_DATE_0;
        final Path targetPath = getTargetPath().toAbsolutePath();
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourcePath,targetPath,null,null,null,null,true,null,settings);
        // verify
        {
            Assert.assertTrue(Files.exists(targetFolder));
            Assert.assertTrue(Files.exists(targetFolderAktuell));
            Assert.assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            Assert.assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_FILENAME+".zip")));
            Assert.assertFalse(Files.exists(unexpectedTargetFile));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_FILENAME)));
            Assert.assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            Assert.assertEquals(SRC_DATA_DATE, PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file2local_newExistingHistory_fail() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if(Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        file2local_firstHistory();
        final String SRC_DATA_DATE=SRC_DATA_DATE_0;
        final Path targetPath = getTargetPath().toAbsolutePath();
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
