package ch.so.agi.gretl.util.publisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.AbstractPublisherStepOldTest;
import ch.so.agi.gretl.steps.PublisherStepOld;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PublicationLogTest {
    final public static String SRC_TEST_DATA = AbstractPublisherStepOldTest.SRC_TEST_DATA;
    final protected Path localTestOut = Paths.get("build").resolve("out");

    protected GretlLogger log;
    public PublicationLogTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
        try {
            Files.createDirectories(localTestOut);
        } catch (IOException e) {
            log.error("failed to creat test output directory", e);
        }
    }
    @Test
    public void readMinimalFile() throws Exception {
        PublicationLog expected=new PublicationLog("ch.so.afu.gewaesserschutz", PublisherStepOld.parsePublicationTimestamp("2021-12-23T14:54:49.050062"));
        final Path file = localTestOut.resolve("minimalPublication.json");
        PublisherStepOld.writePublication(file,expected);
        PublicationLog actual= PublisherStepOld.readPublication(file);
        assertEquals(expected.getDataIdent(), actual.getDataIdent());
        assertEquals(expected.getPublished(), actual.getPublished());
        assertNull(actual.getPublishedBaskets());
        assertNull(actual.getPublishedRegions());
    }
    @Test
    public void readBasketsFile() throws Exception {
        PublicationLog expected=new PublicationLog("ch.so.afu.gewaesserschutz", PublisherStepOld.parsePublicationTimestamp("2021-12-23T14:54:49.050062"));
        PublishedBasket expectedBasket1=new PublishedBasket("SO_AGI_MOpublic_20201009","Bodenbedeckung","oltenBID");
        expected.addPublishedBasket(expectedBasket1);
        PublishedBasket expectedBasket2=new PublishedBasket("DM01","Liegenschaften","wangenBID");
        expected.addPublishedBasket(expectedBasket2);
        final Path file = localTestOut.resolve("basketsPublication.json");
        PublisherStepOld.writePublication(file,expected);
        PublicationLog actual= PublisherStepOld.readPublication(file);
        assertEquals(expected.getDataIdent(), actual.getDataIdent());
        assertEquals(expected.getPublished(), actual.getPublished());
        assertEquals(2,actual.getPublishedBaskets().size());
        assertEquals("SO_AGI_MOpublic_20201009",actual.getPublishedBaskets().get(0).getModel());
        assertEquals("Bodenbedeckung",actual.getPublishedBaskets().get(0).getTopic());
        assertEquals("oltenBID",actual.getPublishedBaskets().get(0).getBasket());
        assertEquals("DM01",actual.getPublishedBaskets().get(1).getModel());
        assertEquals("Liegenschaften",actual.getPublishedBaskets().get(1).getTopic());
        assertEquals("wangenBID",actual.getPublishedBaskets().get(1).getBasket());
        assertNull(actual.getPublishedRegions());
    }
    @Test
    public void readRegionsFile() throws Exception {
        PublicationLog expected=new PublicationLog("ch.so.afu.gewaesserschutz", PublisherStepOld.parsePublicationTimestamp("2021-12-23T14:54:49.050062"));
        PublishedRegion expectedRegion1=new PublishedRegion("olten");
        expectedRegion1.addPublishedBasket(new PublishedBasket("SO_AGI_MOpublic_20201009","Bodenbedeckung","oltenBID"));
        expected.addPublishedRegion(expectedRegion1);
        PublishedRegion expectedRegion2=new PublishedRegion("wangen");
        expectedRegion2.addPublishedBasket(new PublishedBasket("SO_AGI_MOpublic_20201009","Bodenbedeckung","wangenBID"));
        expected.addPublishedRegion(expectedRegion2);
        final Path file = localTestOut.resolve("regionsPublication.json");
        PublisherStepOld.writePublication(file,expected);
        PublicationLog actual= PublisherStepOld.readPublication(file);
        assertEquals(expected.getDataIdent(), actual.getDataIdent());
        assertEquals(expected.getPublished(), actual.getPublished());
        assertNull(actual.getPublishedBaskets());
        assertEquals(2,actual.getPublishedRegions().size());
        assertEquals("olten",actual.getPublishedRegions().get(0).getRegion());
        assertEquals(1,actual.getPublishedRegions().get(0).getPublishedBaskets().size());
        assertEquals("SO_AGI_MOpublic_20201009",actual.getPublishedRegions().get(0).getPublishedBaskets().get(0).getModel());
        assertEquals("Bodenbedeckung",actual.getPublishedRegions().get(0).getPublishedBaskets().get(0).getTopic());
        assertEquals("oltenBID",actual.getPublishedRegions().get(0).getPublishedBaskets().get(0).getBasket());
        assertEquals("wangen",actual.getPublishedRegions().get(1).getRegion());
        assertEquals(1,actual.getPublishedRegions().get(1).getPublishedBaskets().size());
        assertEquals("SO_AGI_MOpublic_20201009",actual.getPublishedRegions().get(1).getPublishedBaskets().get(0).getModel());
        assertEquals("Bodenbedeckung",actual.getPublishedRegions().get(1).getPublishedBaskets().get(0).getTopic());
        assertEquals("wangenBID",actual.getPublishedRegions().get(1).getPublishedBaskets().get(0).getBasket());
    }
}
