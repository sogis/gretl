package ch.so.agi.gretl.steps.publisher.stage.derivedformats;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleTransferDerivatorTest {
    @Test
    void removesIntermediatesAfterSuccessfulDerivation() throws IOException {
        Path cacheDir = Files.createTempDirectory("single-transfer");
        Path transferDir = Files.createDirectories(cacheDir.resolve("nested"));
        copyJobInput(transferDir);

        new SingleTransferDerivator(
                transferDir.resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.xtf"),
                List.of(DerivedFormat.GPKG)).derive();

        assertTrue(Files.isRegularFile(transferDir.resolve("gpkg").resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg")));
        assertFalse(Files.exists(transferDir.resolve("derivation_intermediates")));
    }

    @Test
    void removesIntermediatesAfterFailedDerivation() throws IOException {
        Path cacheDir = Files.createTempDirectory("single-transfer");
        Path transferDir = Files.createDirectories(cacheDir.resolve("nested"));
        Files.writeString(transferDir.resolve("broken.xtf"), "<TRANSFER />");

        try {
            new SingleTransferDerivator(
                    transferDir.resolve("broken.xtf"),
                    List.of(DerivedFormat.SHP)).derive();
        } catch (IllegalStateException expected) {
            // expected
        }

        assertFalse(Files.exists(transferDir.resolve("derivation_intermediates")));
    }

    private void copyJobInput(Path targetDir) throws IOException {
        Path sourceJobDir = Path.of(System.getProperty("user.dir"), "src", "integrationTest", "jobs", "Ili2gpkgImport");
        Files.copy(sourceJobDir.resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.xtf"), targetDir.resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.xtf"));
        Files.copy(sourceJobDir.resolve("SO_AGI_AV_GB_Administrative_Einteilungen_20180613.ili"), targetDir.resolve("SO_AGI_AV_GB_Administrative_Einteilungen_20180613.ili"));
    }
}
