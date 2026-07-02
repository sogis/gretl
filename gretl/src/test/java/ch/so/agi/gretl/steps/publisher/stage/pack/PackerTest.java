package ch.so.agi.gretl.steps.publisher.stage.pack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackerTest {
    @TempDir
    Path tempDir;

    @Test
    void packsSingleFileAndValidationArtifacts() throws IOException {
        Path source = tempDir.resolve("ch.so.agi.demo.xtf");
        Files.write(source, "xtf-content".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("validation.log"), "validation-log".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("validation.ini"), "validation-ini".getBytes(StandardCharsets.UTF_8));

        new Packer().execute(PackerParameters.of(tempDir, "ch.so.agi.demo"));

        Path zip = tempDir.resolve("ch.so.agi.demo.xtf.zip");
        assertTrue(Files.exists(zip));
        assertFalse(Files.exists(tempDir.resolve(".pack")));

        assertZipEntries(zip, "ch.so.agi.demo.xtf", "validation.ini", "validation.log");
    }

    @Test
    void packsFolderContentsAndValidationLog() throws IOException {
        Path sourceDir = tempDir.resolve("ch.so.agi.demo.shp");
        Files.createDirectories(sourceDir);
        Files.write(sourceDir.resolve("parcel.shp"), "shp".getBytes(StandardCharsets.UTF_8));
        Files.write(sourceDir.resolve("parcel.dbf"), "dbf".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("validation.log"), "validation-log".getBytes(StandardCharsets.UTF_8));

        new Packer().execute(PackerParameters.of(tempDir, "ch.so.agi.demo"));

        Path zip = tempDir.resolve("ch.so.agi.demo.shp.zip");
        assertTrue(Files.exists(zip));
        assertZipEntries(zip, "parcel.dbf", "parcel.shp", "validation.log");
    }

    private static void assertZipEntries(Path zip, String... expectedEntries) throws IOException {
        List<String> entries = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                entries.add(enumeration.nextElement().getName());
            }
        }

        Collections.sort(entries);
        List<String> expected = new ArrayList<>();
        for (String expectedEntry : expectedEntries) {
            expected.add(expectedEntry);
        }
        Collections.sort(expected);

        assertEquals(expected, entries);
    }
}
