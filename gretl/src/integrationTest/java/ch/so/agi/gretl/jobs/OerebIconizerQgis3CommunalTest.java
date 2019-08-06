package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.imageio.ImageIO;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

public class OerebIconizerQgis3CommunalTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    private static String dbusr = "ddluser";
    private static String dbpwd = "ddluser";
    private static String dbdatabase = "gretl";

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName(dbdatabase)
        .withUsername(dbusr).withPassword(dbpwd)
        .withInitScript("oerebIconizer/init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));
    
    @ClassRule
    public static GenericContainer qgis = new GenericContainer("sogis/qgis-server-base:3.4")
            .withEnv("QGIS_FCGI_MIN_PROCESSES", "0")
            .withEnv("QGIS_FCGI_MAX_PROCESSES", "1")
            .withExposedPorts(80).withClasspathResourceMapping("oerebIconizer/communal", "/data", BindMode.READ_WRITE).waitingFor(Wait.forHttp("/"));

    
    @Test
    public void createAndSaveSymbolsCommunal_Ok() throws Exception {
        // Schema and table creation including data preparation is done
        // in the oerebIconizer/init_postgresql.sql.

        String ipAddress = qgis.getContainerIpAddress();
        String port = String.valueOf(qgis.getFirstMappedPort());
        
        // I have problems with the url that are passed to the start-gretl.sh. It seems that they are not interpreted
        // correctly in the shell script. So I pass only the url without any path and query string. The url needs to
        // be passed because it is dynamic.
        String sldUrl = "http://" + ipAddress + ":" + port;
        String legendGraphicUrl = "http://" + ipAddress + ":" + port;

        GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl()), GradleVariable.newGradleProperty("legendGraphicUrl", legendGraphicUrl), GradleVariable.newGradleProperty("sldUrl", sldUrl)};
        IntegrationTestUtil.runJob("src/integrationTest/jobs/OerebIconizerQgis3Communal", gvs);
        
        // check results
        // TypeCode and legend text do not fit together in real life. 
        String typeCodeCommunal = "1111";
        File symbolFile = new File("src/integrationTest/resources/oerebIconizer/communal/gruen_und_freihaltezone_innerhalb_bauzone.png");

        Connection con = IntegrationTestUtilSql.connectPG(postgres);

        Statement s = con.createStatement();
        ResultSet rs = s.executeQuery("SELECT artcode, symbol, legendetext_de FROM agi_oereb.transferstruktur_legendeeintrag_kommunal");
        
        if(!rs.next()) {
            fail();
        }
        
        assertEquals(typeCodeCommunal, rs.getString(1));

        ByteArrayInputStream bis = new ByteArrayInputStream(rs.getBytes(2));
        BufferedImage bim = ImageIO.read(bis);
        assertEquals(ImageIO.read(symbolFile).getHeight(), bim.getHeight());
        assertEquals(ImageIO.read(symbolFile).getWidth(), bim.getWidth());
        assertEquals(ImageIO.read(symbolFile).isAlphaPremultiplied(), bim.isAlphaPremultiplied());
                        
        if(rs.next()) {
            fail();
        }

        rs.close();
        s.close();

        IntegrationTestUtilSql.closeCon(con);
    }
    

}
