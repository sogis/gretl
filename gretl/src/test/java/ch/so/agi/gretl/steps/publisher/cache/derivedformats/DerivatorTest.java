package ch.so.agi.gretl.steps.publisher.cache.derivedformats;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DerivatorTest {
    @Test
    void findsTransferFilesRecursively() throws IOException {
        Path cacheDir = Files.createTempDirectory("derivator");
        Path nested1 = Files.createDirectories(cacheDir.resolve("a").resolve("b"));
        Path nested2 = Files.createDirectories(cacheDir.resolve("c").resolve("d"));
        copyJobInput(nested1);
        copyJobInput(nested2);

        Derivator derivator = new Derivator(List.of(DerivedFormat.GPKG), cacheDir);
        derivator.deriveAllTransferFiles();

        assertTrue(Files.isRegularFile(nested1.resolve("gpkg").resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg")));
        assertTrue(Files.isRegularFile(nested2.resolve("gpkg").resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg")));
        assertFalse(Files.exists(nested1.resolve("derivation_intermediates")));
        assertFalse(Files.exists(nested2.resolve("derivation_intermediates")));
    }

    @Test
    void derivesSharedGpkgOncePerTransferFileForDownstreamFormats() throws IOException {
        Path cacheDir = Files.createTempDirectory("derivator");
        Path nested = Files.createDirectories(cacheDir.resolve("single").resolve("source"));
        copyJobInput(nested);

        Derivator derivator = new Derivator(List.of(DerivedFormat.SHP), cacheDir);
        derivator.deriveAllTransferFiles();

        assertTrue(Files.isRegularFile(nested.resolve("shp").resolve("gemeinde.shp")));
        assertTrue(Files.isRegularFile(nested.resolve("shp").resolve("grundbuchkreis.shp")));
        assertFalse(Files.exists(nested.resolve("derivation_intermediates")));
    }

    private void copyJobInput(Path targetDir) throws IOException {
        Path sourceJobDir = Path.of(System.getProperty("user.dir"), "src", "integrationTest", "jobs", "Ili2gpkgImport");
        Files.copy(sourceJobDir.resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.xtf"), targetDir.resolve("ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.xtf"));
        Files.copy(sourceJobDir.resolve("SO_AGI_AV_GB_Administrative_Einteilungen_20180613.ili"), targetDir.resolve("SO_AGI_AV_GB_Administrative_Einteilungen_20180613.ili"));
    }
}
