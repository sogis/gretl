package ch.so.agi.gretl.testutil;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.steps.AbstractPublisherStepOldTest;
import ch.so.agi.gretl.steps.PublisherStepOld;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileListTest {
    @Test
    public void filesTest() throws Exception {
        List<String> regions= PublisherStepOld.listRegions(Paths.get(AbstractPublisherStepOldTest.SRC_TEST_DATA_FILES), "[0-9][0-9][0-9][0-9]", "itf");
        assertEquals(2, regions.size());
        assertTrue(regions.contains("2501"));
        assertTrue(regions.contains("2502"));
    }

}
