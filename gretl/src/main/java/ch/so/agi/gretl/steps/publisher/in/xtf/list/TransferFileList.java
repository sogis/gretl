package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable list of transfer-file basenames.
 *
 * <p>Entries must not contain path separators or the transfer-file suffix.
 * The original order is preserved.</p>
 */
public final class TransferFileList implements Iterable<String> {
    private final List<String> fileNames;

    private TransferFileList(List<String> fileNames) {
        this.fileNames = validate(fileNames);
    }

    public static TransferFileList of(List<String> fileNames) {
        return new TransferFileList(fileNames);
    }

    public static TransferFileList of(String... fileNames) {
        Objects.requireNonNull(fileNames, "fileNames must not be null");
        List<String> values = new ArrayList<String>(fileNames.length);
        Collections.addAll(values, fileNames);
        return new TransferFileList(values);
    }

    public List<String> asList() {
        return fileNames;
    }

    public int size() {
        return fileNames.size();
    }

    public String get(int index) {
        return fileNames.get(index);
    }

    @Override
    public Iterator<String> iterator() {
        return fileNames.iterator();
    }

    private static List<String> validate(List<String> values) {
        Objects.requireNonNull(values, "fileNames must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("fileNames must not be empty");
        }

        List<String> copy = new ArrayList<String>(values.size());
        Set<String> seen = new LinkedHashSet<String>();
        for (String value : values) {
            String normalized = requireBasename(value);
            if (!seen.add(normalized)) {
                throw new IllegalArgumentException("fileNames must not contain duplicates");
            }
            copy.add(normalized);
        }
        return Collections.unmodifiableList(copy);
    }

    private static String requireBasename(String value) {
        if (value == null) {
            throw new IllegalArgumentException("fileNames must not contain null values");
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("fileNames must not contain blank values");
        }
        if (trimmed.contains("/") || trimmed.contains("\\")) {
            throw new IllegalArgumentException("fileNames must not contain path separators");
        }
        String lower = trimmed.toLowerCase();
        if (lower.endsWith(".xtf") || lower.endsWith(".itf")) {
            throw new IllegalArgumentException("fileNames must not contain transfer-file suffixes");
        }
        return trimmed;
    }
}
