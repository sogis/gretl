package ch.so.agi.gretl.steps.publisher.in.xtf.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class XtfByRegexParamsTest {
    @Test
    void storesTrimmedRegex() {
        XtfByRegexParams params = XtfByRegexParams.of(Path.of("source"), Path.of("target"), "  .*\\.xtf$  ");

        assertEquals(Path.of("source"), params.getSourceDir());
        assertEquals(Path.of("target"), params.getTargetDir());
        assertEquals(".*\\.xtf$", params.getFileNameRegex());
    }

    @Test
    void rejectsBlankRegex() {
        assertThrows(IllegalArgumentException.class,
                () -> XtfByRegexParams.of(Path.of("source"), Path.of("target"), "   "));
    }

    @Test
    void rejectsInvalidRegex() {
        assertThrows(IllegalArgumentException.class,
                () -> XtfByRegexParams.of(Path.of("source"), Path.of("target"), "["));
    }
}
