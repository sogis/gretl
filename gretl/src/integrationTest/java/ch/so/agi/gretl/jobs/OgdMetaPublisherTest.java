package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.interlis2.validator.Validator;
import org.junit.Test;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;


public class OgdMetaPublisherTest {
    @Test
    public void single_resource_no_identifier_Ok() throws Exception {        
        // Run GRETL task
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/OgdMetaPublisher", gvs);
                
        // Validate result
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, Validator.SETTING_DEFAULT_ILIDIRS + ";src/main/resources/ogdmetapublisher/ili/");

        Path xtfFile = Paths.get("src/integrationTest/jobs/OgdMetaPublisher", "meta-ch.so.hba.kantonale_gebaeude.xtf");
        boolean valid = Validator.runValidation(xtfFile.toString(), settings);
        assertTrue(valid);
        
        String content = new String(Files.readAllBytes(xtfFile));
        assertTrue(content.contains("<Description>Anzahl kantonale (nicht-öffentliche) Ladestationen</Description>"));
    }
}
