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
public class Ili2pgImportFileSetTest {

    @Container
    public static PostgreSQLContainer<?> postgres =
            (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
                    .withDatabaseName(IntegrationTestUtilSql.PG_CON_DDLDB)
                    .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
                    .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
                    .withInitScript(IntegrationTestUtilSql.PG_INIT_SCRIPT)
                    .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void importOk() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgImportFileSet", gvs);

        // Check results
        try (
                Connection con = IntegrationTestUtilSql.connectPG(postgres);
                Statement stmt = con.createStatement()
        ) {
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM beispiel2.boflaechen")) {
                assertTrue(rs.next());
                assertEquals(4,rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT " + DbNames.DATASETS_TAB_DATASETNAME + "  FROM beispiel2." + DbNames.DATASETS_TAB)) {
                Set<String> datasets = new HashSet<>();
                while (rs.next()) {
                    datasets.add(rs.getString(1));
                }
                assertEquals(2,datasets.size());
                assertTrue(datasets.contains("A_Da"));
                assertTrue(datasets.contains("B_Da"));
            }
        }
    }
}
