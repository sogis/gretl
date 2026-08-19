package ch.so.agi.gretl.steps.publisher.out.metainfo.json;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.publisher.operation.Operation;

/**
 * Writes the first JSON array element with a matching {@code ident} field.
 */
public final class JsonSectionWriter implements Operation<JsonSectionWriterParameters> {
    private final GretlLogger log;
    private final ObjectMapper objectMapper;

    public JsonSectionWriter() {
        this(LogEnvironment.getLogger(JsonSectionWriter.class), new ObjectMapper());
    }

    JsonSectionWriter(GretlLogger log, ObjectMapper objectMapper) {
        this.log = Objects.requireNonNull(log, "log must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public void execute(JsonSectionWriterParameters operationParameters) throws Exception {
        write(operationParameters);
    }

    void write(JsonSectionWriterParameters operationParameters) throws IOException {
        Objects.requireNonNull(operationParameters, "operationParameters must not be null");

        URL sourceUrl = new URL(buildSourceUrl(operationParameters));
        byte[] source = download(sourceUrl);
        if (source == null) {
            return;
        }

        JsonNode root = objectMapper.readTree(source);
        if (root == null || !root.isArray()) {
            throw new IOException("publication JSON at <" + sourceUrl + "> must have a top-level array");
        }

        JsonNode matchingSection = findFirstMatch(root, operationParameters.getIdent());
        if (matchingSection == null) {
            warn("no section with ident <" + operationParameters.getIdent() + "> found in <" + sourceUrl + ">");
            return;
        }
        writeTarget(operationParameters.getTargetFile(), matchingSection);
    }

    private byte[] download(URL sourceUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) sourceUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int statusCode = connection.getResponseCode();
            if (statusCode < HttpURLConnection.HTTP_OK || statusCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                warn("could not load publication JSON from <" + sourceUrl + ">: HTTP " + statusCode);
                return null;
            }
            try (java.io.InputStream inputStream = connection.getInputStream()) {
                return inputStream.readAllBytes();
            }
        } catch (IOException exception) {
            warn("could not load publication JSON from <" + sourceUrl + ">: " + exception.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JsonNode findFirstMatch(JsonNode root, String ident) {
        for (JsonNode section : root) {
            JsonNode sectionIdent = section.get("ident");
            if (sectionIdent != null && sectionIdent.isTextual() && ident.equals(sectionIdent.textValue())) {
                return section;
            }
        }
        return null;
    }

    private void writeTarget(Path targetFile, JsonNode section) throws IOException {
        Path absoluteTarget = targetFile.toAbsolutePath().normalize();
        Path parent = absoluteTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporaryFile = Files.createTempFile(parent, "json-section-", ".tmp");
        try {
            objectMapper.writeValue(temporaryFile.toFile(), section);
            try {
                Files.move(temporaryFile, absoluteTarget, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, absoluteTarget, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private String buildSourceUrl(JsonSectionWriterParameters parameters) {
        String address = parameters.getAddress().replaceFirst("/+$", "");
        return address + "/" + encodePathSegment(parameters.getBucket()) + "/"
                + encodePathSegment(parameters.getFileName());
    }

    private static String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void warn(String message) {
        log.lifecycle("Warning: " + message);
    }
}
