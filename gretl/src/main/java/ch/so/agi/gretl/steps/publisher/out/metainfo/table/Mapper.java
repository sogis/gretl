package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Translates publication payload objects into table row data.
 *
 * <p>The mapper normalizes the file names that were written into the cache
 * temp folder and derives the publication parts plus the artifact kinds from
 * them. Global publications are normalized to the canonical part identifier
 * {@value #ALL_PARTS}.</p>
 */
public class Mapper {
    public static final String ALL_PARTS = "allparts";

    /**
     * Converts cached transfer files into a normalized publication tree.
     *
     * @param publicationIdent the logical publication identifier, usually the
     *                         topic publication or dataset identifier
     * @param cachedTransferFiles the files written into the cache temp folder
     * @return normalized publication tree with distinct parts and artifact types
     */
    public PublicationTree map(String publicationIdent, List<Path> cachedTransferFiles) {
        Objects.requireNonNull(publicationIdent, "publicationIdent");
        Objects.requireNonNull(cachedTransferFiles, "cachedTransferFiles");

        Map<String, PartRow> parts = new LinkedHashMap<>();
        LinkedHashSet<String> artifactTypes = new LinkedHashSet<>();
        List<FileRow> files = new ArrayList<>();

        for (Path transferFile : cachedTransferFiles) {
            Objects.requireNonNull(transferFile, "cachedTransferFiles entry");
            String fileName = transferFile.getFileName().toString();
            String artifactType = inferArtifactType(fileName);
            String partIdent = inferPartIdent(publicationIdent, fileName);

            parts.computeIfAbsent(partIdent, PartRow::new);
            artifactTypes.add(artifactType);
            files.add(new FileRow(transferFile, fileName, partIdent, artifactType));
        }

        return new PublicationTree(
                publicationIdent,
                null,
                new ArrayList<>(parts.values()),
                new ArrayList<>(artifactTypes),
                files
        );
    }

    /** Maps the resolved publication state rather than inferring semantics from archive names. */
    public PublicationTree map(String publicationIdent, LocalDate publicationDate, List<String> partIdentifiers,
            List<String> exportedFormats) {
        Objects.requireNonNull(publicationIdent, "publicationIdent");
        Objects.requireNonNull(publicationDate, "publicationDate");
        Objects.requireNonNull(partIdentifiers, "partIdentifiers");
        Objects.requireNonNull(exportedFormats, "exportedFormats");

        Map<String, PartRow> parts = new LinkedHashMap<>();
        for (String partIdentifier : partIdentifiers) {
            String checkedPartIdentifier = requireText(partIdentifier, "partIdentifier");
            parts.computeIfAbsent(checkedPartIdentifier, PartRow::new);
        }
        LinkedHashSet<String> formats = new LinkedHashSet<>();
        for (String exportedFormat : exportedFormats) {
            formats.add(requireText(exportedFormat, "exportedFormat").toLowerCase(Locale.ROOT));
        }
        return new PublicationTree(publicationIdent, publicationDate, new ArrayList<>(parts.values()),
                new ArrayList<>(formats), Collections.emptyList());
    }

    /**
     * Derives the publication part from the cached file name.
     */
    public String inferPartIdent(String publicationIdent, String fileName) {
        String baseName = stripZipExtension(fileName);
        String stem = stripLastExtension(baseName);
        int dotIndex = stem.indexOf('.');
        if (dotIndex < 0) {
            return stem.equals(publicationIdent) ? ALL_PARTS : stem;
        }

        String candidate = stem.substring(0, dotIndex);
        return candidate.equals(publicationIdent) ? ALL_PARTS : candidate;
    }

    /**
     * Derives the artifact type from the cached file name.
     */
    public String inferArtifactType(String fileName) {
        String baseName = stripZipExtension(fileName);
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex < 0) {
            return baseName.toLowerCase(Locale.ROOT);
        }
        return baseName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String stripZipExtension(String fileName) {
        if (fileName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private String stripLastExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    /**
     * Normalized publication tree used by the writer and groomer.
     */
    public static final class PublicationTree {
        private final String publicationIdent;
        private final LocalDate publicationDate;
        private final List<PartRow> parts;
        private final List<String> artifactTypes;
        private final List<FileRow> files;

        PublicationTree(String publicationIdent, LocalDate publicationDate, List<PartRow> parts, List<String> artifactTypes,
                List<FileRow> files) {
            this.publicationIdent = publicationIdent;
            this.publicationDate = publicationDate;
            this.parts = parts;
            this.artifactTypes = artifactTypes;
            this.files = files;
        }

        public String getPublicationIdent() {
            return publicationIdent;
        }

        public LocalDate getPublicationDate() {
            return publicationDate;
        }

        public List<PartRow> getParts() {
            return parts;
        }

        public List<String> getArtifactTypes() {
            return artifactTypes;
        }

        public List<FileRow> getFiles() {
            return files;
        }
    }

    /**
     * One logical part of a publication.
     */
    public static final class PartRow {
        private final String partIdent;

        PartRow(String partIdent) {
            this.partIdent = partIdent;
        }

        public String getPartIdent() {
            return partIdent;
        }
    }

    /**
     * One cached file with its derived publication semantics.
     */
    public static final class FileRow {
        private final Path path;
        private final String fileName;
        private final String partIdent;
        private final String artifactType;

        FileRow(Path path, String fileName, String partIdent, String artifactType) {
            this.path = path;
            this.fileName = fileName;
            this.partIdent = partIdent;
            this.artifactType = artifactType;
        }

        public Path getPath() {
            return path;
        }

        public String getFileName() {
            return fileName;
        }

        public String getPartIdent() {
            return partIdent;
        }

        public String getArtifactType() {
            return artifactType;
        }
    }
}
