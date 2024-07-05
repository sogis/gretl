package ch.so.agi.gretl.jobs;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.ehi.ili2db.base.DbNames;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

public class Ili2pgDeleteDatasetsTest {
    private Connection connection = null;

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Before
    public void setup() {
        connection = IntegrationTestUtilSql.connectPG(postgres);
    }

    @After
    public void tearDown() {
        IntegrationTestUtilSql.closeCon(connection);
    }

    @Test
    public void deleteOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgDeleteDatasets");

        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        IntegrationTestUtil.getGradleRunner(projectDirectory, "ili2pgdelete", variables).build();

        // check results
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT count(*) FROM beispiel2.boflaechen");

        assertTrue(rs.next());

        assertEquals(0,rs.getInt(1));

        rs = s.executeQuery("SELECT "+DbNames.DATASETS_TAB_DATASETNAME+"  FROM beispiel2."+DbNames.DATASETS_TAB);
        HashSet<String> datasets=new HashSet<>();
        while(rs.next()) {
            datasets.add(rs.getString(1));
        }
        assertEquals(0,datasets.size());
        rs.close();
        s.close();
    }

}
