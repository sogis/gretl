package ch.so.agi.gretl.steps.publisher.out.metainfo.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MapperTest {
    @TempDir
    Path tempDir;

    @Test
    public void map_globalPublication_usesAllParts() {
        Mapper mapper = new Mapper();
        Mapper.PublicationTree publicationTree = mapper.map(
                "mypublication",
                List.of(tempDir.resolve("mypublication.xtf.zip"))
        );

        assertEquals("mypublication", publicationTree.getPublicationIdent());
        assertEquals(1, publicationTree.getParts().size());
        assertEquals(Mapper.ALL_PARTS, publicationTree.getParts().get(0).getPartIdent());
        assertEquals(List.of("xtf"), publicationTree.getArtifactTypes());
        assertEquals("xtf", publicationTree.getFiles().get(0).getArtifactType());
    }

    @Test
    public void map_partitionedPublication_derivesPartFromFileNamePrefix() {
        Mapper mapper = new Mapper();
        Mapper.PublicationTree publicationTree = mapper.map(
                "mypublication",
                List.of(
                        tempDir.resolve("north.mypublication.xtf.zip"),
                        tempDir.resolve("north.mypublication.gpkg.zip"),
                        tempDir.resolve("south.mypublication.xtf.zip")
                )
        );

        assertEquals(2, publicationTree.getParts().size());
        assertEquals("north", publicationTree.getParts().get(0).getPartIdent());
        assertEquals("south", publicationTree.getParts().get(1).getPartIdent());
        assertEquals(List.of("xtf", "gpkg"), publicationTree.getArtifactTypes());
    }

    @Test
    public void inferArtifactType_usesExtensionBeforeZip() {
        Mapper mapper = new Mapper();

        assertEquals("xtf", mapper.inferArtifactType("north.mypublication.xtf.zip"));
        assertEquals("gpkg", mapper.inferArtifactType("north.mypublication.gpkg.zip"));
        assertEquals("shape", mapper.inferArtifactType("north.mypublication.shape"));
    }
}
