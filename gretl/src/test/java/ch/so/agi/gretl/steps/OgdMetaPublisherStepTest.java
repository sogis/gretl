package ch.so.agi.gretl.steps;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.testutil.TestUtil;
import org.interlis2.validator.Validator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class OgdMetaPublisherStepTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    // TODO
    // Test mit Subdatasets
    
    @Test
    public void single_resource_no_identifier_Ok() throws Exception {
        // Prepare
        File configFile = TestUtil.getResourceFile(TestUtil.KANTONALE_GEBAEUDE_TOML_PATH);
        Path outputPath = folder.newFolder().toPath();
       
        // Run
        OgdMetaPublisherStep ogdMetaPublisherStep = new OgdMetaPublisherStep();
        ogdMetaPublisherStep.execute(configFile.toPath(), outputPath);
        
        // Validate
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, Validator.SETTING_DEFAULT_ILIDIRS + ";src/main/resources/ogdmetapublisher/ili/");

        Path xtfFile = Paths.get(outputPath.toFile().getAbsolutePath(), "meta-ch.so.hba.kantonale_gebaeude.xtf");
        boolean valid = Validator.runValidation(xtfFile.toString(), settings);
        assertTrue(valid);
        
        String content = new String(Files.readAllBytes(xtfFile));
        assertTrue(content.contains("<Description>Anzahl kantonale (nicht-öffentliche) Ladestationen</Description>"));
    }

}
