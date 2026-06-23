package ch.so.agi.gretl.steps.schemacopy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Request {
    private final String sourceSchema;
    private final List<String> targetSchemas;
    private final List<TableCopySpecification> sourceTables;

    private Request(Builder builder) {
        this.sourceSchema = requireText(builder.sourceSchema, "sourceSchema");
        this.targetSchemas = immutableTextList(builder.targetSchemas, "targetSchemas");
        this.sourceTables = immutableTables(builder.tables);
    }

    public static Builder builder(String sourceSchema) {
        return new Builder(sourceSchema);
    }

    public String getSourceSchema() {
        return sourceSchema;
    }

    public List<String> getTargetSchemas() {
        return targetSchemas;
    }

    public List<TableCopySpecification> getSourceTables() {
        return sourceTables;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return value.trim();
    }

    private static List<String> immutableTextList(List<String> values, String fieldName) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }

        List<String> copy = new ArrayList<>();
        for (String value : values) {
            copy.add(requireText(value, fieldName));
        }
        return Collections.unmodifiableList(copy);
    }

    private static List<TableCopySpecification> immutableTables(List<TableCopySpecification> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("tables must not be empty");
        }

        List<TableCopySpecification> copy = new ArrayList<>();
        for (TableCopySpecification value : values) {
            if (value == null) {
                throw new IllegalArgumentException("tables must not contain null values");
            }
            copy.add(value);
        }
        return Collections.unmodifiableList(copy);
    }

    public static final class Builder {
        private final String sourceSchema;
        private final List<String> targetSchemas = new ArrayList<>();
        private final List<TableCopySpecification> tables = new ArrayList<>();

        private Builder(String sourceSchema) {
            this.sourceSchema = sourceSchema;
        }

        public Builder targetSchema(String targetSchema) {
            this.targetSchemas.add(targetSchema);
            return this;
        }

        public Builder targetSchemas(List<String> targetSchemas) {
            this.targetSchemas.addAll(targetSchemas);
            return this;
        }

        public Builder table(TableCopySpecification table) {
            this.tables.add(table);
            return this;
        }

        public Builder tables(List<TableCopySpecification> tables) {
            this.tables.addAll(tables);
            return this;
        }

        public Request build() {
            return new Request(this);
        }
    }
}
