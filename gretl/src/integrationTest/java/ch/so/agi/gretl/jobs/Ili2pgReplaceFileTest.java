package ch.so.agi.gretl.jobs;

import ch.ehi.ili2db.base.DbNames;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class Ili2pgReplaceFileTest {
    
    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void importLocalFile_Ok() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgReplaceFile", gvs);

        try (
            Connection con = IntegrationTestUtilSql.connectPG(postgres);
            Statement stmt = con.createStatement();
        ) {
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM beispiel2.boflaechen")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }

            try (ResultSet rs = stmt.executeQuery("SELECT "+DbNames.DATASETS_TAB_DATASETNAME+" FROM beispiel2."+DbNames.DATASETS_TAB)) {
                Set<String> datasets = new HashSet<>();
                while (rs.next()) {
                    datasets.add(rs.getString(1));
                }
                assertEquals(1, datasets.size());
                assertTrue(datasets.contains("A_Dataset"));
            }
        }
    }
    
    // Nicht ganz sicher, ob es wirklich alles testet, was getestet werden soll.
    // Das ilidata.xml ist lokal und die Datei wird gefunden mittels ID. Soweit finde ich, 
    // dass wohl so ziemlich das wichtige getestet wird.
    // Wenn man ein externes ilidata.xml verwendet, wird die Datei "richtig" 
    // heruntergeladen. Dieser Schritt entfaellt beim lokalen ilidata.xml
    @Test
    public void importIlidataFile_Ok() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgReplaceIlidataFile", gvs);

        try (
                Connection con = IntegrationTestUtilSql.connectPG(postgres);
                Statement stmt = con.createStatement();
        ) {
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM agi_av_mopublic.strassenname_pos")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }

            try (ResultSet rs = stmt.executeQuery("SELECT "+DbNames.DATASETS_TAB_DATASETNAME+" FROM agi_av_mopublic."+DbNames.DATASETS_TAB)) {
                Set<String> datasets=new HashSet<>();
                while (rs.next()) {
                    datasets.add(rs.getString(1));
                }
                assertEquals(1, datasets.size());
                assertTrue(datasets.contains("2549"));
            }
        }
    }
}
