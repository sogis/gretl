package ch.so.agi.gretl.tasks.impl.publisher;

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
    private static final String JSONMETA_ADDRESS = "jsonmetaAddress";
    private static final String JSONMETA_BUCKET = "jsonmetaBucket";
    private static final String JSONMETA_FILE_NAME = "jsonmetaFileName";
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
        String jsonmetaAddress = readRequiredProperty(JSONMETA_ADDRESS);
        String jsonmetaBucket = readRequiredProperty(JSONMETA_BUCKET);
        String jsonmetaFileName = readRequiredProperty(JSONMETA_FILE_NAME);
        String modelDir = readRequiredProperty(MODEL_DIR);
        Path groomingConfigFilePath = Path.of(readRequiredProperty(GROOMING_CONFIG_FILE_PATH)).normalize();

        PupDateEnv pupDateEnv = new PupDateEnv(pubDateDbUrl, pubDateDbSchema, pubDateDbUser, pubDateDbPass);
        PubFolderEnv pubFolderEnv = new PubFolderEnv(pupFolderPath, pupFolderUser, pupFolderPass);

        return new PublisherEnv(pupDateEnv, pubFolderEnv, jsonmetaAddress, jsonmetaBucket, jsonmetaFileName,
                modelDir, groomingConfigFilePath);
    }

    private String readRequiredProperty(String propertyName) {
        Object propertyValue = project.findProperty(propertyName);
        String trimmedValue = propertyValue == null ? null : propertyValue.toString().trim();
        if (trimmedValue == null || trimmedValue.isEmpty()) {
            throw new IllegalStateException("Missing required Gradle property: " + propertyName);
        }
        return trimmedValue;
    }

}
