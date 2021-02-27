package ch.so.agi.gretl.jobs;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import ch.so.agi.gretl.util.IntegrationTestUtil;

public class Ili2h2gisExportTest {
    @Test
    public void export_Ok() throws Exception {
        // Prepare
        String[] files = new File("src/integrationTest/jobs/Ili2h2gisExport/").list();
        for (String file : files) {
            if (file.contains("itf")) {
                Paths.get("src/integrationTest/jobs/Ili2h2gisExport/", file).toFile().delete();
            }
        }
        
        // Run GRETL job
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2h2gisExport");
        
        // Check results
        // ITF file is validated in gretl job but still can contain no data at all.
        File itfFile = new File("src/integrationTest/jobs/Ili2h2gisExport/254900.itf");
        Assert.assertTrue(itfFile.length() > 1000000);
    }
}
