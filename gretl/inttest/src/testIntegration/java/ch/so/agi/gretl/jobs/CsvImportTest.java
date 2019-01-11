package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSql;

import org.junit.Test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Assert;

public class CsvImportTest {
    @Test
    public void importOk() throws Exception {
        String schemaName = "csvimport".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSql.connectPG();
            TestUtilSql.createOrReplaceSchema(con, schemaName);
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE "+schemaName+".importdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");
            s1.close();
            TestUtilSql.grantDataModsInSchemaToUser(con, schemaName, TestUtilSql.PG_CON_DMLUSER);

            con.commit();
            TestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
            TestUtil.runJob("jobs/CsvImport", gvs);

            //reconnect to check results
            con = TestUtilSql.connectPG();

            Statement s2 = con.createStatement();
            ResultSet rs=s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum,adate, atimestamp, aboolean, aextra FROM "+schemaName+".importdata WHERE t_id=1"); 
            if(!rs.next()) {
                fail();
            }
            assertEquals(2,rs.getInt(1));
            assertEquals(new BigDecimal("3.1"),rs.getBigDecimal(2));
            assertEquals("abc",rs.getString(3));
            assertEquals("rot",rs.getString(4));
            assertEquals(new java.sql.Date(2017-1900,9-1,21),rs.getDate(5));
            assertEquals(new java.sql.Timestamp(2016-1900,8-1,22,13,15,22,450000000),rs.getTimestamp(6));
            assertEquals(true,rs.getBoolean(7));
            if(rs.next()) {
                fail();
            }
            rs.close();
            s1.close();
        }
        finally {
            TestUtilSql.closeCon(con);
        }
    }
    
    @Test
    public void importOk_batchSize() throws Exception {
        String schemaName = "csvimport".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSql.connectPG();
            TestUtilSql.createOrReplaceSchema(con, schemaName);
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE "+schemaName+".importdata_batchsize(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");
            s1.close();
            TestUtilSql.grantDataModsInSchemaToUser(con, schemaName, TestUtilSql.PG_CON_DMLUSER);

            con.commit();
            TestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
            TestUtil.runJob("jobs/CsvImportBatchSize", gvs);

            //reconnect to check results
            con = TestUtilSql.connectPG();

            Statement s2 = con.createStatement();
            ResultSet rs=s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum,adate, atimestamp, aboolean, aextra FROM "+schemaName+".importdata_batchsize WHERE t_id=1"); 
            if(!rs.next()) {
                fail();
            }
            assertEquals(2,rs.getInt(1));
            assertEquals(new BigDecimal("3.1"),rs.getBigDecimal(2));
            assertEquals("abc",rs.getString(3));
            assertEquals("rot",rs.getString(4));
            assertEquals(new java.sql.Date(2017-1900,9-1,21),rs.getDate(5));
            assertEquals(new java.sql.Timestamp(2016-1900,8-1,22,13,15,22,450000000),rs.getTimestamp(6));
            assertEquals(true,rs.getBoolean(7));
            if(rs.next()) {
                fail();
            }
            rs.close();
            s1.close();
        }
        finally {
            TestUtilSql.closeCon(con);
        }
    }

}
