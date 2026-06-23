package ch.so.agi.gretl.steps.publisher.in.xtf.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class XtfByRegexParamsTest {
    @Test
    void storesTrimmedRegex() {
        XtfByRegexParams params = XtfByRegexParams.of("  .*\\.xtf$  ");

        assertEquals(".*\\.xtf$", params.getFileNameRegex());
    }

    @Test
    void rejectsBlankRegex() {
        assertThrows(IllegalArgumentException.class, () -> XtfByRegexParams.of("   "));
    }

    @Test
    void rejectsInvalidRegex() {
        assertThrows(IllegalArgumentException.class, () -> XtfByRegexParams.of("["));
    }
}
