package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import net.sf.saxon.s9api.SaxonApiException;

public class MetaPublisherStepTestFile2LocalTest {
    
    protected GretlLogger log;

    public MetaPublisherStepTestFile2LocalTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    @Test
    public void create_meta_Ok() {
        MetaPublisherStep metaPublisherStep = new MetaPublisherStep("create_meta_Ok");
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/thema-config/ch.so.awjf.seltene_baumarten"), "ch.so.awjf.seltene_baumarten");
        metaPublisherStep.execute(new File("src/test/resources/data/metapublisher/thema-config/ch.so.afu.abbaustellen"), "ch.so.afu.abbaustellen");
    }
}