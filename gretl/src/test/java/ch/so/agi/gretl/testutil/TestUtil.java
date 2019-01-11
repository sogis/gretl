package ch.so.agi.gretl.testutil;

import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestUtil {
    public static final String PG_CONNECTION_URI = System.getProperty("gretltest_dburi_pg");
    public static final String PG_DDLUSR_USR = "ddluser";
    public static final String PG_DDLUSR_PWD = "ddluser";
    public static final String PG_DMLUSR_USR ="dmluser";
    public static final String PG_DMLUSR_PWD ="dmluser";
    public static final String PG_READERUSR_USR ="readeruser";
    public static final String PG_READERUSR_PWD ="readeruser";

    public static File createFile(TemporaryFolder folder, String stm, String fileName) throws IOException {
        File sqlFile =  folder.newFile(fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(stm);
        writer.close();

        return sqlFile;
    }
}
