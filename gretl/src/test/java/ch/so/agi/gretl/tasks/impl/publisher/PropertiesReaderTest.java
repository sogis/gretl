package ch.so.agi.gretl.tasks.impl.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class PropertiesReaderTest {
    @Test
    void readsPublisherProperties() {
        Project project = baseProject();

        PropertiesReader reader = new PropertiesReader(project);

        var env = reader.readProperties();

        assertEquals("jdbc:postgresql://db/publisher", env.getPupDateEnv().getConnectionUrl());
        assertEquals("pub_meta", env.getPupDateEnv().getDbSchema());
        assertEquals("publisher_user", env.getPupDateEnv().getUser());
        assertEquals("publisher_pass", env.getPupDateEnv().getPassword());
        assertEquals("sftp://host/data", env.getPubFolderEnv().getPath());
        assertEquals("sftp_user", env.getPubFolderEnv().getUser());
        assertEquals("sftp_pass", env.getPubFolderEnv().getPassword());
        assertEquals("https://geo.so.ch/datasheet", env.getDatasheetUri().toString());
        assertEquals("/models;/more-models", env.getModeldir());
        assertEquals(Path.of("/tmp/grooming.json"), env.getGroomingConfigFilePath());
    }

    @Test
    void trimsPublisherProperties() {
        Project project = baseProject();
        project.getExtensions().getExtraProperties().set("pubDateDbUrl", "  jdbc:postgresql://db/publisher  ");
        project.getExtensions().getExtraProperties().set("pupFolderUser", "  sftp_user  ");
        project.getExtensions().getExtraProperties().set("modelDir", "  /models;/more-models  ");

        PropertiesReader reader = new PropertiesReader(project);

        var env = reader.readProperties();

        assertEquals("jdbc:postgresql://db/publisher", env.getPupDateEnv().getConnectionUrl());
        assertEquals("sftp_user", env.getPubFolderEnv().getUser());
        assertEquals("/models;/more-models", env.getModeldir());
    }

    @Test
    void rejectsMissingRequiredProperty() {
        Project project = baseProject();
        project.getExtensions().getExtraProperties().set("pubDateDbSchema", null);

        PropertiesReader reader = new PropertiesReader(project);

        IllegalStateException exception = assertThrows(IllegalStateException.class, reader::readProperties);

        assertTrue(exception.getMessage().contains("pubDateDbSchema"));
    }

    @Test
    void rejectsBlankRequiredProperty() {
        Project project = baseProject();
        project.getExtensions().getExtraProperties().set("modelDir", "   ");

        PropertiesReader reader = new PropertiesReader(project);

        IllegalStateException exception = assertThrows(IllegalStateException.class, reader::readProperties);

        assertTrue(exception.getMessage().contains("modelDir"));
    }

    @Test
    void rejectsRelativeDatasheetUrl() {
        Project project = baseProject();
        project.getExtensions().getExtraProperties().set("datasheetUrl", "/datasheet");

        PropertiesReader reader = new PropertiesReader(project);

        IllegalStateException exception = assertThrows(IllegalStateException.class, reader::readProperties);

        assertTrue(exception.getMessage().contains("absolute URL"));
    }

    @Test
    void rejectsNonHttpDatasheetUrl() {
        Project project = baseProject();
        project.getExtensions().getExtraProperties().set("datasheetUrl", "ftp://geo.so.ch/datasheet");

        PropertiesReader reader = new PropertiesReader(project);

        IllegalStateException exception = assertThrows(IllegalStateException.class, reader::readProperties);

        assertTrue(exception.getMessage().contains("http or https"));
    }

    @Test
    void readsNormalizedGroomingConfigPath() {
        Project project = baseProject();
        project.getExtensions().getExtraProperties().set("groomingConfigFilePath", "/tmp/publisher/../grooming.json");

        PropertiesReader reader = new PropertiesReader(project);

        var env = reader.readProperties();

        assertEquals(Path.of("/tmp/grooming.json"), env.getGroomingConfigFilePath());
    }

    private static Project baseProject() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("pubDateDbUrl", "jdbc:postgresql://db/publisher");
        project.getExtensions().getExtraProperties().set("pubDateDbSchema", "pub_meta");
        project.getExtensions().getExtraProperties().set("pubDateDbUser", "publisher_user");
        project.getExtensions().getExtraProperties().set("pubDateDbPass", "publisher_pass");
        project.getExtensions().getExtraProperties().set("pupFolderPath", "sftp://host/data");
        project.getExtensions().getExtraProperties().set("pupFolderUser", "sftp_user");
        project.getExtensions().getExtraProperties().set("pupFolderPass", "sftp_pass");
        project.getExtensions().getExtraProperties().set("datasheetUrl", "https://geo.so.ch/datasheet");
        project.getExtensions().getExtraProperties().set("modelDir", "/models;/more-models");
        project.getExtensions().getExtraProperties().set("groomingConfigFilePath", "/tmp/grooming.json");
        return project;
    }
}
