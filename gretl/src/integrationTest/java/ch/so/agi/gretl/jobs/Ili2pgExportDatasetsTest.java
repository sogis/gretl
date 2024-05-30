package ch.so.agi.gretl.jobs;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.ehi.ili2db.base.DbNames;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.EndBasketEvent;
import ch.interlis.iox.EndTransferEvent;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox.StartBasketEvent;
import ch.interlis.iox.StartTransferEvent;
import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

public class Ili2pgExportDatasetsTest {
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(TestUtil.WAIT_PATTERN, 2));

    @Test
    public void exportOk() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgExportDatasets", gvs);
        
        // check results
        {
            assertXtfFile(new java.io.File("src/integrationTest/jobs/Ili2pgExportDatasets/DatasetA-out.xtf"));
        }
        {
            assertXtfFile(new java.io.File("src/integrationTest/jobs/Ili2pgExportDatasets/DatasetB-out.xtf"));
        }
    }

    private void assertXtfFile(java.io.File file) throws IoxException {
        XtfReader reader=new XtfReader(file);
        assertTrue(reader.read() instanceof StartTransferEvent);
        assertTrue(reader.read() instanceof StartBasketEvent);
        
        IoxEvent event=reader.read();
        while(event instanceof ObjectEvent) {
            event=reader.read();
        }
        assertTrue(event instanceof EndBasketEvent);
        assertTrue(reader.read() instanceof EndTransferEvent);
    }

}
