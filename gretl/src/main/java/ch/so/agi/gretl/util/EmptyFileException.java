package ch.so.agi.gretl.util;

public class EmptyFileException extends GretlException {

    public EmptyFileException() {
    }

    public EmptyFileException(String message) {
        super(message);
    }

    public EmptyFileException(String message, Throwable cause) {
        super(message, cause);
    }


}
