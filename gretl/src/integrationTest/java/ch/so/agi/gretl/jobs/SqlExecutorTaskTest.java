package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class SqlExecutorTaskTest {

    @Container
    public static PostgreSQLContainer<?> postgres =
            (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                    .withDatabaseName("gretl")
                    .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
                    .withInitScript("init_postgresql.sql")
                    .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    /**
     * Tests that a chain of statements executes properly
     * 1. statement: fill the source table with rows
     * 2. statement: execute the "insert into select from" statement
     * @throws Exception
     */
    @Test
    public void taskChainTest() throws Exception {
        String schemaName = "sqlExecuterTaskChain".toLowerCase();
        Connection con = null;
        try{
            con = IntegrationTestUtilSql.connectPG(postgres);
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            IntegrationTestUtilSql.createSqlExecuterTaskChainTables(con, schemaName);

            con.commit();
            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/SqlExecutorTaskChain", gvs);

            //reconnect to check results
            con = IntegrationTestUtilSql.connectPG(postgres);

            String countSrcSql = String.format("select count(*) from %s.albums_src", schemaName);
            String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);

            int countSrc = IntegrationTestUtilSql.execCountQuery(con, countSrcSql);
            int countDest = IntegrationTestUtilSql.execCountQuery(con, countDestSql);

            assertEquals(countSrc, countDest, "Rowcount in destination table must be equal to rowcount in source table");
            assertTrue(countDest > 0, "Rowcount in destination table must be greater than zero");
        }
        finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }

    /**
     * Tests if the sql-files can be configured using a relative path.
     * The relative path relates to the location of the build.gradle file
     * of the corresponding gretl job.
     */
    @Test
    public void relPathTest() throws Exception {
        String schemaName = "sqlExecuterRelPath".toLowerCase();
        Connection con = null;
        try{
            con = IntegrationTestUtilSql.connectPG(postgres);
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            IntegrationTestUtilSql.createSqlExecuterTaskChainTables(con, schemaName);

            con.commit();
            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/SqlExecutorTaskRelPath", gvs);
        }
        finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }
    @Test
    public void parameter() throws Exception{
        String schemaName = "sqlexec".toLowerCase();
        Connection con = null;
        try{
            con = IntegrationTestUtilSql.connectPG(postgres);
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);

            Statement stmt=con.createStatement();
            stmt.execute(String.format("CREATE TABLE %s.src(title text)", schemaName));
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName,IntegrationTestUtilSql.PG_CON_DMLUSER);

            con.commit();
            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/SqlExecutorTaskParameter", gvs);

            //reconnect to check results
            con = IntegrationTestUtilSql.connectPG(postgres);
            String countDestSql = String.format("select title from %s.src", schemaName);
            stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(countDestSql);
            HashSet<String> titles=new HashSet<String>();
            while(rs.next()) {
                String title=rs.getString(1);
                titles.add(title);
            }
            assertEquals(1, titles.size());
            assertTrue(titles.contains("ele1"));
        }
        finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }
    @Test
    public void parameterList() throws Exception{
        String schemaName = "sqlexec".toLowerCase();
        Connection con = null;
        try{
            con = IntegrationTestUtilSql.connectPG(postgres);
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);

            Statement stmt=con.createStatement();
            stmt.execute(String.format("CREATE TABLE %s.src(title text)", schemaName));
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName,IntegrationTestUtilSql.PG_CON_DMLUSER);

            con.commit();
            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/SqlExecutorTaskParameterList", gvs);

            //reconnect to check results
            con = IntegrationTestUtilSql.connectPG(postgres);
            String countDestSql = String.format("select title from %s.src", schemaName);
            stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(countDestSql);
            HashSet<String> titles=new HashSet<String>();
            while(rs.next()) {
                String title=rs.getString(1);
                titles.add(title);
            }
            assertEquals(2, titles.size());
            assertTrue(titles.contains("ele1"));
            assertTrue(titles.contains("ele2"));
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }
}