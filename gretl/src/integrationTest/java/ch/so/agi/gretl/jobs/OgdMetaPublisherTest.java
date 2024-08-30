package ch.so.agi.gretl.jobs;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.interlis2.validator.Validator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OgdMetaPublisherTest {
    @Test
    public void single_resource_no_identifier_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/OgdMetaPublisher");
        IntegrationTestUtil.executeTestRunner(projectDirectory, "publishMeta");
                
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
