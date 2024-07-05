package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;
import org.junit.*;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

public class SqlExecutorTaskTest {
    private final GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

    private Connection connection = null;

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Before
    public void setup() {
        connection = IntegrationTestUtilSql.connectPG(postgres);
    }

    @After
    public void teardown() {
        IntegrationTestUtilSql.closeCon(connection);
    }

    /*
    Tests that a chain of statements executes properly
    1. statement: fill the source table with rows
    2. statement: execute the "insert into select from" statement
    */
    @Test
    public void taskChainTest() throws Exception {
        String schemaName = "sqlExecuterTaskChain".toLowerCase();
        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        IntegrationTestUtilSql.createSqlExecuterTaskChainTables(connection, schemaName);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/SqlExecutorTaskChain");

        IntegrationTestUtil.getGradleRunner(projectDirectory, "insertInto", gradleVariables).build();

        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);

        String countSrcSql = String.format("select count(*) from %s.albums_src", schemaName);
        String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);

        int countSrc = IntegrationTestUtilSql.execCountQuery(connection, countSrcSql);
        int countDest = IntegrationTestUtilSql.execCountQuery(connection, countDestSql);

        Assert.assertEquals("Rowcount in destination table must be equal to rowcount in source table", countSrc, countDest);
        Assert.assertTrue("Rowcount in destination table must be greater than zero", countDest > 0);

    }

    /**
     * Tests if the sql-files can be configured using a relative path.
     *
     * The relative path relates to the location of the build.gradle file
     * of the corresponding gretl job.
     */
    @Test
    public void relPathTest() throws Exception {
        String schemaName = "sqlExecuterRelPath".toLowerCase();

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
        IntegrationTestUtilSql.createSqlExecuterTaskChainTables(connection, schemaName);

        connection.commit();

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/SqlExecutorTaskRelPath");

        IntegrationTestUtil.getGradleRunner(projectDirectory, "relativePathConfiguration", gradleVariables).build();
    }

    @Test
    public void parameter() throws Exception{
        String schemaName = "sqlexec".toLowerCase();

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);

        Statement stmt=connection.createStatement();
        stmt.execute(String.format("CREATE TABLE %s.src(title text)", schemaName));
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName,IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/SqlExecutorTaskParameter");

        IntegrationTestUtil.getGradleRunner(projectDirectory, "insertInto", gradleVariables).build();

        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);
        String countDestSql = String.format("select title from %s.src", schemaName);
        stmt=connection.createStatement();
        ResultSet rs=stmt.executeQuery(countDestSql);
        HashSet<String> titles=new HashSet<>();
        while(rs.next()) {
            String title=rs.getString(1);
            titles.add(title);
        }
        Assert.assertEquals(1,titles.size());
        Assert.assertTrue(titles.contains("ele1"));
    }
    @Test
    public void parameterList() throws Exception{
        String schemaName = "sqlexec".toLowerCase();

        IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);

        Statement stmt=connection.createStatement();
        stmt.execute(String.format("CREATE TABLE %s.src(title text)", schemaName));
        IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName,IntegrationTestUtilSql.PG_CON_DMLUSER);

        connection.commit();
        IntegrationTestUtilSql.closeCon(connection);

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/SqlExecutorTaskParameterList");

        IntegrationTestUtil.getGradleRunner(projectDirectory, "insertInto", gradleVariables).build();
        //reconnect to check results
        connection = IntegrationTestUtilSql.connectPG(postgres);
        String countDestSql = String.format("select title from %s.src", schemaName);
        stmt=connection.createStatement();
        ResultSet rs=stmt.executeQuery(countDestSql);
        HashSet<String> titles=new HashSet<>();
        while(rs.next()) {
            String title=rs.getString(1);
            titles.add(title);
        }
        Assert.assertEquals(2,titles.size());
        Assert.assertTrue(titles.contains("ele1"));
        Assert.assertTrue(titles.contains("ele2"));
    }
}
