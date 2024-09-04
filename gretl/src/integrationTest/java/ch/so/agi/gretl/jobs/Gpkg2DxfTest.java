package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class Gpkg2DxfTest {
    private final GretlLogger log;

    public Gpkg2DxfTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void export_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Gpkg2Dxf");

        String testOutDir = projectDirectory + "/out/";
        String testFilePath = projectDirectory + "/ch.so.agi.av_gb_admin_einteilung_edit_2020-08-20.gpkg";
        String[] dxfFiles = {
                "nachfuehrngskrise_gemeinde.dxf",
                "grundbuchkreise_grundbuchkreis.dxf"
        };

        // Clean up existing files
        Files.deleteIfExists(Paths.get(testFilePath));
        try (Stream<Path> paths = Files.list(Paths.get(testOutDir))) {
            paths.filter(p -> p.toString().endsWith(".dxf"))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    });
        }

        IntegrationTestUtil.executeTestRunner(projectDirectory, "gpkg2dxf");

        // Check results
        for (String dxfFile : dxfFiles) {
            Path filePath = Paths.get(testOutDir, dxfFile);
            assertTrue(Files.exists(filePath), "Expected file not found: " + dxfFile);

            String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            switch (dxfFile) {
                case "nachfuehrngskrise_gemeinde.dxf":
                    assertTrue(content.contains("LerchWeberAG"));
                    assertTrue(content.contains("2638171.578"));
                    break;
                case "grundbuchkreise_grundbuchkreis.dxf":
                    assertTrue(content.contains("2619682.201"));
                    break;
                default:
                    fail("Unexpected file: " + dxfFile);
            }
        }
    }
}
