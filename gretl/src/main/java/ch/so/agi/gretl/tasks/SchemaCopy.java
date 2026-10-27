package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.schemacopy.CopyOperation;
import ch.so.agi.gretl.steps.schemacopy.Request;
import ch.so.agi.gretl.steps.schemacopy.TableCopySpecification;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Copies selected tables from one schema into identically named tables in one or more target schemas.
 * Task is not usable for copying data tables referencing each other with foreign keys.
 */
public class SchemaCopy extends DefaultTask {
    private static final GretlLogger log = LogEnvironment.getLogger(SchemaCopy.class);

    private Connector database;
    private String sourceSchema;
    private String targetSchema;
    private List<String> targetSchemas;
    private List<String> tables;
    private List<String> tablesWithFilter;

    /** Datenbank mit dem Quell- und den Ziel-Schemas. */
    @Input
    public Connector getDatabase() {
        return database;
    }

    /** Name des Quell-Schemas. */
    @Input
    public String getSourceSchema() {
        return sourceSchema;
    }

    /** Name eines einzelnen Ziel-Schemas. Bei mehreren Zielschemas {@link #getTargetSchemas()} nutzen. */
    @Input
    @Optional
    public String getTargetSchema() {
        return targetSchema;
    }

    /** Namen der Ziel-Schemas. Bei einem Zielschema ist es einfacher {@link #getTargetSchema()} zu nutzen. */
    @Input
    @Optional
    public List<String> getTargetSchemas() {
        return targetSchemas;
    }

    /** Namen der ungefiltert zu kopierenden Tabellen. */
    @Input
    @Optional
    public List<String> getTables() {
        return tables;
    }

    /**
     * Tabellenname und Whereclause für gefiltertes Befüllen der Zieltabelle. 
     * Liste nach folgendem Muster paarweise befüllen: 
     * Tabelle1 Name, Tabelle1 Filter, Tabelle2 Name, Tabelle2 Filter, ...
     * Die Zeilenfilter dürfen {@code WHERE} nicht enthalten.
     */
    @Input
    @Optional
    public List<String> getTablesWithFilter() {
        return tablesWithFilter;
    }

    public void setDatabase(List<String> databaseDetails) {
        this.database = TaskUtil.getDatabaseConnectorObject(databaseDetails);
    }

    public void setSourceSchema(String sourceSchema) {
        this.sourceSchema = sourceSchema;
    }

    public void setTargetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
    }

    public void setTargetSchemas(List<String> targetSchemas) {
        this.targetSchemas = targetSchemas;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public void setTablesWithFilter(List<String> tablesWithFilter) {
        this.tablesWithFilter = tablesWithFilter;
    }

    @TaskAction
    public void copy() {
        Request request = createRequest();
        try {
            int copiedRows = new CopyOperation(database).execute(request);
            log.lifecycle(String.format("Schema copy completed: %d data rows copied", copiedRows));
        } catch (Exception e) {
            log.error("Exception in SchemaCopy task.", e);
            throw TaskUtil.toGradleException(e);
        }
    }

    Request createRequest() {
        requireDatabase();
        String validatedSourceSchema = requireText(sourceSchema, "sourceSchema");
        List<String> targets = resolveTargetSchemas();
        List<TableCopySpecification> specifications = createTableSpecifications();

        Request.Builder builder = Request.builder(validatedSourceSchema).targetSchemas(targets);
        for (TableCopySpecification specification : specifications) {
            builder.table(specification);
        }
        return builder.build();
    }

    private List<String> resolveTargetSchemas() {
        boolean hasTargetSchema = hasText(targetSchema);
        boolean hasTargetSchemas = targetSchemas != null && !targetSchemas.isEmpty();
        if (hasTargetSchema == hasTargetSchemas) {
            throw new IllegalArgumentException("configure exactly one of targetSchema or targetSchemas");
        }

        List<String> resolved = new ArrayList<>();
        if (hasTargetSchema) {
            resolved.add(requireText(targetSchema, "targetSchema"));
        } else {
            for (String schema : targetSchemas) {
                resolved.add(requireText(schema, "targetSchemas"));
            }
        }
        return resolved;
    }

    private List<TableCopySpecification> createTableSpecifications() {
        List<TableCopySpecification> specifications = new ArrayList<>();
        Set<String> seenTableNames = new HashSet<>();

        if (tables != null) {
            for (String table : tables) {
                String tableName = requireText(table, "tables");
                addTable(specifications, seenTableNames, tableName, null);
            }
        }

        if (tablesWithFilter != null) {
            if (tablesWithFilter.size() % 2 != 0) {
                throw new IllegalArgumentException("tablesWithFilter must contain an even number of values");
            }
            for (int index = 0; index < tablesWithFilter.size(); index += 2) {
                String tableName = requireText(tablesWithFilter.get(index), "tablesWithFilter table name");
                String rowFilter = requireText(tablesWithFilter.get(index + 1), "tablesWithFilter row filter");
                assertValidRowFilter(rowFilter);
                addTable(specifications, seenTableNames, tableName, rowFilter);
            }
        }

        if (specifications.isEmpty()) {
            throw new IllegalArgumentException("configure at least one table in tables or tablesWithFilter");
        }
        return specifications;
    }

    private static void addTable(
            List<TableCopySpecification> specifications,
            Set<String> seenTableNames,
            String tableName,
            String rowFilter
    ) {
        String normalizedTableName = tableName.toLowerCase(Locale.ROOT);
        if (!seenTableNames.add(normalizedTableName)) {
            throw new IllegalArgumentException("table <" + tableName + "> is configured more than once");
        }

        TableCopySpecification.Builder builder = TableCopySpecification.builder(tableName);
        if (rowFilter != null) {
            builder.rowFilter(rowFilter);
        }
        specifications.add(builder.build());
    }

    private static void assertValidRowFilter(String rowFilter) {
        String normalizedFilter = rowFilter.toLowerCase(Locale.ROOT);
        if (normalizedFilter.contains("where")) {
            throw new IllegalArgumentException("tablesWithFilter row filter must not contain the keyword 'where'");
        }
        if (normalizedFilter.contains(";")) {
            throw new IllegalArgumentException("tablesWithFilter row filter must not contain the statement delimiter ';'");
        }
    }

    private void requireDatabase() {
        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
