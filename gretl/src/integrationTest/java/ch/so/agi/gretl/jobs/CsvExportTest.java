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
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class CsvExportTest {
    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void exportOk() throws Exception {
        seedDatabase();

        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvExport");
        GradleVariable[] gradleVariables = { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()) };
        IntegrationTestUtil.executeTestRunner(projectDirectory, gradleVariables);

        System.out.println("cwd " + new File(".").getAbsolutePath());
        java.io.LineNumberReader reader = new java.io.LineNumberReader(new java.io.InputStreamReader(new java.io.FileInputStream(new File("src/integrationTest/jobs/CsvExport/data.csv"))));
        String line = reader.readLine();
        assertEquals("\"t_id\",\"Aint\",\"adec\",\"atext\",\"aenum\",\"adate\",\"atimestamp\",\"aboolean\"", line);
        line = reader.readLine();
        assertEquals("\"1\",\"2\",\"3.4\",\"abc\",\"\",\"2013-10-21\",\"2015-02-16T08:35:45.000\",\"true\"", line);
        line = reader.readLine();
        assertEquals("\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"", line);
        reader.close();
    }

    private void seedDatabase() throws SQLException {
        String schemaName = "csvexport";
        try (Connection con = IntegrationTestUtilSql.connectPG(postgres)) {
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE " + schemaName + ".exportdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");
            s1.execute("INSERT INTO " + schemaName + ".exportdata(t_id, \"Aint\", adec, atext, adate, atimestamp, aboolean) VALUES (1,2,3.4,'abc','2013-10-21','2015-02-16T08:35:45.000','true')");
            s1.execute("INSERT INTO " + schemaName + ".exportdata(t_id) VALUES (2)");
            s1.close();
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

            con.commit();
            IntegrationTestUtilSql.closeCon(con);
        }
    }
}
