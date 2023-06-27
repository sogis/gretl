package ch.so.agi.gretl.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "grooming")
@JsonPropertyOrder({"daily","weekly","monthly","yearly"})
public class Grooming {
    private GroomingRange ranges[]=new GroomingRange[4];
    private static final int DAILY=0;
    private static final int WEEKLY=1;
    private static final int MONTHLY=2;
    private static final int YEARLY=3;
    public GroomingRange getDaily() {
        return ranges[DAILY];
    }
    public void setDaily(GroomingRange daily) {
        ranges[Grooming.DAILY] = daily;
    }
    public GroomingRange getWeekly() {
        return ranges[WEEKLY];
    }
    public void setWeekly(GroomingRange weekly) {
        ranges[Grooming.WEEKLY] = weekly;
    }
    public GroomingRange getMonthly() {
        return ranges[MONTHLY];
    }
    public void setMonthly(GroomingRange monthly) {
        ranges[Grooming.MONTHLY] = monthly;
    }
    public GroomingRange getYearly() {
        return ranges[YEARLY];
    }
    public void setYearly(GroomingRange yearly) {
        ranges[Grooming.YEARLY] = yearly;
    }
    public void isValid() throws IOException {
        int start=0;
        for(;start<ranges.length;start++) {
            if(ranges[start]!=null && ranges[start].getFrom()!=null) {
                break;
            }
        }
        if(start==ranges.length) {
            throw new IOException("kein start definiert "+this.toString());
        }
        int end=-1;
        if(ranges[start].getTo()==null) {
            end=start;
        }else {
            int previous=start;
            int current=previous+1;
            for(;current<ranges.length;current++) {
                if(ranges[current]!=null) {
                    if( ranges[previous].getTo()==null) {
                        throw new IOException("ranges["+previous+"].getTo()==null");
                    }else if(ranges[current].getFrom()==null) {
                        throw new IOException("ranges["+current+"].getFrom()==null");
                    }else if( ranges[previous].getTo().equals(ranges[current].getFrom())) {
                        previous=current;
                    }else if( !ranges[previous].getTo().equals(ranges[current].getFrom())) {
                        throw new IOException("Luecke/Ueberlappung vorhanden "+ranges[previous].getTo()+" "+ranges[current].getFrom());
                    }
                    if( ranges[current].getTo()==null) {
                        end=current;
                        break;
                    }
                }
            }
        }
        if(end>=0) {
            int current=end+1;
            for(;current<ranges.length;current++) {
                if(ranges[current]!=null) {
                    throw new IOException("Definition nach Ende vorhanden (Ende: "+end+", Definition: "+current+")");
                }
            }
        }
    }
    public static long diffInDays(java.util.Date today,java.util.Date filedate) {
        long diffInMillies = today.getTime() - filedate.getTime();
        long diff = java.util.concurrent.TimeUnit.DAYS.convert(Math.abs(diffInMillies), java.util.concurrent.TimeUnit.MILLISECONDS);
        if(diffInMillies<0) {
            return -diff;
        }
        return diff;
    }
    public static boolean isInRange(long days,GroomingRange range) {
        if(days<0) {
            return false;
        }
        if(range==null || range.getFrom()==null) {
            return false;
        }
        if(days>=range.getFrom() && (range.getTo()==null || days<range.getTo())) {
            return true;
        }
        return false;
    }
    public boolean isDaily(long diff) {
        return isInRange(diff,ranges[DAILY]);
    }
    public boolean isWeekly(long diff) {
        return isInRange(diff,ranges[WEEKLY]);
    }
    public boolean isMonthly(long diff) {
        return isInRange(diff,ranges[MONTHLY]);
    }
    public boolean isYearly(long diff) {
        return isInRange(diff,ranges[YEARLY]);
    }
    @Override
    public String toString() {
        return "Grooming [daily=" + ranges[DAILY] + ", weekly=" + ranges[WEEKLY] + ", monthly=" + ranges[MONTHLY] + ", yearly=" + ranges[YEARLY] + "]";
    }
    public void getFilesToDelete(Date today, List<Date> allHistory0, List<Date> deleteDates) {
        List<Date> allHistory=new java.util.ArrayList<Date>();
        allHistory.addAll(allHistory0);
        allHistory.sort(null); // oldest first!
        //allHistory.sort(Collections.reverseOrder()); // newest first!
        Set<Long> weeks=new HashSet<Long>();
        Set<Long> months=new HashSet<Long>();
        Set<Long> years=new HashSet<Long>();
        for(Date item:allHistory) {
            long diff=diffInDays(today, item);
            java.util.Calendar itemc=java.util.Calendar.getInstance();
            itemc.setTime(item);
            long year=itemc.get(java.util.Calendar.YEAR);
            if(item.equals(today)) {
                // today; keep it in any case
            }else if(isYearly(diff)) {
                if(years.contains(year)) {
                    deleteDates.add(item);
                }else {
                    years.add(year);
                }
            }else if(isMonthly(diff)) {
                long month=year*100+itemc.get(java.util.Calendar.MONTH);
                if(months.contains(month)) {
                    deleteDates.add(item);
                }else {
                    months.add(month);
                }
            }else if(isWeekly(diff)) {
                long month=year*100+itemc.get(java.util.Calendar.MONTH);
                long week=month*100+itemc.get(java.util.Calendar.WEEK_OF_MONTH);
                if(getWeekly()==null) {
                    week=year*100+itemc.get(java.util.Calendar.WEEK_OF_YEAR);
                }
                if(weeks.contains(week)) {
                    deleteDates.add(item);
                }else {
                    weeks.add(week);
                }
            }else if(isDaily(diff)) {
                ;
            }else {
                deleteDates.add(item);
            }
        }
    }

}
