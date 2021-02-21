package ch.so.agi.gretl.jobs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ch.so.agi.gretl.testutil.ThirdPartyPluginTest;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class DownloadTest {
    @Category(ThirdPartyPluginTest.class)
    @Test
    public void downloadFileHttp() throws Exception {
        IntegrationTestUtil.runJob("src/integrationTest/jobs/DownloadFile");
        
        String content = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/DownloadFile", "index.html")));
        Assert.assertTrue(content.contains("Example Domain"));
    }
}
