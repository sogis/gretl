package ch.so.agi.gretl.util;

public class GroomingRange {
    private Integer from=null;
    private Integer to=null;
    public GroomingRange() {
    }
    public GroomingRange(Integer from, Integer to) {
        this.from=from;
        this.to=to;
    }
    public Integer getFrom() {
        return from;
    }
    public void setFrom(Integer from) {
        this.from = from;
    }
    public Integer getTo() {
        return to;
    }
    public void setTo(Integer to) {
        this.to = to;
    }
    @Override
    public String toString() {
        return "[from=" + from + ", to=" + to + "]";
    }

}
