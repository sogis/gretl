/**
 * Writes publication metadata into the database-backed publication tables and
 * keeps only a bounded number of historical publication roots.
 *
 * <p>The package is split into a filename normalizer, a write coordinator, a
 * retention groomer, and a low-level repository boundary.</p>
 */
package ch.so.agi.gretl.steps.publisher.out.metainfo.table;
