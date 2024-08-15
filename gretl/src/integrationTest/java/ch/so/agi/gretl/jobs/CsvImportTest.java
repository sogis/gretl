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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class CsvImportTest {
    
    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void importOk() throws Exception {
        String schemaName = "csvimport".toLowerCase();

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement s1 = con.createStatement()) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            s1.execute("CREATE TABLE " + schemaName + ".importdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120), adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");

            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        GradleVariable[] gvs = {
                GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())
        };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/CsvImport", gvs);

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres);
             Statement s2 = con.createStatement();
             ResultSet rs = s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum, adate, atimestamp, aboolean, aextra FROM " + schemaName + ".importdata WHERE t_id=1")
        ) {
            if (!rs.next()) {
                fail();
            }

            assertEquals(2, rs.getInt(1));
            assertEquals(new BigDecimal("3.1"), rs.getBigDecimal(2));
            assertEquals("abc", rs.getString(3));
            assertEquals("rot", rs.getString(4));
            assertEquals(new java.sql.Date(2017-1900,9-1,21),rs.getDate(5));
            assertEquals(new java.sql.Timestamp(2016-1900,8-1,22,13,15,22,450000000),rs.getTimestamp(6));
            assertTrue(rs.getBoolean(7));

            if (rs.next()) {
                fail();
            }
        }
    }

    @Test
    public void importOk_batchSize() throws Exception {
        String schemaName = "csvimport".toLowerCase();

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres); Statement s1 = con.createStatement()) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            s1.execute("CREATE TABLE " + schemaName + ".importdata_batchsize(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120), adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            con.commit();
        }

        GradleVariable[] gvs = {
                GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())
        };
        IntegrationTestUtil.runJob("src/integrationTest/jobs/CsvImportBatchSize", gvs);

        try (Connection con = IntegrationTestUtilSql.connectPG(postgres);
             Statement s2 = con.createStatement();
             ResultSet rs = s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum, adate, atimestamp, aboolean, aextra FROM " + schemaName + ".importdata_batchsize WHERE t_id=1")
        ) {

            if (!rs.next()) {
                fail();
            }

            assertEquals(2, rs.getInt(1));
            assertEquals(new BigDecimal("3.1"), rs.getBigDecimal(2));
            assertEquals("abc", rs.getString(3));
            assertEquals("rot", rs.getString(4));
            assertEquals(new java.sql.Date(2017-1900,9-1,21),rs.getDate(5));
            assertEquals(new java.sql.Timestamp(2016-1900,8-1,22,13,15,22,450000000),rs.getTimestamp(6));
            assertTrue(rs.getBoolean(7));

            if (rs.next()) {
                fail();
            }
        }
    }
}
