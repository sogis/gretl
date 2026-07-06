package ch.so.agi.gretl.steps.publisher.out.metainfo.sheet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.Operation;

public final class SheetDownloader implements Operation<SheetDownloaderParameters> {
    private static final String META_DIRECTORY_NAME = "meta";
    private static final String SHEET_FILE_NAME = "datenbeschreibung.html";

    @Override
    public void execute(SheetDownloaderParameters operationParameters) throws IOException {
        downloadSheet(operationParameters);
    }

    Path downloadSheet(SheetDownloaderParameters operationParameters) throws IOException {
        Objects.requireNonNull(operationParameters, "operationParameters must not be null");

        Path metaDirectory = Files.createDirectories(
                operationParameters.getPublicationRootDirectory().resolve(META_DIRECTORY_NAME));
        Path targetFile = metaDirectory.resolve(SHEET_FILE_NAME);
        URL requestUrl = new URL(buildRequestUrl(operationParameters.getBaseUrl(), operationParameters.getDataIdent()));

        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/html");

        try {
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("failed to download sheet from <" + requestUrl + "> with status <"
                        + statusCode + ">");
            }

            try (InputStream inputStream = connection.getInputStream()) {
                Files.writeString(targetFile, new String(inputStream.readAllBytes(), StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8);
            }
            return targetFile;
        } finally {
            connection.disconnect();
        }
    }

    private String buildRequestUrl(String baseUrl, String dataIdent) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "dataident=" + URLEncoder.encode(dataIdent, StandardCharsets.UTF_8);
    }
}
