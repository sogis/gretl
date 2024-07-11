package ch.so.agi.gretl.steps;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.SimiSvcClientMock;
import org.interlis2.validator.Validator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractPublisherStepTest {
    public static final String SRC_TEST_DATA = "src/test/resources/data/publisher";
    public static final String SRC_TEST_DATA_FILES = SRC_TEST_DATA + "/files";
    public static final String SRC_TEST_DATA_ILI = SRC_TEST_DATA + "/ili";
    protected final static String SRC_DATA_IDENT = "ch.so.agi.vermessung.edit";
    protected final static Date SRC_DATA_DATE_0 = new GregorianCalendar(2021, Calendar.DECEMBER, 2).getTime();
    protected final static Date SRC_DATA_DATE_1 = new GregorianCalendar(2021, Calendar.DECEMBER, 3).getTime();
    protected final static Date SRC_DATA_DATE_2 = new GregorianCalendar(2021, Calendar.DECEMBER, 4).getTime();
    protected final static String SRC_DATA_SIMPLE_FILENAME = "SimpleCoord23a.xtf";
    protected final static String SRC_DATA_AV_FILENAME = "av_test.itf";
    protected final static String SRC_GROOM_FILENAME = "groomTest.json";
    protected final static String SRC_ILI_SIMPLE_FILENAME = "SimpleCoord23.ili";
    protected final static String SRC_ILI_AV_FILENAME = "DM.01-AV-CH_LV95_24d_ili1.ili";
    protected final static String ILI_DIRS = new File(SRC_TEST_DATA_ILI).getAbsolutePath();
    protected final Path localTestOut = Paths.get("build").resolve("out");
    protected GretlLogger log;

    public AbstractPublisherStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    protected abstract Path getTargetPath();

    @Test
    public void file_allNew() throws Exception {
        Path targetFolder = getTargetPath().resolve(SRC_DATA_IDENT);

        // prepare
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }

        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step = new PublisherStep();
        Settings settings = new Settings();
        SimiSvcClientMock simiSvc = new SimiSvcClientMock();

        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishDatasetFromFile(SRC_DATA_DATE_0,SRC_DATA_IDENT,sourcePath,false,targetPath,null,null,null,null,null,settings,localTestOut, simiSvc);

        // verify
        assertTrue(Files.exists(targetFolder));

        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        assertTrue(Files.exists(targetFolderAktuell));
        assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
        assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));

        final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
        assertTrue(Files.exists(targetFolderAktuellMeta));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_LEAFLET_HTML)));
        assertEquals(SRC_DATA_IDENT, simiSvc.getNotifiedDataIdent());
        assertEquals(SRC_DATA_DATE_0, simiSvc.getNotifiedPublishDate());
        assertNull(simiSvc.getNotifiedRegions());
        assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
    }

    @Test
    public void file_allNew_userFormat() throws Exception {
        Path targetFolder = getTargetPath().resolve(SRC_DATA_IDENT);

        // prepare
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }

        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step = new PublisherStep();
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        SimiSvcClientMock simiSvc = new SimiSvcClientMock();
        step.publishDatasetFromFile(SRC_DATA_DATE_0,SRC_DATA_IDENT,sourcePath,true,targetPath,null,null,null,null,null,settings,localTestOut, simiSvc);

        // verify
        assertTrue(Files.exists(targetFolder));

        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        assertTrue(Files.exists(targetFolderAktuell));
        assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
        assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
        assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".dxf.zip")));
        assertFalse(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".shp.zip")));
        assertFalse(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".gpkg.zip")));

        final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
        assertTrue(Files.exists(targetFolderAktuellMeta));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_LEAFLET_HTML)));
        assertEquals(SRC_DATA_IDENT, simiSvc.getNotifiedDataIdent());
        assertEquals(SRC_DATA_DATE_0, simiSvc.getNotifiedPublishDate());
        assertNull(simiSvc.getNotifiedRegions());
        assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
    }
    @Test
    public void file_allNew_groom() throws Exception {
        Path targetFolder = getTargetPath().resolve(SRC_DATA_IDENT);

        // prepare
        // delete output folder
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }

        Path targetPath = getTargetPath().toAbsolutePath();
        Path srcTestDataPath = Paths.get(SRC_TEST_DATA);
        Path sourcePath = srcTestDataPath.resolve("files").resolve(SRC_DATA_AV_FILENAME);
        Path groomFile = srcTestDataPath.resolve(SRC_GROOM_FILENAME);

        SimiSvcClientMock simiSvc = new SimiSvcClientMock();
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);

        PublisherStep step = new PublisherStep();
        step.publishDatasetFromFile(SRC_DATA_DATE_0,SRC_DATA_IDENT,sourcePath,false,targetPath,null,null,null,null,groomFile,settings,localTestOut, simiSvc);

        // verify
        assertTrue(Files.exists(targetFolder));

        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        assertTrue(Files.exists(targetFolderAktuell));
        assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
        assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT + ".itf.zip")));

        final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
        assertTrue(Files.exists(targetFolderAktuellMeta));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_LEAFLET_HTML)));
        assertEquals(SRC_DATA_IDENT, simiSvc.getNotifiedDataIdent());
        assertEquals(SRC_DATA_DATE_0, simiSvc.getNotifiedPublishDate());
        assertNull(simiSvc.getNotifiedRegions());
        assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
    }
    @Test
    public void file_allNew_noGroomFile_fail() throws Exception {
        Path targetFolder = getTargetPath().resolve(SRC_DATA_IDENT);

        // prepare
        // delete output folder
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }

        Path targetPath = getTargetPath().toAbsolutePath();
        Path srcTestData = Paths.get(SRC_TEST_DATA);
        Path sourcePath = srcTestData.resolve("files").resolve(SRC_DATA_AV_FILENAME);
        final Path groomFile = srcTestData.resolve(SRC_GROOM_FILENAME + "-missing");

        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        SimiSvcClientMock simiSvc=new SimiSvcClientMock();

        try {
            PublisherStep step = new PublisherStep();
            step.publishDatasetFromFile(SRC_DATA_DATE_0,SRC_DATA_IDENT,sourcePath,false,targetPath,null,null,null,null,groomFile,settings,localTestOut, simiSvc);
            fail();
        } catch (IOException ex) {
            // ok, expected
            log.error("file_allNew_noGroomFile_fail", ex);
        }
    }
    @Test
    public void file_regionsRegEx() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);

        // prepare
        // delete output folder
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }

        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);

        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        List<String> publishedRegions= new ArrayList<>();

        PublisherStep step = new PublisherStep();
        step.publishDatasetFromFile(SRC_DATA_DATE_0,SRC_DATA_IDENT,sourcePath,false,targetPath,"[0-9][0-9][0-9][0-9]",null,publishedRegions,null,null,settings,localTestOut, null);

        // verify
        assertTrue(Files.exists(targetFolder));

        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        assertTrue(Files.exists(targetFolderAktuell));
        assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
        assertEquals(2,publishedRegions.size());
        for (String controlRegion : new String[] {"2501","2502"}) {
            assertTrue(publishedRegions.contains(controlRegion));
            assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+SRC_DATA_IDENT+".itf.zip")));
        }

        final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
        assertTrue(Files.exists(targetFolderAktuellMeta));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
        assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
    }
    @Test
    public void file_regionsList() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        // delete output folder
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }

        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);

        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        List<String> regions = Collections.singletonList("2501");
        List<String> publishedRegions = new ArrayList<>();

        PublisherStep step = new PublisherStep();
        step.publishDatasetFromFile(SRC_DATA_DATE_0,SRC_DATA_IDENT,sourcePath,false,targetPath,null,regions,publishedRegions,null,null,settings,localTestOut, null);

        // verify
        assertTrue(Files.exists(targetFolder));
        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        assertTrue(Files.exists(targetFolderAktuell));
        assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
        assertEquals(1,publishedRegions.size());
        for(String controlRegion:new String[] {"2501"}) {
            assertTrue(publishedRegions.contains(controlRegion));
            assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+SRC_DATA_IDENT+".itf.zip")));
        }
        final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
        assertTrue(Files.exists(targetFolderAktuellMeta));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
        assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
        assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderAktuell));
    }
    @Test
    public void file_regionsUpdate() throws Exception {
        Path targetFolder = getTargetPath().resolve(SRC_DATA_IDENT);

        // prepare
        // delete output folder
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }

        Path targetPath = getTargetPath().toAbsolutePath();
        Path sourcePath = Paths.get(SRC_TEST_DATA).resolve("files").resolve(SRC_DATA_AV_FILENAME);
        // first publication
        {
            PublisherStep step = new PublisherStep();
            Settings settings = new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            List<String> regions = new ArrayList<String>();
            regions.add("2501");
            List<String> publishedRegions = new ArrayList<>();
            step.publishDatasetFromFile(SRC_DATA_DATE_1,SRC_DATA_IDENT,sourcePath,false,targetPath,null,regions,publishedRegions,null,null,settings,localTestOut, null);
            assertEquals(1,publishedRegions.size());
        }

        // incremental second publication
        List<String> publishedRegions = new ArrayList<>();
        {
            PublisherStep step = new PublisherStep();
            Settings settings = new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
            settings.setValue(Validator.SETTING_CONFIGFILE, null);
            List<String> regions = new ArrayList<>();
            regions.add("2502");
            step.publishDatasetFromFile(SRC_DATA_DATE_1,SRC_DATA_IDENT,sourcePath,false,targetPath,null,regions,publishedRegions,null,null,settings,localTestOut, null);
        }
        
        // verify
        {
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertEquals(1,publishedRegions.size()); // nur die neu publizierte Region! (Nicht: alle nun publizierten Regionen)
            assertEquals("2502",publishedRegions.get(0));

            for (String controlRegion:new String[] {"2501","2502"}) {
                assertTrue(Files.exists(targetFolderAktuell.resolve(controlRegion+"."+SRC_DATA_IDENT+".itf.zip")));
            }

            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_1), PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_0));
            assertTrue(Files.exists(targetFolderHistory));
            assertTrue(Files.exists(targetFolderHistory.resolve("2501."+SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderHistory));
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
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_0));
            assertTrue(Files.exists(targetFolderHistory));
            assertTrue(Files.exists(targetFolderHistory.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderHistory));
        }
    }
    @Test
    public void file_groomHistory() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        // delete output folder
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
        }

        file_firstHistory();
        final Date SRC_DATA_DATE = SRC_DATA_DATE_2;
        final Path targetPath = getTargetPath().toAbsolutePath();
        final Path sourceFile = Paths.get(SRC_TEST_DATA_FILES).resolve(SRC_DATA_AV_FILENAME);
        final Path groomFile = Paths.get(SRC_TEST_DATA).resolve(SRC_GROOM_FILENAME);
        PublisherStep step = new PublisherStep();
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishDatasetFromFile(SRC_DATA_DATE,SRC_DATA_IDENT,sourceFile,false,targetPath,null,null,null,null,groomFile,settings,localTestOut, null);

        // verify
        {
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
            assertTrue(Files.exists(targetFolderAktuell));
            assertTrue(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
        // verify history
        {
            assertTrue(Files.exists(targetFolder));
            final Path targetFolderHistoryRoot = targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY);
            final Path targetFolderHistory1 = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_1));
            assertFalse(Files.exists(targetFolderHistory1));
            final Path targetFolderHistory = targetFolderHistoryRoot.resolve(PublisherStep.getDateTag(SRC_DATA_DATE_0));
            assertTrue(Files.exists(targetFolderHistory));
            assertTrue(Files.exists(targetFolderHistory.resolve(SRC_DATA_IDENT+".itf.zip")));
            final Path targetFolderAktuellMeta = targetFolderHistory.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE_0), PublisherStep.readPublishDate(targetFolderHistory));
        }
    }
    @Test
    public void file_overwriteAktuell() throws Exception {
        Path targetFolder=getTargetPath().resolve(SRC_DATA_IDENT);
        final Path targetFolderAktuell = targetFolder.resolve(PublisherStep.PATH_ELE_AKTUELL);
        final Path unexpectedTargetFile=targetFolderAktuell.resolve(SRC_DATA_AV_FILENAME+".gugus");
        // prepare
        // delete output folder
        if (Files.exists(targetFolder)) {
            PublisherStep.deleteFileTree(targetFolder);
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
            assertTrue(Files.exists(targetFolder));
            assertTrue(Files.exists(targetFolderAktuell));
            assertFalse(Files.exists(targetFolder.resolve(PublisherStep.PATH_ELE_HISTORY)));
            assertTrue(Files.exists(targetFolderAktuell.resolve(SRC_DATA_IDENT+".itf.zip")));
            assertFalse(Files.exists(unexpectedTargetFile));
            final Path targetFolderAktuellMeta = targetFolderAktuell.resolve(PublisherStep.PATH_ELE_META);
            assertTrue(Files.exists(targetFolderAktuellMeta));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(SRC_ILI_AV_FILENAME)));
            assertTrue(Files.exists(targetFolderAktuellMeta.resolve(PublisherStep.PATH_ELE_PUBLISHDATE_JSON)));
            assertEquals(PublisherStep.getDateTag(SRC_DATA_DATE), PublisherStep.readPublishDate(targetFolderAktuell));
        }
    }
    @Test
    public void file_newExistingHistory_fail() throws Exception {
        Path targetFolder = getTargetPath().resolve(SRC_DATA_IDENT);
        // prepare
        {
            // delete output folder
            if (Files.exists(targetFolder)) {
                PublisherStep.deleteFileTree(targetFolder);
            }
        }
        file_firstHistory();
        final Path targetPath = getTargetPath().toAbsolutePath();
        final Path sourceFile = Paths.get(SRC_TEST_DATA_FILES).resolve(SRC_DATA_AV_FILENAME);
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, ILI_DIRS);
        settings.setValue(Validator.SETTING_CONFIGFILE, null);

        try {
            step.publishDatasetFromFile(SRC_DATA_DATE_0,SRC_DATA_IDENT,sourceFile,false,targetPath,null,null,null,null,null,settings,localTestOut, null);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals("neuer Stand (2021-12-02) existiert auch schon als History",ex.getMessage());
        }
    }
}
