package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlExecutorTaskTest {
    /*
    Test's that a chain of statements executes properly
    1. statement: fill the source table with rows
    2. statement: execute the "insert into select from" statement
    */
    @Test
    public void taskChainTest() throws Exception {
        String schemaName = "sqlExecuterTaskChain".toLowerCase();
        Connection con = null;
        try{
            con = IntegrationTestUtilSql.connectPG();
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            IntegrationTestUtilSql.createSqlExecuterTaskChainTables(con, schemaName);

            con.commit();
            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, IntegrationTestUtilSql.PG_CON_URI)};
            IntegrationTestUtil.runJob("jobs/sqlExecutorTaskChain", gvs);

            //reconnect to check results
            con = IntegrationTestUtilSql.connectPG();

            String countSrcSql = String.format("select count(*) from %s.albums_src", schemaName);
            String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);

            int countSrc = IntegrationTestUtilSql.execCountQuery(con, countSrcSql);
            int countDest = IntegrationTestUtilSql.execCountQuery(con, countDestSql);

            Assert.assertEquals("Rowcount in destination table must be equal to rowcount in source table", countSrc, countDest);
            Assert.assertTrue("Rowcount in destination table must be greater than zero", countDest > 0);
        }
        finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }

    /**
     * Test's if the sql-files can be configured using a relative path.
     *
     * The relative path relates to the location of the build.gradle file
     * of the corresponding gretl job.
     */
    @Test
    public void relPathTest() throws Exception {
        String schemaName = "sqlExecuterRelPath".toLowerCase();
        Connection con = null;
        try{
            con = IntegrationTestUtilSql.connectPG();
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            IntegrationTestUtilSql.createSqlExecuterTaskChainTables(con, schemaName);

            con.commit();
            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, IntegrationTestUtilSql.PG_CON_URI)};
            IntegrationTestUtil.runJob("jobs/sqlExecutorTaskRelPath", gvs);
        }
        finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }
}
