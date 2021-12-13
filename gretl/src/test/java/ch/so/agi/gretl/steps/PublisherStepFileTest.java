package ch.so.agi.gretl.steps;

import java.io.File;

import org.interlis2.validator.Validator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class PublisherStepFileTest {
    private String SRC_TEST_DATA = "src/test/resources/data/publisher";
    private GretlLogger log;
    public File testOut = new File("build/out");
    public PublisherStepFileTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    @Test
    public void file2local() throws Exception {
        String targetPath = testOut.getAbsolutePath();
        String dataIdent = "ch.so.agi.vermessung.edit";
        File sourcePath = new File(SRC_TEST_DATA,"files/av_test.itf");
        PublisherStep step=new PublisherStep();
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, new File(SRC_TEST_DATA,"ili").getAbsolutePath());
        settings.setValue(Validator.SETTING_CONFIGFILE, null);
        step.publishFromFile("2021-12-02",dataIdent,sourcePath,targetPath,null,null,null,null,true,null,settings);
    }
}
