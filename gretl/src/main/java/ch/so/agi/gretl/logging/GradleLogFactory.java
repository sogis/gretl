package ch.so.agi.gretl.logging;

public class GradleLogFactory implements LogFactory {

    GradleLogFactory(){}

    public GretlLogger getLogger(Class logSource){
        return new GradleLogAdaptor(logSource);
    }

}
