/**
 * Copies specified database tables from a source schema into existing target schemas and
 * tables using catalog reads and SQL DML statements.
 * The table names in source and target schema must be equal. Each source table must contain
 * all columns of the target table.
 */
package ch.so.agi.gretl.steps.publisher.in.db.schemacopy;
