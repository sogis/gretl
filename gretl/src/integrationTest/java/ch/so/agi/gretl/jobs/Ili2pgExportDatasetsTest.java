package ch.so.agi.gretl.jobs;

import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.*;
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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Testcontainers
public class Ili2pgExportDatasetsTest {
    
    @Container
    public static PostgreSQLContainer<?> postgres =
        (PostgreSQLContainer<?>) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl")
            .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
            .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
            .withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void exportOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgExportDatasets");

        GradleVariable[] variables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

        IntegrationTestUtil.executeTestRunner(projectDirectory, variables);
        
        // check results
        {
            assertXtfFile(new File("src/integrationTest/jobs/Ili2pgExportDatasets/DatasetA-out.xtf"));
        }
        {
            assertXtfFile(new File("src/integrationTest/jobs/Ili2pgExportDatasets/DatasetB-out.xtf"));
        }
    }

    private void assertXtfFile(java.io.File file) throws IoxException {
        XtfReader reader=new XtfReader(file);
        assertInstanceOf(StartTransferEvent.class, reader.read());
        assertInstanceOf(StartBasketEvent.class, reader.read());
        
        IoxEvent event = reader.read();
        while(event instanceof ObjectEvent) {
            event = reader.read();
        }
        assertInstanceOf(EndBasketEvent.class, event);
        assertInstanceOf(EndTransferEvent.class, reader.read());
    }
}
