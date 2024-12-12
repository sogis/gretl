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

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class Db2DbTaskTest {

    private GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));
    
	/*
	 * Tests if fetchSize parameter is working.
	 * Gradle throws an error if a parameter is being
	 * used that is not defined in the task class.
	 */
    @Test
    public void fetchSizeParameterTest() throws Exception {
        String schemaName = "db2dbTaskFetchSize".toLowerCase();

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            stmt.execute("CREATE TABLE " + schemaName + ".source_data(" +
                    "t_id serial, aint integer, adec decimal(7,1), " +
                    "atext varchar(40), aenum varchar(120), adate date, " +
                    "atimestamp timestamp, aboolean boolean, " +
                    "geom_so geometry(POINT,2056))");

            stmt.execute("INSERT INTO " + schemaName + ".source_data(t_id, aint, adec, atext, adate, atimestamp, aboolean, geom_so) " +
                    "VALUES (1, 2, 3.4, 'abc', '2013-10-21', '2015-02-16T08:35:45.000', 'true', ST_GeomFromText('POINT(2638000.0 1175250.0)', 2056))");

            stmt.execute("INSERT INTO " + schemaName + ".source_data(t_id, aint, adec, atext, adate, atimestamp, aboolean, geom_so) " +
                    "VALUES (2, 33, 44.4, 'asdf', '2017-12-21', '2015-03-16T11:35:45.000', 'true', ST_GeomFromText('POINT(2648000.0 1185250.0)', 2056))");

            stmt.execute("CREATE TABLE " + schemaName + ".target_data(" +
                    "t_id serial, aint integer, adec decimal(7,1), " +
                    "atext varchar(40), aenum varchar(120), adate date, " +
                    "atimestamp timestamp, aboolean boolean, " +
                    "geom_so geometry(POINT,2056))");


            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Db2DbTaskFetchSize");
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            String countDestSql = String.format("SELECT COUNT(*) FROM %s.target_data", schemaName);
            int countDest = IntegrationTestUtilSql.execCountQuery(con, countDestSql);

            assertEquals(2, countDest, "Rowcount in table source_data must be equal to rowcount in table target_data");
        }
    }


    /**
     * Test's that a chain of statements executes properly and results in the correct number of inserts
     * (corresponding to the last statement)
     * 1. Statement transfers rows from a to b
     * 2. Statement transfers rows from b to a
     * @throws Exception
     */
    @Test
    public void taskChainTest() throws Exception {
        String schemaName = "db2dbTaskChain".toLowerCase();
        int countSrc;

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            countSrc = IntegrationTestUtilSql.prepareDb2DbChainTables(con, schemaName);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Db2DbTaskChain");
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            String countDestSql = String.format("SELECT COUNT(*) FROM %s.albums_dest", schemaName);
            int countDest = IntegrationTestUtilSql.execCountQuery(con, countDestSql);
            assertEquals(countSrc, countDest,
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest"
            );
        }
    }


    /**
     * Test's if the sql-files can be configured using a relative path.
     * The relative path relates to the location of the build.gradle file
     * of the corresponding gretl job.
     */
    @Test
    public void relativePathTest() throws Exception {
        String schemaName = "relativePath".toLowerCase();
        int countSrc;

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            countSrc = IntegrationTestUtilSql.prepareDb2DbChainTables(con, schemaName);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Db2DbTaskRelPath");
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            String countDestSql = String.format("SELECT COUNT(*) FROM %s.albums_dest", schemaName);
            int countDest = IntegrationTestUtilSql.execCountQuery(con, countDestSql);

            assertEquals(countSrc, countDest,
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest"
            );
        }
    }


    /**
     * Test's that the delete flag of the Db2dbTask's Transferset works properly
     */
    @Test
    public void deleteDestTableContent() throws Exception {
        String schemaName = "deleteDestTableContent".toLowerCase();
        int countSrc;

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            countSrc = IntegrationTestUtilSql.prepareDb2DbChainTables(con, schemaName);
            IntegrationTestUtilSql.insertRowsInAlbumsTable(con, schemaName, "dest", 3);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Db2DbTaskDelTable");
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            String countDestSql = String.format("SELECT COUNT(*) FROM %s.albums_dest", schemaName);
            int countDest = IntegrationTestUtilSql.execCountQuery(con, countDestSql);

            assertEquals(countSrc, countDest,
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest"
            );
        }
    }

    @Test
    public void parameter() throws Exception {
        String schemaName = "parameterList".toLowerCase();

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            stmt.execute(String.format("CREATE TABLE %s.src1(title text)", schemaName));
            stmt.execute(String.format("CREATE TABLE %s.dest(title text)", schemaName));
            stmt.execute(String.format("INSERT INTO %s.src1(title) VALUES('1a')", schemaName));
            stmt.execute(String.format("INSERT INTO %s.src1(title) VALUES('1b')", schemaName));

            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Db2DbParameter");
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            String countDestSql = String.format("SELECT COUNT(*) FROM %s.dest", schemaName);
            int countDest = IntegrationTestUtilSql.execCountQuery(con, countDestSql);

            assertEquals(2, countDest, "Rowcount in table src1 must be equal to rowcount in table dest");
        }
    }

    @Test
    public void parameterList() throws Exception {
        String schemaName = "parameterList".toLowerCase();

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement stmt = con.createStatement()) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            stmt.execute(String.format("CREATE TABLE %s.src1(title text)", schemaName));
            stmt.execute(String.format("CREATE TABLE %s.src2(title text)", schemaName));
            stmt.execute(String.format("CREATE TABLE %s.dest(title text)", schemaName));
            stmt.execute(String.format("INSERT INTO %s.src1(title) VALUES('1a')", schemaName));
            stmt.execute(String.format("INSERT INTO %s.src1(title) VALUES('1b')", schemaName));
            stmt.execute(String.format("INSERT INTO %s.src2(title) VALUES('2a')", schemaName));
            stmt.execute(String.format("INSERT INTO %s.src2(title) VALUES('2b')", schemaName));
            stmt.execute(String.format("INSERT INTO %s.src2(title) VALUES('2c')", schemaName));

            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Db2DbParameterList");
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            String countDestSql = String.format("SELECT COUNT(*) FROM %s.dest", schemaName);
            int countDest = IntegrationTestUtilSql.execCountQuery(con, countDestSql);

            assertEquals(5, countDest, "Rowcount in table dest must be equal to the combined rowcount in table src1 and src2");
        }
    }
}
