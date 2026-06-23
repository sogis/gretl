/**
 * Copies specified database tables from a source schema into existing target schemas and
 * tables using catalog reads and SQL DML statements.
 * The table names in source and target schema must be equal. Each source table must contain
 * all columns of the target table.
 * If all copied data tables in source and target contain ili2db basket references, the copy
 * also replaces target ili2db dataset and basket metadata rows from the source schema.
 */
package ch.so.agi.gretl.steps.schemacopy;
