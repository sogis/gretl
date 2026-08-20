package ch.so.agi.gretl.steps.publisher.out.metainfo.json;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;

/**
 * Input for loading one JSON document from a path-style S3 HTTP endpoint.
 */
public final class JsonSectionWriterParameters implements OperationParameters {
    private final String address;
    private final String bucket;
    private final String fileName;
    private final String ident;
    private final Path targetFile;

    private JsonSectionWriterParameters(String address, String bucket, String fileName, String ident, Path targetFile) {
        this.address = requireHttpAddress(address);
        this.bucket = requireText(bucket, "bucket");
        this.fileName = requireText(fileName, "fileName");
        this.ident = requireText(ident, "ident");
        this.targetFile = Objects.requireNonNull(targetFile, "targetFile must not be null");
    }

    public static JsonSectionWriterParameters of(String address, String bucket, String fileName, String ident,
            Path targetFile) {
        return new JsonSectionWriterParameters(address, bucket, fileName, ident, targetFile);
    }

    public String getAddress() {
        return address;
    }

    public String getBucket() {
        return bucket;
    }

    public String getFileName() {
        return fileName;
    }

    public String getIdent() {
        return ident;
    }

    public Path getTargetFile() {
        return targetFile;
    }

    private static String requireHttpAddress(String value) {
        String address = requireText(value, "address");
        try {
            URI uri = new URI(address);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("address must be an absolute URL");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("address must use http or https");
            }
            if (uri.getQuery() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("address must not contain a query or fragment");
            }
            return address;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("address must be a valid URL", exception);
        }
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
