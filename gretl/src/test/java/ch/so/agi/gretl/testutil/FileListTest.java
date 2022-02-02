package ch.so.agi.gretl.testutil;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.interlis.iox.IoxReader;
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

    @Test
    public void openFileTest() throws Exception {
        Path dataFile=Paths.get("build/out/av_test.itf");
        //List<String> modelnameFromFile = ch.interlis.iox_j.IoxUtility.getModels(dataFile.toFile());
        IoxReader reader = new ch.interlis.iox_j.utility.ReaderFactory().createReader(dataFile.toFile(), null);
        reader.close();
        Files.delete(dataFile);
    }
}
