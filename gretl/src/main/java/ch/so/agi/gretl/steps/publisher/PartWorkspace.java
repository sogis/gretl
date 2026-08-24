package ch.so.agi.gretl.steps.publisher;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

/** Defines the part-first local publisher workspace layout. */
public final class PartWorkspace {
    public static final String RAW = "raw";
    public static final String PUBLICATION = "aktuell_part";
    public static final String TRANSFER = "xtf";
    public static final String ILI_META = "ilimeta";
    public static final String WORK = ".work";

    private PartWorkspace() { }

    public static Path rawRoot(Path cacheRoot) { return cacheRoot.resolve(RAW); }
    public static Path publicationRoot(Path cacheRoot) { return cacheRoot.resolve(PUBLICATION); }
    public static Path workRoot(Path cacheRoot) { return cacheRoot.resolve(WORK); }
    public static Path partRoot(Path rawRoot, String partIdentifier) { return rawRoot.resolve(encode(partIdentifier)); }
    public static Path transferRoot(Path rawRoot, String partIdentifier) { return partRoot(rawRoot, partIdentifier).resolve(TRANSFER); }
    public static Path formatRoot(Path rawRoot, String partIdentifier, String format) {
        return partRoot(rawRoot, partIdentifier).resolve(format);
    }
    public static Path iliMetaRoot(Path rawRoot, String partIdentifier) { return partRoot(rawRoot, partIdentifier).resolve(ILI_META); }

    /** A reversible, path-safe representation of a part identifier. */
    public static String encode(String partIdentifier) {
        if (partIdentifier == null || partIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("partIdentifier must not be blank");
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(partIdentifier.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String partKey) {
        return new String(Base64.getUrlDecoder().decode(partKey), StandardCharsets.UTF_8);
    }
}
