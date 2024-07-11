package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlReaderTest {
    private static GretlLogger log;

    static {
        LogEnvironment.initStandalone();
        log = LogEnvironment.getLogger(SqlReaderTest.class);
    }

    @TempDir
    public Path folder;

    @Test
    public void lineCommentsCleanRemoved() throws Exception {
        String lineComment = "--This is a singleline comment.";
        String statement = "select \"user\", \"admin\", \"alias\" from sqlkeywords";

        String wholeStatement = lineComment + "\n" + statement;

        File sqlFile = TestUtil.createTempFile(folder, wholeStatement, "statementIsUnchanged.sql");

        String parsedStatement = new SqlReader().readSqlStmt(sqlFile);

        assertEquals("Line comment must be removed without changing the statement itself", statement,
                parsedStatement);
    }
}
