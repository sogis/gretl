/**
 * Copies specified database tables from a source schema into existing target schemas and
 * tables using catalog reads and SQL DML statements.
 * The table names in source and target schema must be equal. Each source table must contain
 * all columns of the target table.
 * If at least one copied data table pair in source and target contains ili2db basket references, the copy
 * also replaces target ili2db dataset and basket metadata rows from the source schema.
 */
package ch.so.agi.gretl.steps.schemacopy;
