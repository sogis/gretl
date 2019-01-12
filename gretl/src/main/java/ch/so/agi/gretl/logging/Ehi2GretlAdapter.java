package ch.so.agi.gretl.logging;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.logging.LogEvent;
import ch.ehi.basics.logging.LogListener;
import ch.ehi.basics.logging.StdListener;
import ch.interlis.iox.IoxLogEvent;

public class Ehi2GretlAdapter implements LogListener {
    private static Ehi2GretlAdapter instance = null;
    private GretlLogger logger = null;

    private Ehi2GretlAdapter() {
        logger = LogEnvironment.getLogger(EhiLogger.class);
    }

    public static void init() {
        if (instance == null) {
            instance = new Ehi2GretlAdapter();
            EhiLogger.getInstance().addListener(instance);
            EhiLogger.getInstance().removeListener(StdListener.getInstance());
        }
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
            logger.info(objRef + msg);
            break;
        case LogEvent.STATE:
            logger.info(objRef + msg);
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
}
