package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.steps.DatabaseDocumentExportStep;
import ch.so.agi.gretl.testutil.TestUtil;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

public class DatabaseDocumentExportTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Test
    public void exportOk() throws Exception {
        String schemaName = "ada_denkmalschutz";
        String tableName = "fachapplikation_rechtsvorschrift_link";
        String columnName = "multimedia_link";
        
        File targetDir = new File("src/integrationTest/jobs/DatabaseDocumentExport/");

        Connection con = null;
        try {
            con = IntegrationTestUtilSql.connectPG(postgres);
            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);

            Statement stmt = con.createStatement();
            
            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://geo.so.ch/models/ilimodels.xml');");
            stmt.close();
            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);

            con.commit();

            IntegrationTestUtilSql.closeCon(con);

            GradleVariable[] gvs = { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()) };
            IntegrationTestUtil.runJob("src/integrationTest/jobs/DatabaseDocumentExport", gvs);

//            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
//            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, targetDir.getAbsolutePath(), "ada_", "pdf");
//
//            File resultFile = Paths.get(targetDir.getAbsolutePath(), "ada_ilimodels.xml.pdf").toFile();
//            assertTrue(resultFile.length() > 60L);
//            
//            String content = new String(Files.readAllBytes(Paths.get(resultFile.getAbsolutePath())));
//            assertTrue(content.contains("IliRepository"));
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }

        
        
//        String schemaName = "csvexport".toLowerCase();
//        Connection con = null;
//        try {
//            con = IntegrationTestUtilSql.connectPG(postgres);
//            IntegrationTestUtilSql.createOrReplaceSchema(con, schemaName);
//            Statement s1 = con.createStatement();
//            s1.execute("CREATE TABLE " + schemaName + ".exportdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean, aextra varchar(40))");
//            s1.execute("INSERT INTO " + schemaName + ".exportdata(t_id, \"Aint\", adec, atext, adate, atimestamp, aboolean) VALUES (1,2,3.4,'abc','2013-10-21','2015-02-16T08:35:45.000','true')");
//            s1.execute("INSERT INTO " + schemaName + ".exportdata(t_id) VALUES (2)");
//            s1.close();
//            IntegrationTestUtilSql.grantDataModsInSchemaToUser(con, schemaName, IntegrationTestUtilSql.PG_CON_DMLUSER);
//
//            con.commit();
//            IntegrationTestUtilSql.closeCon(con);
//            
//            GradleVariable[] gvs = { GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()) };
//            IntegrationTestUtil.runJob("src/integrationTest/jobs/CsvExport", gvs);
//
//            // check results
//            System.out.println("cwd " + new File(".").getAbsolutePath());
//            java.io.LineNumberReader reader = new java.io.LineNumberReader(new java.io.InputStreamReader(new java.io.FileInputStream(new File("src/integrationTest/jobs/CsvExport/data.csv"))));
//            String line = reader.readLine();
//            assertEquals("\"t_id\",\"Aint\",\"adec\",\"atext\",\"aenum\",\"adate\",\"atimestamp\",\"aboolean\"", line);
//            line = reader.readLine();
//            assertEquals("\"1\",\"2\",\"3.4\",\"abc\",\"\",\"2013-10-21\",\"2015-02-16T08:35:45.000\",\"true\"", line);
//            line = reader.readLine();
//            assertEquals("\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"", line);
//            reader.close();
//        } finally {
//            IntegrationTestUtilSql.closeCon(con);
//        }
    }

}
