package ch.so.agi.gretl.steps.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.Endpoint;

class PublisherStepParametersTest {
    @Test
    void acceptsDbSourceWithExplicitValues() {
        PublisherStepParameters parameters = baseBuilder()
                .dbDatabase(database())
                .dbSchema("live")
                .dbIliIdentType("dataset")
                .dbIliIdentValues(List.of("ch.so.agi.alpha", "ch.so.agi.beta"))
                .dbMergeToSingleXtf(true)
                .build();

        assertEquals("live", parameters.getDbSchema());
        assertEquals("dataset", parameters.getDbIliIdentType());
        assertEquals(List.of("ch.so.agi.alpha", "ch.so.agi.beta"), parameters.getDbIliIdentValues());
        assertTrue(parameters.isDbMergeToSingleXtf());
    }

    @Test
    void acceptsDbSourceWithRegex() {
        PublisherStepParameters parameters = baseBuilder()
                .dbDatabase(database())
                .dbSchema("live")
                .dbIliIdentType("dataset")
                .dbIliIdentRegEx("ch\\.so\\.agi\\..*")
                .build();

        assertEquals("ch\\.so\\.agi\\..*", parameters.getDbIliIdentRegEx());
        assertFalse(parameters.isDbMergeToSingleXtf());
    }

    @Test
    void acceptsXtfSingleFile() {
        PublisherStepParameters parameters = baseBuilder()
                .xtfFilePath("data.xtf")
                .build();

        assertEquals("data.xtf", parameters.getXtfFilePath());
        assertTrue(parameters.getXtfFilenameList().isEmpty());
    }

    @Test
    void acceptsXtfRegex() {
        PublisherStepParameters parameters = baseBuilder()
                .xtfFilePath("dummy.xtf")
                .xtfFilenameRegex("[0-9]{4}")
                .build();

        assertEquals("[0-9]{4}", parameters.getXtfFilenameRegex());
    }

    @Test
    void acceptsXtfFilenameList() {
        PublisherStepParameters parameters = baseBuilder()
                .xtfFilePath("dummy.xtf")
                .xtfFilenameList(List.of("2401", "2402"))
                .build();

        assertEquals(List.of("2401", "2402"), parameters.getXtfFilenameList());
    }

    @Test
    void defaultsOptionalValues() {
        PublisherStepParameters parameters = baseBuilder()
                .xtfFilePath("data.xtf")
                .build();

        assertFalse(parameters.isOutWriteUserFormats());
        assertFalse(parameters.isDbMergeToSingleXtf());
        assertNotNull(parameters.getDepVersion());
    }

    @Test
    void defensivelyCopiesDate() {
        Date version = new Date(1000L);

        PublisherStepParameters parameters = baseBuilder()
                .xtfFilePath("data.xtf")
                .depVersion(version)
                .build();
        version.setTime(2000L);
        Date returned = parameters.getDepVersion();
        returned.setTime(3000L);

        assertEquals(1000L, parameters.getDepVersion().getTime());
    }

    @Test
    void rejectsMissingSource() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> baseBuilder().build());

        assertContains(exception, "dbDatabase");
        assertContains(exception, "xtfFilePath");
    }

    @Test
    void rejectsMixedDbAndXtfSources() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .dbDatabase(database())
                .dbSchema("live")
                .dbIliIdentType("dataset")
                .dbIliIdentValues(List.of("ch.so.agi.alpha"))
                .xtfFilePath("data.xtf")
                .build());

        assertContains(exception, "DB and XTF source properties must not be mixed");
        assertContains(exception, "dbDatabase");
        assertContains(exception, "xtfFilePath");
    }

    @Test
    void rejectsMissingOutBasePath() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> PublisherStepParameters.builder()
                .outDataIdent("ch.so.agi.alpha")
                .xtfFilePath("data.xtf")
                .build());

        assertContains(exception, "outBasePath");
    }

    @Test
    void rejectsMissingOutDataIdent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> PublisherStepParameters.builder()
                .outBasePath(target())
                .xtfFilePath("data.xtf")
                .build());

        assertContains(exception, "outDataIdent");
    }

    @Test
    void rejectsDbSourceWithoutSchema() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .dbDatabase(database())
                .dbIliIdentType("dataset")
                .dbIliIdentValues(List.of("ch.so.agi.alpha"))
                .build());

        assertContains(exception, "dbSchema");
    }

    @Test
    void rejectsDbSourceWithoutIliIdentType() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .dbDatabase(database())
                .dbSchema("live")
                .dbIliIdentValues(List.of("ch.so.agi.alpha"))
                .build());

        assertContains(exception, "dbIliIdentType");
    }

    @Test
    void rejectsDbSourceWithBothValuesAndRegex() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .dbDatabase(database())
                .dbSchema("live")
                .dbIliIdentType("dataset")
                .dbIliIdentValues(List.of("ch.so.agi.alpha"))
                .dbIliIdentRegEx("ch\\.so\\.agi\\..*")
                .build());

        assertContains(exception, "dbIliIdentValues");
        assertContains(exception, "dbIliIdentRegEx");
    }

    @Test
    void rejectsXtfSourceWithRegexAndList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .xtfFilePath("dummy.xtf")
                .xtfFilenameRegex("[0-9]{4}")
                .xtfFilenameList(List.of("2401"))
                .build());

        assertContains(exception, "xtfFilenameRegex");
        assertContains(exception, "xtfFilenameList");
    }

    @Test
    void reportsAggregatedErrors() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> PublisherStepParameters.builder()
                .dbDatabase(database())
                .build());

        assertContains(exception, "Invalid Publisher configuration:");
        assertContains(exception, "outBasePath");
        assertContains(exception, "outDataIdent");
        assertContains(exception, "dbSchema");
        assertContains(exception, "dbIliIdentType");
        assertContains(exception, "dbIliIdentValues");
        assertContains(exception, "dbIliIdentRegEx");
    }

    private static PublisherStepParameters.Builder baseBuilder() {
        return PublisherStepParameters.builder()
                .outBasePath(target())
                .outDataIdent("ch.so.agi.alpha");
    }

    private static Connector database() {
        return new Connector("jdbc:postgresql://localhost:5432/postgres", "user", "password");
    }

    private static Endpoint target() {
        return new Endpoint("/tmp/publisher-target");
    }

    private static void assertContains(Exception exception, String expected) {
        assertTrue(exception.getMessage().contains(expected), exception.getMessage());
    }
}
