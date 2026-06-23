package ch.so.agi.gretl.steps.schemacopy;

public class TableCopySpecification {
    private final String tableName;
    private final String rowFilter;

    private TableCopySpecification(Builder builder) {
        this.tableName = requireText(builder.tableName, "tableName");
        this.rowFilter = trimToNull(builder.rowFilter);
    }

    public static Builder builder(String tableName) {
        return new Builder(tableName);
    }

    public String getTableName() {
        return tableName;
    }

    public String getRowFilter() {
        return rowFilter;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    public static final class Builder {
        private final String tableName;
        private String rowFilter;

        private Builder(String tableName) {
            this.tableName = tableName;
        }

        public Builder rowFilter(String rowFilter) {
            this.rowFilter = rowFilter;
            return this;
        }

        public TableCopySpecification build() {
            return new TableCopySpecification(this);
        }
    }
}
