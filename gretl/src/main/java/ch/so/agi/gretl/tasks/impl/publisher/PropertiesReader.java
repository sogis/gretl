package ch.so.agi.gretl.tasks.impl.publisher;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.gradle.api.Project;

import ch.so.agi.gretl.steps.publisher.util.env.PubFolderEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PupDateEnv;

/**
 * Reads the Properties for the Publisher and
 * returns a PublisherEnv instance as result.
 */
public class PropertiesReader {
    private static final String PUB_DATE_DB_URL = "pubDateDbUrl";
    private static final String PUB_DATE_DB_SCHEMA = "pubDateDbSchema";
    private static final String PUB_DATE_DB_USER = "pubDateDbUser";
    private static final String PUB_DATE_DB_PASS = "pubDateDbPass";
    private static final String PUP_FOLDER_PATH = "pupFolderPath";
    private static final String PUP_FOLDER_USER = "pupFolderUser";
    private static final String PUP_FOLDER_PASS = "pupFolderPass";
    private static final String DATASHEET_URL = "datasheetUrl";
    private static final String MODEL_DIR = "modelDir";
    private static final String GROOMING_CONFIG_FILE_PATH = "groomingConfigFilePath";

    private final Project project;

    public PropertiesReader(Project project) {
        this.project = project;
    }

    public PublisherEnv readProperties() {
        String pubDateDbUrl = readRequiredProperty(PUB_DATE_DB_URL);
        String pubDateDbSchema = readRequiredProperty(PUB_DATE_DB_SCHEMA);
        String pubDateDbUser = readRequiredProperty(PUB_DATE_DB_USER);
        String pubDateDbPass = readRequiredProperty(PUB_DATE_DB_PASS);
        String pupFolderPath = readRequiredProperty(PUP_FOLDER_PATH);
        String pupFolderUser = readRequiredProperty(PUP_FOLDER_USER);
        String pupFolderPass = readRequiredProperty(PUP_FOLDER_PASS);
        URI datasheetUri = parseDatasheetUri(readRequiredProperty(DATASHEET_URL));
        String modelDir = readRequiredProperty(MODEL_DIR);
        Path groomingConfigFilePath = Path.of(readRequiredProperty(GROOMING_CONFIG_FILE_PATH)).normalize();

        PupDateEnv pupDateEnv = new PupDateEnv(pubDateDbUrl, pubDateDbSchema, pubDateDbUser, pubDateDbPass);
        PubFolderEnv pubFolderEnv = new PubFolderEnv(pupFolderPath, pupFolderUser, pupFolderPass);

        return new PublisherEnv(pupDateEnv, pubFolderEnv, datasheetUri, modelDir, groomingConfigFilePath);
    }

    private String readRequiredProperty(String propertyName) {
        Object propertyValue = project.findProperty(propertyName);
        String trimmedValue = propertyValue == null ? null : propertyValue.toString().trim();
        if (trimmedValue == null || trimmedValue.isEmpty()) {
            throw new IllegalStateException("Missing required Gradle property: " + propertyName);
        }
        return trimmedValue;
    }

    private URI parseDatasheetUri(String propertyValue) {
        URI uri;
        try {
            uri = new URI(propertyValue);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URI in Gradle property " + DATASHEET_URL, e);
        }

        if (!uri.isAbsolute()) {
            throw new IllegalStateException("Gradle property " + DATASHEET_URL + " must be an absolute URL");
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalStateException("Gradle property " + DATASHEET_URL + " must use http or https");
        }

        return uri;
    }
}
