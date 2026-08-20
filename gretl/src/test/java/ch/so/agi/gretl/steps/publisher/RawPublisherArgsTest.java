package ch.so.agi.gretl.steps.publisher;

import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.list;
import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.publisherArgs;
import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.target;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;

class RawPublisherArgsTest {
    @Test
    void rejectsMissingOutFormats() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RawPublisherArgs.builder().output(target(), "ch.so.agi.demo")
                        .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                        .outFormats(List.of())
                        .build());

        assertContains(exception, "outFormats");
    }

    @Test
    void acceptsDbSourceWithExplicitValues() {
        RawPublisherArgs args = publisherArgs()
                .dbValuesSource("edit", "live", RawPublisherArgs.IliIdentType.dataset, true,
                        list("ch.so.agi.alpha", "ch.so.agi.beta"))
                .build();

        assertEquals("edit", args.getDbDatabase());
        assertEquals("live", args.getDbSchema());
        assertEquals(RawPublisherArgs.IliIdentType.dataset, args.getDbIliIdentType());
        assertEquals(List.of("ch.so.agi.alpha", "ch.so.agi.beta"), args.getDbIliIdent_Values());
        assertEquals(RawPublisherArgs.PublishMode.dbIdentvaluesList, args.getPublishMode());
        assertEquals(Boolean.TRUE, args.getDbMergeToSingleXtf());
    }

    @Test
    void acceptsDbSourceWithRegex() {
        RawPublisherArgs args = publisherArgs()
                .dbRegexSource("edit", "live", RawPublisherArgs.IliIdentType.topic, false, "ch\\.so\\.agi\\..*")
                .build();

        assertEquals("ch\\.so\\.agi\\..*", args.getDbIliIdent_RegEx());
        assertEquals(RawPublisherArgs.IliIdentType.topic, args.getDbIliIdentType());
        assertEquals(RawPublisherArgs.PublishMode.dbIdentvaluesRegex, args.getPublishMode());
        assertEquals(Boolean.FALSE, args.getDbMergeToSingleXtf());
    }

    @Test
    void acceptsXtfSourceWithFilenameList() {
        RawPublisherArgs args = publisherArgs()
                .xtfListSource("/data/incoming", list("2401.xtf", "2402.xtf"))
                .build();

        assertEquals("/data/incoming", args.getXtfFile_FolderPath());
        assertEquals(List.of("2401.xtf", "2402.xtf"), args.getXtfFilename_List());
        assertEquals(RawPublisherArgs.PublishMode.xtfFilesList, args.getPublishMode());
    }

    @Test
    void acceptsXtfSourceWithRegex() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .build();

        assertEquals(".*\\.xtf$", args.getXtfFilename_Regex());
        assertEquals(RawPublisherArgs.PublishMode.xtfFilesRegex, args.getPublishMode());
    }

    @Test
    void storesDistinctExplicitOutputFormatsWithoutAddingTransferFiles() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .outFormats(List.of(OutputFormat.GPKG, OutputFormat.SHP, OutputFormat.GPKG))
                .build();

        assertEquals(List.of(OutputFormat.GPKG, OutputFormat.SHP), args.getOutFormats());
    }

    @Test
    void defaultsToWritingMetadata() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .build();

        assertEquals(Boolean.TRUE, args.getOutWriteMetadata());
        assertNull(args.getOutGroomingConfigFilePath());
        assertNull(args.getOutValidationConfigFilePath());
    }

    @Test
    void storesCustomGroomingConfigFilePathAsNormalizedString() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .groomingConfig("  grooming.json  ")
                .build();

        assertEquals("grooming.json", args.getOutGroomingConfigFilePath());
    }

    @Test
    void storesValidationConfigFilePathAsNormalizedString() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .validationConfig("  validation.ini  ")
                .build();

        assertEquals("validation.ini", args.getOutValidationConfigFilePath());
    }

    @Test
    void storesCustomModelDirAsNormalizedString() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .customModelDir("  /custom/models  ")
                .build();

        assertEquals("/custom/models", args.getCustomModelDir());
    }

    @Test
    void storesBlankCustomModelDirAsNull() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .customModelDir("   ")
                .build();

        assertNull(args.getCustomModelDir());
    }

    @Test
    void rejectsMissingOutputArguments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RawPublisherArgs.builder()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .build());

        assertContains(exception, "outDataIdent");
    }

    @Test
    void requiresFolderPathWhenMetadataWritingIsDisabled() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RawPublisherArgs.builder().output(null, "ch.so.agi.demo")
                        .writeMetadata(false)
                        .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                        .outFormats(List.of(OutputFormat.XTF))
                        .build());

        assertContains(exception, "outFolderPath when outWriteMetadata is false");
    }

    @Test
    void rejectsDbSourceMissingMandatoryArgs() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> publisherArgs()
                .dbValuesSource(null, null, null, null, list("ch.so.agi.alpha"))
                .build());

        assertContains(exception, "dbDatabase");
        assertContains(exception, "dbSchema");
        assertContains(exception, "dbMergeToSingleXtf");
        assertContains(exception, "dbIliIdent_Type");
    }

    @Test
    void rejectsDbSourceWithoutRegexOrValueList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> publisherArgs()
                .dbRegexSource("edit", "live", RawPublisherArgs.IliIdentType.dataset, false, null)
                .build());

        assertContains(exception, "Either dbIliIdent_RegEx or dbIliIdent_Values must be set");
    }

    @Test
    void rejectsXtfSourceWithoutRegexOrFilenameList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> publisherArgs()
                .xtfRegexSource("/data/incoming", null)
                .build());

        assertContains(exception, "Either xtfFilename_Regex or xtfFilename_List must be set");
    }

    private static void assertContains(Exception exception, String expected) {
        assertTrue(exception.getMessage().contains(expected), exception.getMessage());
    }

}
