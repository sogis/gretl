package ch.so.agi.gretl.steps.publisher.in.xtf.regex;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Parameters for selecting transfer files by regular expression.
 */
public final class XtfByRegexParams {
    private final String fileNameRegex;
    private final Pattern fileNamePattern;

    private XtfByRegexParams(String fileNameRegex) {
        this.fileNameRegex = requireText(fileNameRegex, "fileNameRegex");
        try {
            this.fileNamePattern = Pattern.compile(this.fileNameRegex);
        } catch (PatternSyntaxException ex) {
            throw new IllegalArgumentException("fileNameRegex must be a valid regular expression", ex);
        }
    }

    public static XtfByRegexParams of(String fileNameRegex) {
        return new XtfByRegexParams(fileNameRegex);
    }

    public String getFileNameRegex() {
        return fileNameRegex;
    }

    Pattern getFileNamePattern() {
        return fileNamePattern;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
