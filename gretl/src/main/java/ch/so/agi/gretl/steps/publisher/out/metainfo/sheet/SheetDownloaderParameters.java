package ch.so.agi.gretl.steps.publisher.out.metainfo.sheet;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleDirectoryParameters;

public final class SheetDownloaderParameters extends AbstractSingleDirectoryParameters {
    private final String baseUrl;
    private final String dataIdent;

    private SheetDownloaderParameters(Path publicationRootDirectory, String baseUrl, String dataIdent) {
        super(publicationRootDirectory);
        this.baseUrl = requireAbsoluteHttpUrl(baseUrl, "baseUrl");
        this.dataIdent = requireText(dataIdent, "dataIdent");
    }

    public static SheetDownloaderParameters of(Path publicationRootDirectory, String baseUrl, String dataIdent) {
        return new SheetDownloaderParameters(publicationRootDirectory, baseUrl, dataIdent);
    }

    public Path getPublicationRootDirectory() {
        return getDirectory();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getDataIdent() {
        return dataIdent;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private static String requireAbsoluteHttpUrl(String value, String fieldName) {
        String url = requireText(value, fieldName);
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || uri.getHost() == null) {
                throw new IllegalArgumentException(fieldName + " must be an absolute URL");
            }
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException(fieldName + " must use http or https");
            }
            return url;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid URL", e);
        }
    }
}
