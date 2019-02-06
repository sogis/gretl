package ch.so.agi.gretl.jobs;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;

public class IliRepositorizerTest {
    @Test
    public void exportIliModelsXmlOk() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/IliRepositorizer", gvs);
        
        String resultString = new String(Files.readAllBytes(Paths.get("src/integrationTest/jobs/IliRepositorizer/ilimodels.xml")), StandardCharsets.UTF_8);

        assertThat(resultString, containsString("<Name>SO_MOpublic_20180221</Name>"));
        assertThat(resultString, containsString("<Name>DM01AVSO24LV95</Name>"));
        assertThat(resultString, containsString("<Name>SO_Nutzungsplanung_20171118</Name>"));
    }
}
