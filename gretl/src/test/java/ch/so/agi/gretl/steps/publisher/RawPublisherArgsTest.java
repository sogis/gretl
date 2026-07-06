package ch.so.agi.gretl.steps.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RawPublisherArgsTest {
    @Test
    void acceptsDbSourceWithExplicitValues() {
        RawPublisherArgs args = new RawPublisherArgs(
                "edit",
                "live",
                "dataset",
                list("ch.so.agi.alpha", "ch.so.agi.beta"),
                null,
                true,
                null,
                null,
                null);

        assertEquals("edit", args.getDbDatabase());
        assertEquals("live", args.getDbSchema());
        assertEquals(RawPublisherArgs.IliIdentType.dataset, args.getDbIliIdentType());
        assertEquals(list("ch.so.agi.alpha", "ch.so.agi.beta"), args.getDbIliIdent_Values());
        assertEquals(RawPublisherArgs.PublishMode.dbIdentvaluesList, args.getPublishMode());
        assertEquals(Boolean.TRUE, args.getDbMergeToSingleXtf());
    }

    @Test
    void acceptsDbSourceWithRegex() {
        RawPublisherArgs args = new RawPublisherArgs(
                "edit",
                "live",
                "topic",
                null,
                "ch\\.so\\.agi\\..*",
                false,
                null,
                null,
                null);

        assertEquals("ch\\.so\\.agi\\..*", args.getDbIliIdent_RegEx());
        assertEquals(RawPublisherArgs.IliIdentType.topic, args.getDbIliIdentType());
        assertEquals(RawPublisherArgs.PublishMode.dbIdentvaluesRegex, args.getPublishMode());
        assertEquals(Boolean.FALSE, args.getDbMergeToSingleXtf());
    }

    @Test
    void acceptsXtfSourceWithFilenameList() {
        RawPublisherArgs args = new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                "/data/incoming",
                null,
                list("2401.xtf", "2402.xtf"));

        assertEquals("/data/incoming", args.getXtfFile_FolderPath());
        assertEquals(list("2401.xtf", "2402.xtf"), args.getXtfFilename_List());
        assertEquals(RawPublisherArgs.PublishMode.xtfFilesList, args.getPublishMode());
    }

    @Test
    void acceptsXtfSourceWithRegex() {
        RawPublisherArgs args = new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                "/data/incoming",
                ".*\\.xtf$",
                null);

        assertEquals(".*\\.xtf$", args.getXtfFilename_Regex());
        assertEquals(RawPublisherArgs.PublishMode.xtfFilesRegex, args.getPublishMode());
    }

    @Test
    void rejectsDbSourceMissingMandatoryArgs() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RawPublisherArgs(
                null,
                null,
                null,
                list("ch.so.agi.alpha"),
                null,
                null,
                null,
                null,
                null));

        assertContains(exception, "Missing mandatory arguments:");
        assertContains(exception, "dbDatabase");
        assertContains(exception, "dbSchema");
        assertContains(exception, "dbMergeToSingleXtf");
        assertContains(exception, "dbIliIdent_Type");
    }

    @Test
    void rejectsDbSourceWithoutRegexOrValueList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RawPublisherArgs(
                "edit",
                "live",
                "dataset",
                null,
                null,
                false,
                null,
                null,
                null));

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
                null));

        assertContains(exception, "Publisher is in db mode. These xtf arguments must be null");
        assertContains(exception, "xtfFilename_Regex");
    }

    @Test
    void rejectsXtfSourceWithoutRegexOrFilenameList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RawPublisherArgs(
                null,
                null,
                null,
                null,
                null,
                null,
                "/data/incoming",
                null,
                null));

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
                list("2401.xtf")));

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
                null));

        assertContains(exception, "Publisher is in file mode. These db arguments must be null");
        assertContains(exception, "dbMergeToSingleXtf");
        assertContains(exception, "dbDatabase");
        assertContains(exception, "dbSchema");
        assertContains(exception, "dbIliIdent_Type");
        assertContains(exception, "dbIliIdent_Values");
    }

    private static ArrayList<String> list(String... values) {
        return new ArrayList<>(List.of(values));
    }

    private static void assertContains(Exception exception, String expected) {
        assertTrue(exception.getMessage().contains(expected), exception.getMessage());
    }
}
