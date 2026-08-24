package ch.so.agi.gretl.steps.publisher.stage.pack;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.so.agi.gretl.steps.publisher.PartWorkspace;

class PublicationArtifactPackagerTest {
    @TempDir Path tempDir;

    @Test
    void packagesEachPartAndFormatIntoFlattenedPublicationArtifacts() throws Exception {
        Path raw = PartWorkspace.rawRoot(tempDir);
        Path aTransfer = PartWorkspace.transferRoot(raw, "a");
        Path aShp = PartWorkspace.formatRoot(raw, "a", "shp");
        Path bTransfer = PartWorkspace.transferRoot(raw, "b");
        Path bShp = PartWorkspace.formatRoot(raw, "b", "shp");
        Files.createDirectories(aTransfer);
        Files.createDirectories(aShp);
        Files.createDirectories(bTransfer);
        Files.createDirectories(bShp);
        Files.writeString(aTransfer.resolve("a.xtf"), "a");
        Files.writeString(bTransfer.resolve("b.xtf"), "b");
        Files.writeString(aShp.resolve("a.shp"), "shape");
        Files.writeString(aShp.resolve("a.dbf"), "attributes");
        Files.writeString(bShp.resolve("b.shp"), "shape");
        Files.writeString(bShp.resolve("b.dbf"), "attributes");

        Path publication = PartWorkspace.publicationRoot(tempDir);
        new PublicationArtifactPackager(PublicationArtifactPackagerParameters.of(raw, publication,
                List.of(OutputFormat.XTF, OutputFormat.SHP))).execute();

        String aKey = PartWorkspace.encode("a");
        String bKey = PartWorkspace.encode("b");
        assertTrue(Files.isRegularFile(publication.resolve(aKey + ".xtf.zip")));
        assertTrue(Files.isRegularFile(publication.resolve(aKey + ".shp.zip")));
        assertTrue(Files.isRegularFile(publication.resolve(bKey + ".xtf.zip")));
        assertTrue(Files.isRegularFile(publication.resolve(bKey + ".shp.zip")));
        try (ZipFile zip = new ZipFile(publication.resolve(aKey + ".shp.zip").toFile())) {
            assertTrue(zip.getEntry("a.shp") != null);
            assertTrue(zip.getEntry("a.dbf") != null);
        }
    }

    @Test
    void failsWhenRequestedDerivedFormatIsMissingForPart() throws Exception {
        Path raw = PartWorkspace.rawRoot(tempDir);
        Path transfer = PartWorkspace.transferRoot(raw, "a");
        Files.createDirectories(transfer);
        Files.writeString(transfer.resolve("a.xtf"), "a");
        Path publication = PartWorkspace.publicationRoot(tempDir);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new PublicationArtifactPackager(PublicationArtifactPackagerParameters.of(raw, publication,
                        List.of(OutputFormat.XTF, OutputFormat.SHP))).execute());

        String partKey = PartWorkspace.encode("a");
        assertTrue(exception.getMessage().contains(partKey));
        assertTrue(exception.getMessage().contains("shp"));
        assertTrue(Files.notExists(publication.resolve(partKey + ".xtf.zip")));
    }

    @Test
    void failsWhenRequestedTransferFormatIsMissingForPart() throws Exception {
        Path raw = PartWorkspace.rawRoot(tempDir);
        Path shp = PartWorkspace.formatRoot(raw, "a", "shp");
        Files.createDirectories(shp);
        Files.writeString(shp.resolve("a.shp"), "shape");
        Path publication = PartWorkspace.publicationRoot(tempDir);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new PublicationArtifactPackager(PublicationArtifactPackagerParameters.of(raw, publication,
                        List.of(OutputFormat.XTF, OutputFormat.SHP))).execute());

        String partKey = PartWorkspace.encode("a");
        assertTrue(exception.getMessage().contains(partKey));
        assertTrue(exception.getMessage().contains("xtf"));
        assertTrue(Files.notExists(publication.resolve(partKey + ".shp.zip")));
    }
}
