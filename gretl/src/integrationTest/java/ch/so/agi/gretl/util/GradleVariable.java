package ch.so.agi.gretl.util;

/**
 * Helper class that constructs the -P and -D variable options
 * for a gradle command line call.
 * Use newGradleProperty(...) or newJvmOption(...) to make
 * instances of this class.
 */
public class GradleVariable {
    private static final String TYPE_GRADLE_PROPERTY = "P";
    private static final String TYPE_JVM_OPTION = "D";

    private String type;
    private String name;
    private String value;

    private GradleVariable(String type, String name, String value){
        if(name.contains(" ")){
            throw new RuntimeException("No spaces allowed in gradle variable name");
        }

        /*
         * Wrapping the value in "" or '' both leads to unexpected behaviour of gradle.
         * For the sql tasks, gradle does no more find the jdbc driver to postgres
         */
        if(value.contains(" ")){
            throw new RuntimeException("No spaces allowed in gradle variable value");
        }

        this.type = type;
        this.name = name;
        this.value = value;
    }

    public String buildOptionString(){
        return String.format("-%s%s=%s", type, name, value);
    }

    public static GradleVariable newGradleProperty(String name, String value){
        return new GradleVariable(TYPE_GRADLE_PROPERTY, name, value);
    }

    public static GradleVariable newJvmOption(String name, String value){
        return new GradleVariable(TYPE_JVM_OPTION, name, value);
    }
}

