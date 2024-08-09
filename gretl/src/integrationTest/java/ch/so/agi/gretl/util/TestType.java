package ch.so.agi.gretl.util;

public enum TestType {
    JAR("jar"),
    IMAGE("image");

    private String value;

    TestType(String value){
        this.value = value;
    }

    public boolean equals(String value){
        return this.value.equals(value);
    }
}
