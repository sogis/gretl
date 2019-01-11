package ch.so.agi.gretl.logging;

import org.gradle.api.logging.LogLevel;
import org.gradle.internal.logging.events.LogEvent;
import org.gradle.internal.logging.events.OutputEvent;
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLoggerContext;
import org.junit.*;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.logging.Logger;




/**
 * Test-Class for Logger-Class
 */
public class GradleLoggerTest {

    private GretlLogger log;
    private GradleCollector collector=new GradleCollector();


    public GradleLoggerTest() {
        // make sure we use the gradle logger, as this is the gradle logger test
        LogEnvironment.setLogFactory(new GradleLogFactory());
        log = LogEnvironment.getLogger(this.getClass());
        OutputEventListenerBackedLoggerContext context = (OutputEventListenerBackedLoggerContext) LoggerFactory.getILoggerFactory();
        context.setLevel(LogLevel.DEBUG);
        context.setOutputEventListener(collector);
    }
    private void resetSystemOutAndErr() {
        collector.clear();
        
    }

    @Test
    public void logInfoTest() throws Exception {

        try {

            log.info("$Info-Logger-Test$");

            LogEvent event=(LogEvent) collector.getEvent(0);
            Assert.assertEquals(LogLevel.INFO,event.getLogLevel());
              Assert.assertEquals(event.getMessage(),"$Info-Logger-Test$");

        } finally {
            resetSystemOutAndErr();
        }

    }

    @Test
    public void logErrorTest() throws Exception {

        try {
            log.error("$Error-Logger-Test$", new RuntimeException("Test Exception"));

            LogEvent event=(LogEvent) collector.getEvent(0);
            Assert.assertEquals(LogLevel.ERROR,event.getLogLevel());
              Assert.assertEquals(event.getMessage(),"$Error-Logger-Test$");

        } finally {
            resetSystemOutAndErr();
        }

    }


    @Test
    public void logDebugTest() throws Exception {

        try{
            log.debug("$Debug-Logger-Test$");

            LogEvent event=(LogEvent) collector.getEvent(0);
            Assert.assertEquals(LogLevel.DEBUG,event.getLogLevel());
              Assert.assertEquals(event.getMessage(),"$Debug-Logger-Test$");

        } finally {
            resetSystemOutAndErr();
        }

    }


}

