package ch.so.agi.gretl.steps.publisher.stage.derivedformats;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleTransferDerivatorTest {
    @Test
    void reusesExistingGpkgForDownstreamDerivation() throws IOException {
        Path cacheDir = Files.createTempDirectory("single-transfer");
        Path transferDir = Files.createDirectories(cacheDir.resolve("nested"));
        copyJobInput(transferDir);

        Path transferFile = transferDir.resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.xtf");
        new SingleTransferDerivator(transferFile, List.of(DerivedFormat.GPKG)).derive();

        Path gpkg = transferDir.resolve("gpkg").resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg");
        FileTime expectedTimestamp = FileTime.fromMillis(946684800000L);
        Files.setLastModifiedTime(gpkg, expectedTimestamp);

        new SingleTransferDerivator(transferFile, List.of(DerivedFormat.SHP)).derive();

        assertTrue(Files.isRegularFile(gpkg));
        assertEquals(expectedTimestamp, Files.getLastModifiedTime(gpkg));
        assertTrue(Files.isRegularFile(transferDir.resolve("shp").resolve("gemeinde.shp")));
    }

    @Test
    void reportsFailedDerivation() throws IOException {
        Path cacheDir = Files.createTempDirectory("single-transfer");
        Path transferDir = Files.createDirectories(cacheDir.resolve("nested"));
        Files.writeString(transferDir.resolve("broken.xtf"), "<TRANSFER />");

        assertThrows(IllegalStateException.class, () -> new SingleTransferDerivator(
                transferDir.resolve("broken.xtf"), List.of(DerivedFormat.SHP)).derive());
    }

    private void copyJobInput(Path targetDir) throws IOException {
        Path sourceJobDir = Path.of(System.getProperty("user.dir"), "src", "integrationTest", "jobs", "Ili2gpkgImport");
        Files.copy(sourceJobDir.resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.xtf"), targetDir.resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.xtf"));
        Files.copy(sourceJobDir.resolve("SO_AGI_AV_GB_Administrative_Einteilungen_20180613.ili"), targetDir.resolve("SO_AGI_AV_GB_Administrative_Einteilungen_20180613.ili"));
    }
}
