package ch.so.agi.gretl.steps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;

public class DatabaseDocumentExportStepTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(TestUtil.PG_DDLUSR_USR)
        .withPassword(TestUtil.PG_DDLUSR_PWD)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    
    public DatabaseDocumentExportStepTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;
    
    // TODO: 
    // Die mühsamen self-signed Zertifikate des AIO können schlecht getestet werden. A) Nur intern B) Filenamen können sich ändern.
    // https://artplus.verw.rootso.org/MpWeb-apSolothurnDenkmal/download/2W8v0qRZQBC0ahDnZGut3Q?mode=gis
    // Manuell getestet...
    
    @Test
    public void exportDocuments_Ok() throws Exception {
        Connector connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);

        String schemaName = "ada_denkmalschutz";
        String tableName = "fachapplikation_rechtsvorschrift_link";
        String columnName = "multimedia_link";
        
        File targetDir = folder.newFolder();

        Connection con = connector.connect();
        con.setAutoCommit(false);
        try {
            Statement stmt = con.createStatement();
            
            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            //https://artplus.verw.rootso.org/MpWeb-apSolothurnDenkmal/download/2W8v0qRZQBC0ahDnZGut3Q?mode=gis
            //http://geo.so.ch/models/ilimodels.xml
            //http://models.geo.admin.ch/ilimodels.xml
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
            con.commit();

            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, targetDir.getAbsolutePath(), "ada_", "pdf");

            File resultFile = Paths.get(targetDir.getAbsolutePath(), "ada_ilimodels.xml.pdf").toFile();
            assertTrue(resultFile.exists() == true);
            assertTrue(resultFile.length() > 60L);
            
            String content = new String(Files.readAllBytes(Paths.get(resultFile.getAbsolutePath())));
            assertTrue(content.contains("IliRepository"));
        } finally {
            con.close();
        }
    }

    // It does not work if there is a redirect from http to https. The response code will be 302.
    // https://stackoverflow.com/questions/1884230/httpurlconnection-doesnt-follow-redirect-from-http-to-https
    @Test
    public void exportDocuments_Fail() throws Exception {
        Connector connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);

        String schemaName = "ada_denkmalschutz";
        String tableName = "fachapplikation_rechtsvorschrift_link";
        String columnName = "multimedia_link";
        
        File targetDir = folder.newFolder();

        Connection con = connector.connect();
        con.setAutoCommit(false);
        try {
            Statement stmt = con.createStatement();
            
            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('https://geo.so.ch/fubar');");
            con.commit();

            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, targetDir.getAbsolutePath(), "ada_", "pdf");

            File resultFile = Paths.get(targetDir.getAbsolutePath(), "ada_ilimodels.xml.pdf").toFile();
            assertTrue(resultFile.exists() == false);
        } finally {
            con.close();
        }
    } 
    
    @Test 
    public void exportDocuments_WithoutPrefix_Ok() throws Exception {
        Connector connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);

        String schemaName = "ada_denkmalschutz";
        String tableName = "fachapplikation_rechtsvorschrift_link";
        String columnName = "multimedia_link";
        
        File targetDir = folder.newFolder();
        System.out.println("targetDir: " + targetDir.getAbsolutePath());

        Connection con = connector.connect();
        con.setAutoCommit(false);
        try {
            Statement stmt = con.createStatement();
            
            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
            con.commit();

            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, targetDir.getAbsolutePath(), null, "pdf");

            File resultFile = Paths.get(targetDir.getAbsolutePath(), "ilimodels.xml.pdf").toFile();
            assertTrue(resultFile.exists() == true);            
            assertTrue(resultFile.length() > 60L);
            
            String content = new String(Files.readAllBytes(Paths.get(resultFile.getAbsolutePath())));
            assertTrue(content.contains("IliRepository"));
        } finally {
            con.close();
        }
    }
    
    @Test 
    public void exportDocuments_WithoutFileNameExtension_Ok() throws Exception {
        Connector connector = new Connector(postgres.getJdbcUrl(), TestUtil.PG_DDLUSR_USR, TestUtil.PG_DDLUSR_PWD);

        String schemaName = "ada_denkmalschutz";
        String tableName = "fachapplikation_rechtsvorschrift_link";
        String columnName = "multimedia_link";
        
        File targetDir = folder.newFolder();
        System.out.println("targetDir: " + targetDir.getAbsolutePath());

        Connection con = connector.connect();
        con.setAutoCommit(false);
        try {
            Statement stmt = con.createStatement();
            
            stmt.execute("DROP SCHEMA IF EXISTS "+schemaName+" CASCADE;");
            stmt.execute("CREATE SCHEMA "+schemaName+";");
            stmt.execute("CREATE TABLE "+schemaName+"."+tableName+" (id serial, "+columnName+" text);");
            stmt.execute("INSERT INTO "+schemaName+"."+tableName+" ("+columnName+") VALUES('http://models.geo.admin.ch/ilimodels.xml');");
            con.commit();

            DatabaseDocumentExportStep databaseDocumentExport = new DatabaseDocumentExportStep();
            databaseDocumentExport.execute(connector, schemaName+"."+tableName, columnName, targetDir.getAbsolutePath(), null, null);

            File resultFile = Paths.get(targetDir.getAbsolutePath(), "ilimodels.xml").toFile();
            assertTrue(resultFile.exists() == true);                        
            assertTrue(resultFile.length() > 60L);
            
            String content = new String(Files.readAllBytes(Paths.get(resultFile.getAbsolutePath())));
            assertTrue(content.contains("IliRepository"));
        } finally {
            con.close();
        }
    }
}
