package ch.so.agi.gretl.jobs;

import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.EndBasketEvent;
import ch.interlis.iox.EndTransferEvent;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox.StartBasketEvent;
import ch.interlis.iox.StartTransferEvent;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.IntegrationTestUtil;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class Ili2duckdbExportTest {

    private final GretlLogger log;

    public Ili2duckdbExportTest() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void exportOk() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2duckdbExport");
        Files.deleteIfExists(Paths.get(projectDirectory + "/VOLLZUG_SO0200002401_1531_20180105113131.xml"));

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory);

        // Check results
        assertXtfFile(new File("src/integrationTest/jobs/Ili2duckdbExport/VOLLZUG_SO0200002401_1531_20180105113131.xml"));
    }
    
    private void assertXtfFile(File file) throws IoxException {
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
