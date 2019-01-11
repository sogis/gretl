package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Class which reads the given sql-statements
 */
public class SqlReader {
    private PushbackReader reader=null;
    private Map<String,String> params=null;
    private GretlLogger log = LogEnvironment.getLogger(SqlReader.class);


    public String readSqlStmt(File sqlfile)
            throws IOException {
        return readSqlStmt(sqlfile, null);
    }
    public String readSqlStmt(File sqlfile,Map<String,String> params)
            throws IOException {

        if(reader!=null) {
            throw new IllegalStateException("readSqlStmt() must only be called for the first statement");
        }
        this.params=params;
        createPushbackReader(sqlfile);
        String ret = ch.ehi.sqlgen.SqlReader.readSqlStmt(reader, params);
        if(ret==null) {
            close();
        }
        return ret;
    }

    private void createPushbackReader(File sqlfile) throws FileNotFoundException {
        FileInputStream sqlFileInputStream = new FileInputStream(sqlfile);
        InputStreamReader sqlFileReader = new InputStreamReader(sqlFileInputStream, StandardCharsets.UTF_8);

        reader = new PushbackReader(sqlFileReader);
    }


    public String nextSqlStmt() throws IOException{
        if(reader==null) {
            return null;
        }
        String ret = ch.ehi.sqlgen.SqlReader.readSqlStmt(reader, params);
        if(ret==null) {
            close();
        }
        return ret;
    }


    public void close()
            throws IOException {
        if(reader!=null) {
            reader.close();
            reader=null;
        }
    }
}

