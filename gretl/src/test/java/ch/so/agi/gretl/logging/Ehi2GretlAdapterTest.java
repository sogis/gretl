package ch.so.agi.gretl.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.logging.LogEvent;
import ch.ehi.basics.logging.LogListener;
import ch.ehi.basics.logging.StdLogEvent;
import ch.ehi.basics.logging.StdListener;

class Ehi2GretlAdapterTest {

    @Test
    void keepsRoutineEventsAtInfoOutsidePublisher() {
        RecordingLogger logger = new RecordingLogger();
        new Ehi2GretlAdapter(logger).logEvent(event(LogEvent.STATE));

        assertEquals("info", logger.level);
    }

    @Test
    void routesRoutineEventsToDebugDuringPublisher() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        Ehi2GretlAdapter adapter = new Ehi2GretlAdapter(logger);

        try (AutoCloseable ignored = Ehi2GretlAdapter.beginPublisherLogging()) {
            adapter.logEvent(event(LogEvent.STATE));
        }

        assertEquals("debug", logger.level);
    }

    @Test
    void routesWorkerThreadRoutineEventsToDebugDuringPublisher() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        Ehi2GretlAdapter adapter = new Ehi2GretlAdapter(logger);

        try (AutoCloseable ignored = Ehi2GretlAdapter.beginPublisherLogging()) {
            Thread worker = new Thread(() -> adapter.logEvent(event(LogEvent.STATE)));
            worker.start();
            worker.join();
        }

        assertEquals("debug", logger.level);
    }

    @Test
    void keepsWarningEventsAtInfoDuringPublisher() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        Ehi2GretlAdapter adapter = new Ehi2GretlAdapter(logger);

        try (AutoCloseable ignored = Ehi2GretlAdapter.beginPublisherLogging()) {
            adapter.logEvent(event(LogEvent.UNUSUAL_STATE_TRACE));
            assertEquals("info", logger.level);
            adapter.logEvent(event(LogEvent.ADAPTION));
        }

        assertEquals("info", logger.level);
    }

    @Test
    void keepsErrorEventsAtErrorDuringPublisher() throws Exception {
        RecordingLogger logger = new RecordingLogger();
        Ehi2GretlAdapter adapter = new Ehi2GretlAdapter(logger);

        try (AutoCloseable ignored = Ehi2GretlAdapter.beginPublisherLogging()) {
            adapter.logEvent(event(LogEvent.ERROR));
        }

        assertEquals("error", logger.level);
    }

    @Test
    void reinitializationRemovesConsoleListenerRestoredByIlivalidator() throws Exception {
        EhiLogger logger = EhiLogger.getInstance();
        logger.addListener(StdListener.getInstance());

        Ehi2GretlAdapter.init();

        assertFalse(listeners(logger).contains(StdListener.getInstance()));
    }

    @SuppressWarnings("unchecked")
    private Set<LogListener> listeners(EhiLogger logger) throws Exception {
        Field field = EhiLogger.class.getDeclaredField("logListenerv");
        field.setAccessible(true);
        return (Set<LogListener>) field.get(logger);
    }

    private StdLogEvent event(int kind) {
        return new StdLogEvent(kind, "message", null, null);
    }

    private static class RecordingLogger implements GretlLogger {
        private String level;

        @Override
        public void info(String msg) {
            level = "info";
        }

        @Override
        public void debug(String msg) {
            level = "debug";
        }

        @Override
        public void error(String msg, Throwable thrown) {
            level = "error";
        }

        @Override
        public void lifecycle(String msg) {
            level = "lifecycle";
        }
    }
}
