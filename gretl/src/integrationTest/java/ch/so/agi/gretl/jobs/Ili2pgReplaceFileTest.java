package ch.so.agi.gretl.jobs;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.ehi.ili2db.base.DbNames;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

public class Ili2pgReplaceFileTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    private GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
    private Connection connection = null;

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Before
    public void setup() {
        connection = IntegrationTestUtilSql.connectPG(postgres);
    }

    @After
    public void tearDown() {
        IntegrationTestUtilSql.closeCon(connection);
    }

    @Test
    public void importLocalFile_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgReplaceFile");
        IntegrationTestUtil.getGradleRunner(projectDirectory, "ili2pgimport", gradleVariables).build();

        // check results
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT count(*) FROM beispiel2.boflaechen");

        assertTrue(rs.next());

        assertEquals(2, rs.getInt(1));

        rs = s.executeQuery("SELECT "+DbNames.DATASETS_TAB_DATASETNAME+" FROM beispiel2."+DbNames.DATASETS_TAB);
        HashSet<String> datasets=new HashSet<String>();
        while(rs.next()) {
            datasets.add(rs.getString(1));
        }
        assertEquals(1, datasets.size());
        assertTrue(datasets.contains("A_Dataset"));
        rs.close();
        s.close();
    }
    
    // Nicht ganz sicher, ob es wirklich alles testet, was getestet werden soll.
    // Das ilidata.xml ist lokal und die Datei wird gefunden mittels ID. Soweit finde ich, 
    // dass wohl so ziemlich das wichtige getestet wird.
    // Wenn man ein externes ilidata.xml verwendet, wird die Datei "richtig" 
    // heruntergeladen. Dieser Schritt entfaellt beim lokalen ilidata.xml
    @Test
    public void importIlidataFile_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgReplaceIlidataFile");
        IntegrationTestUtil.getGradleRunner(projectDirectory, "ili2pgimport", gradleVariables).build();

        // check results
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT count(*) FROM agi_av_mopublic.strassenname_pos");

        assertTrue(rs.next());

        assertEquals(1, rs.getInt(1));

        rs = s.executeQuery("SELECT "+DbNames.DATASETS_TAB_DATASETNAME+" FROM agi_av_mopublic."+DbNames.DATASETS_TAB);
        HashSet<String> datasets=new HashSet<String>();
        while(rs.next()) {
            datasets.add(rs.getString(1));
        }
        assertEquals(1, datasets.size());
        assertTrue(datasets.contains("2549"));
        rs.close();
        s.close();
    }
    

}
