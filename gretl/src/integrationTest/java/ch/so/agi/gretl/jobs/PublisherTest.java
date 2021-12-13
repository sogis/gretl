package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;

public class PublisherTest {
    @Test
    public void simple() throws Exception {
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Publisher", gvs);
    }

}
