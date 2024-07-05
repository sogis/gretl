package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import jdk.internal.org.jline.utils.Log;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.junit.Assert.*;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CsvExportTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Test
    public void exportOk() {
        try {
            seedDatabase();

            File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/CsvExport");
            GradleVariable[] variables = { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()) };

            IntegrationTestUtil.getGradleRunner(projectDirectory, "csvexport", variables).build();

            // check results
            System.out.println("cwd " + new File(".").getAbsolutePath());
            LineNumberReader reader = getLineNumberReader(projectDirectory);
            String line = reader.readLine();

            assertEquals("\"t_id\",\"Aint\",\"adec\",\"atext\",\"aenum\",\"adate\",\"atimestamp\",\"aboolean\"", line);
            line = reader.readLine();
            assertEquals("\"1\",\"2\",\"3.4\",\"abc\",\"\",\"2013-10-21\",\"2015-02-16T08:35:45.000\",\"true\"", line);
            line = reader.readLine();
            assertEquals("\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"", line);

            reader.close();
        } catch(Exception e){
            Log.error("An error occurred: " + e.getMessage());
        }
    }

    private LineNumberReader getLineNumberReader(File projectDirectory) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(projectDirectory + "/CsvExport/data.csv");
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        return new LineNumberReader(inputStreamReader);
    }

    private void seedDatabase() throws SQLException {
        Connection connection = null;
        try{
            connection = IntegrationTestUtilSql.connectPG(postgres);
            String schemaName = "csvexport".toLowerCase();
            IntegrationTestUtilSql.createOrReplaceSchema(connection, schemaName);
            Statement s1 = connection.createStatement();
            s1.execute("CREATE TABLE " + schemaName + ".exportdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");
            s1.execute("INSERT INTO " + schemaName + ".exportdata(t_id, \"Aint\", adec, atext, adate, atimestamp, aboolean) VALUES (1,2,3.4,'abc','2013-10-21','2015-02-16T08:35:45.000','true')");
            s1.execute("INSERT INTO " + schemaName + ".exportdata(t_id) VALUES (2)");
            s1.close();
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(connection, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
            connection.commit();
        }  finally{
            IntegrationTestUtilSql.closeCon(connection);
        }
    }
}
