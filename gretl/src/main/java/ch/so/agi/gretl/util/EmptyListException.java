package ch.so.agi.gretl.util;

public class EmptyListException extends GretlException {
    public EmptyListException() {
    }

    public EmptyListException(String message) {
        super(message);
    }

    public EmptyListException(String message, Throwable cause) {
        super(message, cause);
    }

}
