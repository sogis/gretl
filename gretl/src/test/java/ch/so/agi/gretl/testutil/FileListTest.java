package ch.so.agi.gretl.testutil;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.so.agi.gretl.steps.AbstractPublisherStepTest;
import ch.so.agi.gretl.steps.PublisherStep;

public class FileListTest {
    @Test
    public void filesTest() throws Exception {
        List<String> regions=PublisherStep.listRegions(Paths.get(AbstractPublisherStepTest.SRC_TEST_DATA_FILES), "[0-9][0-9][0-9][0-9]", "itf");
        Assert.assertEquals(2, regions.size());
        Assert.assertTrue(regions.contains("2501"));
        Assert.assertTrue(regions.contains("2502"));
    }

}
