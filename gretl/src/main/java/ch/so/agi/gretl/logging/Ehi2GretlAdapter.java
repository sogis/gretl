package ch.so.agi.gretl.logging;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.logging.LogEvent;
import ch.ehi.basics.logging.LogListener;
import ch.ehi.basics.logging.StdListener;
import ch.interlis.iox.IoxLogEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class Ehi2GretlAdapter implements LogListener {
    private static Ehi2GretlAdapter instance = null;
    private static final AtomicInteger publisherLoggingDepth = new AtomicInteger();

    private final GretlLogger logger;

    private Ehi2GretlAdapter() {
        this(LogEnvironment.getLogger(EhiLogger.class));
    }

    Ehi2GretlAdapter(GretlLogger logger) {
        this.logger = logger;
    }

    public static void init() {
        if (instance == null) {
            instance = new Ehi2GretlAdapter();
            EhiLogger.getInstance().addListener(instance);
        }
        // ilivalidator reinstalls its console listener after every validation.
        // Remove it again whenever GRETL reinitializes the bridge so EHI events
        // keep flowing through the configured Gradle log level.
        EhiLogger.getInstance().removeListener(StdListener.getInstance());
        removeIli2dbConsoleLoggers();
    }

    /**
     * ili2db's legacy {@code StdLogger} instances have no public accessor and
     * can survive a preceding non-Publisher task in the same Gradle daemon.
     * Remove only those console loggers; all non-console EHI listeners remain.
     */
    private static void removeIli2dbConsoleLoggers() {
        try {
            Field listenersField = EhiLogger.class.getDeclaredField("logListenerv");
            listenersField.setAccessible(true);
            Object listeners = listenersField.get(EhiLogger.getInstance());
            if (!(listeners instanceof Collection<?>)) return;
            for (Object listener : new ArrayList<Object>((Collection<?>) listeners)) {
                if ("ch.interlis.iox_j.logging.StdLogger".equals(listener.getClass().getName())) {
                    EhiLogger.getInstance().removeListener((LogListener) listener);
                }
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // StdListener removal above still covers EHI versions without the
            // legacy ili2db logger implementation.
        }
    }

    /**
     * Routes routine EHI diagnostics produced during a Publisher run to Gradle's
     * debug level. Warning-class events and errors retain their normal levels.
     */
    public static AutoCloseable beginPublisherLogging() {
        publisherLoggingDepth.incrementAndGet();
        return () -> publisherLoggingDepth.updateAndGet(depth -> Math.max(0, depth - 1));
    }

    @Override
    public void logEvent(LogEvent event) {
        String objRef = null;
        if (event instanceof IoxLogEvent) {
            objRef = "";
            IoxLogEvent ioxEvent = (IoxLogEvent) event;
            if (ioxEvent.getSourceLineNr() != null) {
                objRef = objRef + "line " + ioxEvent.getSourceLineNr() + ": ";
            }
            if (ioxEvent.getSourceObjectTag() != null) {
                objRef = objRef + ioxEvent.getSourceObjectTag() + ": ";
            }
            if (ioxEvent.getSourceObjectTechId() != null) {
                objRef = objRef + ioxEvent.getSourceObjectTechId() + ": ";
            }
            if (ioxEvent.getSourceObjectXtfId() != null) {
                objRef = objRef + "tid " + ioxEvent.getSourceObjectXtfId() + ": ";
            }
            if (ioxEvent.getSourceObjectUsrId() != null) {
                objRef = objRef + ioxEvent.getSourceObjectUsrId() + ": ";
            }
        } else {
            objRef = "";
        }
        String msg = event.getEventMsg();
        if (msg != null) {
            msg = msg.trim();
            if (msg.length() == 0) {
                msg = null;
            }
        }
        if (msg == null) {
            Throwable ex = event.getException();
            if (ex != null) {
                msg = ex.getLocalizedMessage();
                if (msg != null) {
                    msg = msg.trim();
                    if (msg.length() == 0) {
                        msg = null;
                    }
                }
                if (msg == null) {
                    msg = ex.getClass().getName();
                }
            }
        }
        switch (event.getEventKind()) {
        case LogEvent.DEBUG_TRACE:
            logger.debug(objRef + msg);
            break;
        case LogEvent.STATE_TRACE:
            logger.debug(objRef + msg);
            break;
        case LogEvent.UNUSUAL_STATE_TRACE:
            logger.info(objRef + msg);
            break;
        case LogEvent.BACKEND_CMD:
            logRoutineEvent(objRef + msg);
            break;
        case LogEvent.STATE:
            logRoutineEvent(objRef + msg);
            break;
        case LogEvent.ADAPTION:
            logger.info(objRef + msg);
            break;
        case LogEvent.ERROR:
            // logger.error(objRef+msg, event.getException());
            logger.error(objRef + msg, null);
            break;
        default:
            logger.info(objRef + msg);
            break;
        }
    }

    private void logRoutineEvent(String msg) {
        if (publisherLoggingDepth.get() > 0) {
            logger.debug(msg);
        } else {
            logger.info(msg);
        }
    }
}
