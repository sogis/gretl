package ch.so.agi.gretl.util;

/**
 * Baseclass for all Exceptions thrown in the Steps code.
 *
 * The Tasks convert pure GretlExceptions to GradleExceptions
 * to avoid wrapping the GretlException in the GradleException,
 * aiming at less confusing Exception nesting.
 */
public class GretlException extends RuntimeException {

    public static final String TYPE_NO_FILE = "no file";
    public static final String TYPE_NO_DB = "no database";
    public static final String TYPE_WRONG_EXTENSION = "no .sql-Extension";
    public static final String TYPE_MISSING_FILE_EXTENSION = "no file extension";
    public static final String TYPE_FILE_WITH_BOM ="file with unallowed BOM";
    public static final String TYPE_NO_STATEMENT = "no statement in sql-file";
    public static final String TYPE_FILE_NOT_READABLE = "TYPE_FILE_NOT_READABLE";
    public static final String TYPE_COLUMN_MISMATCH = "TYPE_COLUMN_MISMATCH";

    private String type;

    public GretlException(){}

    public GretlException(String message) {
        super(message);
    }

    public GretlException(String message, Throwable cause) {
        super(message, cause);
    }

    public GretlException(Throwable cause) {
        super(cause);
    }

    public GretlException(String type, String message){
        super(message);
        this.type = type;

    }

    public String getType(){
        return this.type;
    }
}
