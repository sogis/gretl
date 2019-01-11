package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

public class SqlReaderTest {
    private static GretlLogger log;

    static{
        LogEnvironment.initStandalone();
        log = LogEnvironment.getLogger(SqlReaderTest.class);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void lineCommentsCleanRemoved() throws Exception {

        String lineComment = "--This is a singleline comment.";
        String statement = "select \"user\", \"admin\", \"alias\" from sqlkeywords";

        String wholeStatement = lineComment + "\n" + statement;

        File sqlFile = TestUtil.createFile(folder, wholeStatement, "statementIsUnchanged.sql");

        String parsedStatement = new SqlReader().readSqlStmt(sqlFile);

        Assert.assertEquals("Line comment must be removed without changing the statement itself", statement, parsedStatement);
    }    
}
