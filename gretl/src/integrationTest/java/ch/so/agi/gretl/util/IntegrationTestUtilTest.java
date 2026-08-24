package ch.so.agi.gretl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IntegrationTestUtilTest {

    @Test
    void omitsLogLevelFlagForLifecycleOutput() {
        assertEquals("", IntegrationTestUtil.getLogLevelOption("LIFECYCLE"));
    }

    @Test
    void selectsInfoFlagForInfoOutput() {
        assertEquals(" --info", IntegrationTestUtil.getLogLevelOption("INFO"));
    }

    @Test
    void selectsDebugFlagForDebugOutput() {
        assertEquals(" --debug", IntegrationTestUtil.getLogLevelOption("DEBUG"));
    }
}
