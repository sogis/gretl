package ch.so.agi.gretl.steps.publisher;

import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.list;
import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.publisherArgs;
import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.target;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivedFormat;

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
    void builderMatchesHugeConstructorForDbValuesSource() {
        Date depVersion = new Date(1000L);
        RawPublisherArgs built = publisherArgs()
                .dbValuesSource("edit", "live", RawPublisherArgs.IliIdentType.dataset, true,
                        list("ch.so.agi.alpha", "ch.so.agi.beta"))
                .depVersion(depVersion)
                .build();
        RawPublisherArgs constructed = new RawPublisherArgs(
                "edit",
                "live",
                "dataset",
                list("ch.so.agi.alpha", "ch.so.agi.beta"),
                null,
                true,
                null,
                null,
                null,
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null,
                null,
                null,
                null,
                depVersion);

        assertSameState(constructed, built);
    }

    @Test
    void builderMatchesHugeConstructorForDbRegexSource() {
        Date depVersion = new Date(1000L);
        RawPublisherArgs built = publisherArgs()
                .dbRegexSource("edit", "live", RawPublisherArgs.IliIdentType.topic, false, "ch\\.so\\.agi\\..*")
                .depVersion(depVersion)
                .build();
        RawPublisherArgs constructed = new RawPublisherArgs(
                "edit",
                "live",
                "topic",
                null,
                "ch\\.so\\.agi\\..*",
                false,
                null,
                null,
                null,
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null,
                null,
                null,
                null,
                depVersion);

        assertSameState(constructed, built);
    }

    @Test
    void builderMatchesHugeConstructorForXtfListSource() {
        Date depVersion = new Date(1000L);
        RawPublisherArgs built = publisherArgs()
                .xtfListSource("/data/incoming", list("2401.xtf", "2402.xtf"))
                .depVersion(depVersion)
                .build();
        RawPublisherArgs constructed = new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                "/data/incoming",
                null,
                list("2401.xtf", "2402.xtf"),
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null,
                null,
                null,
                null,
                depVersion);

        assertSameState(constructed, built);
    }

    @Test
    void builderMatchesHugeConstructorForXtfRegexSourceWithOptionalOutputs() {
        Date depVersion = new Date(1000L);
        RawPublisherArgs built = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .isolatedMode(true)
                .localFolderOnly("local/folder")
                .groomingConfig("grooming.json")
                .validationConfig("validation.ini")
                .derivedFormats(List.of(DerivedFormat.GPKG))
                .depVersion(depVersion)
                .build();
        RawPublisherArgs constructed = new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                "/data/incoming",
                ".*\\.xtf$",
                null,
                target(),
                "ch.so.agi.demo",
                true,
                "local/folder",
                "grooming.json",
                "validation.ini",
                null,
                List.of(DerivedFormat.GPKG),
                depVersion);

        assertSameState(constructed, built);
    }

    @Test
    void storesDistinctDerivedFormats() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .derivedFormats(List.of(DerivedFormat.GPKG, DerivedFormat.SHP, DerivedFormat.GPKG))
                .build();

        assertEquals(List.of(DerivedFormat.GPKG, DerivedFormat.SHP), args.getOutDerivedFormats());
    }

    @Test
    void storesNewOutputFieldsAndNormalizesBlankFolderOverride() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .isolatedMode(true)
                .localFolderOnly("   ")
                .build();

        assertEquals(Boolean.TRUE, args.getOutIsolatedMode());
        assertNull(args.getOutWriteToThisLocalFolderOnly());
        assertNull(args.getOutCustomGroomingConfFilePath());
        assertNull(args.getOutValidationConfigFilePath());
    }

    @Test
    void storesCustomGroomingConfigFilePathAsNormalizedString() {
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .groomingConfig("  grooming.json  ")
                .build();

        assertEquals("grooming.json", args.getOutCustomGroomingConfFilePath());
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
    void defaultsAndDefensivelyCopiesDate() {
        Date version = new Date(1000L);
        RawPublisherArgs args = publisherArgs()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .validationConfig("validation.ini")
                .depVersion(version)
                .build();

        version.setTime(2000L);
        Date returned = args.getDepVersion();
        returned.setTime(3000L);

        assertEquals(1000L, args.getDepVersion().getTime());
    }

    @Test
    void rejectsMissingOutputArguments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RawPublisherArgs.builder()
                .xtfRegexSource("/data/incoming", ".*\\.xtf$")
                .build());

        assertContains(exception, "outBasePath");
        assertContains(exception, "outDataIdent");
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
    void rejectsDbSourceWithRegexAndValueList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RawPublisherArgs(
                "edit",
                "live",
                "dataset",
                list("ch.so.agi.alpha"),
                "ch\\.so\\.agi\\..*",
                false,
                null,
                null,
                null,
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null,
                null,
                null,
                null,
                null));

        assertContains(exception, "Setting both dbIliIdent_RegEx and dbIliIdent_Values is invalid");
    }

    @Test
    void rejectsInvalidIliIdentifierType() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RawPublisherArgs(
                "edit",
                "live",
                "invalid",
                list("ch.so.agi.alpha"),
                null,
                false,
                null,
                null,
                null,
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null,
                null,
                null,
                null,
                null));

        assertContains(exception, "dbIliIdent_Type must be one of model, topic, basket, dataset");
    }

    @Test
    void rejectsDbModeWithXtfArguments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RawPublisherArgs(
                "edit",
                "live",
                "dataset",
                list("ch.so.agi.alpha"),
                null,
                false,
                null,
                ".*\\.xtf$",
                null,
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null,
                null,
                null,
                null,
                null));

        assertContains(exception, "Publisher is in db mode. These xtf arguments must be null");
        assertContains(exception, "xtfFilename_Regex");
    }

    @Test
    void rejectsXtfSourceWithoutRegexOrFilenameList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> publisherArgs()
                .xtfRegexSource("/data/incoming", null)
                .build());

        assertContains(exception, "Either xtfFilename_Regex or xtfFilename_List must be set");
    }

    @Test
    void rejectsXtfSourceWithRegexAndFilenameList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                "/data/incoming",
                ".*\\.xtf$",
                list("2401.xtf"),
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null,
                null,
                null,
                null,
                null));

        assertContains(exception, "Setting both xtfFilename_Regex and xtfFilename_List is invalid");
    }

    @Test
    void rejectsXtfModeWithDbArguments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RawPublisherArgs(
                "edit",
                "live",
                "dataset",
                list("ch.so.agi.alpha"),
                null,
                false,
                "/data/incoming",
                ".*\\.xtf$",
                null,
                target(),
                "ch.so.agi.demo",
                null,
                null,
                null,
                null,
                null,
                null,
                null));

        assertContains(exception, "Publisher is in file mode. These db arguments must be null");
        assertContains(exception, "dbMergeToSingleXtf");
        assertContains(exception, "dbDatabase");
        assertContains(exception, "dbSchema");
        assertContains(exception, "dbIliIdent_Type");
        assertContains(exception, "dbIliIdent_Values");
    }

    private static void assertContains(Exception exception, String expected) {
        assertTrue(exception.getMessage().contains(expected), exception.getMessage());
    }

    private static void assertSameState(RawPublisherArgs expected, RawPublisherArgs actual) {
        assertEquals(expected.getDbDatabase(), actual.getDbDatabase());
        assertEquals(expected.getDbSchema(), actual.getDbSchema());
        assertEquals(expected.getDbIliIdent_Type(), actual.getDbIliIdent_Type());
        assertEquals(expected.getDbIliIdent_Values(), actual.getDbIliIdent_Values());
        assertEquals(expected.getDbIliIdent_RegEx(), actual.getDbIliIdent_RegEx());
        assertEquals(expected.getDbMergeToSingleXtf(), actual.getDbMergeToSingleXtf());
        assertEquals(expected.getDbIliIdentType(), actual.getDbIliIdentType());
        assertEquals(expected.getXtfFile_FolderPath(), actual.getXtfFile_FolderPath());
        assertEquals(expected.getXtfFilename_Regex(), actual.getXtfFilename_Regex());
        assertEquals(expected.getXtfFilename_List(), actual.getXtfFilename_List());
        assertEquals(expected.getOutBasePath(), actual.getOutBasePath());
        assertEquals(expected.getOutDataIdent(), actual.getOutDataIdent());
        assertEquals(expected.getOutIsolatedMode(), actual.getOutIsolatedMode());
        assertEquals(expected.getOutWriteToThisLocalFolderOnly(), actual.getOutWriteToThisLocalFolderOnly());
        assertEquals(expected.getOutCustomGroomingConfFilePath(), actual.getOutCustomGroomingConfFilePath());
        assertEquals(expected.getOutValidationConfigFilePath(), actual.getOutValidationConfigFilePath());
        assertEquals(expected.getCustomModelDir(), actual.getCustomModelDir());
        assertEquals(expected.getOutDerivedFormats(), actual.getOutDerivedFormats());
        assertEquals(expected.getDepVersion().getTime(), actual.getDepVersion().getTime());
        assertEquals(expected.getPublishMode(), actual.getPublishMode());
    }
}
