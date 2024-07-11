package ch.so.agi.gretl.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.AbstractPublisherStepTest;
import ch.so.agi.gretl.steps.PublisherStep;

import static org.gradle.internal.impldep.org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;

public class GroomingTest {
    final public static String SRC_TEST_DATA = AbstractPublisherStepTest.SRC_TEST_DATA;

    protected GretlLogger log;
    private java.text.DateFormat dateParser = Grooming.getDateFormat();

    public GroomingTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void weeks() throws Exception {
        Date date0=dateParser.parse("2022-10-31");
        Date date1=addOneDay(date0);
        printDate(date0);
        printDate(date1);
        Calendar cal0=Grooming.getCalendar();cal0.setTime(date0);
        Calendar cal1=Grooming.getCalendar();cal1.setTime(date1);
        assertEquals(9, cal0.get(java.util.Calendar.MONTH));
        assertEquals(10, cal1.get(java.util.Calendar.MONTH));
        assertEquals(5, cal0.get(java.util.Calendar.WEEK_OF_MONTH));
        assertEquals(1, cal1.get(java.util.Calendar.WEEK_OF_MONTH));
    }

    protected void printDate(Date date) {
        java.util.Calendar itemc=Grooming.getCalendar();
        itemc.setTimeInMillis(date.getTime());
        long year=itemc.get(java.util.Calendar.YEAR);
        long month=itemc.get(java.util.Calendar.MONTH);
        long weekOfMonth=itemc.get(java.util.Calendar.WEEK_OF_MONTH);
        long weekOfYear=itemc.get(java.util.Calendar.WEEK_OF_YEAR);
        System.out.println("date "+date+", year "+year+", month "+month+", weekOfMonth "+weekOfMonth+", weekOfYear "+weekOfYear);
    }

    @Test
    public void readsimpleFile() throws Exception {
        Grooming grooming=PublisherStep.readGrooming(Paths.get(SRC_TEST_DATA).resolve("simpleGrooming.json"));
        assertEquals((Integer)0, grooming.getDaily().getFrom());
        assertEquals((Integer)1, grooming.getDaily().getTo());
        assertEquals((Integer)1, grooming.getWeekly().getFrom());
        assertEquals((Integer)4, grooming.getWeekly().getTo());
        assertEquals((Integer)4, grooming.getMonthly().getFrom());
        assertEquals((Integer)52, grooming.getMonthly().getTo());
        assertEquals((Integer)52, grooming.getYearly().getFrom());
        assertNull(grooming.getYearly().getTo());
    }

    @Test
    public void readMissingFile() throws Exception {
        try {
            Grooming grooming = PublisherStep.readGrooming(Paths.get(SRC_TEST_DATA).resolve("missingGrooming.json"));
            fail("exception expected");
        }catch(IOException ex) {
            ; // ok
            log.error("readMissingFile", ex);
        }
    }

    @Test
    public void readWrongFile() throws Exception {
        try {
            Grooming grooming = PublisherStep.readGrooming(Paths.get(SRC_TEST_DATA).resolve("wrongGrooming.json"));
            fail("exception expected");
        }catch(IOException ex) {
            ; // ok
            log.error("readWrongFile", ex);
        }
    }

    @Test
    public void dailyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setDaily(new GroomingRange(0,null));
        grooming.isValid();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=add(allHistory,"2022-05-01",null,null);
        add(allHistory,"2022-04-30",null,today);
        add(allHistory,"2022-04-01",null,today);
        add(allHistory,"2021-04-01",null,today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        assertEquals(0, deleteDates.size());
    }

    @Test
    public void dailyOnly() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setDaily(new GroomingRange(0,2));
        grooming.isValid();
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=add(allHistory,"2022-05-01",null,null);
        add(allHistory,"2022-04-30",null,today);
        add(allHistory,"2022-04-29",expectedDeleteDates,today);
        add(allHistory,"2022-04-01",expectedDeleteDates,today);
        add(allHistory,"2021-04-01",expectedDeleteDates,today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        assertEquals(expectedDeleteDates, deleteDates);
    }

    @Test
    public void weeklyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,null));
        grooming.isValid();
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=add(allHistory,"2022-05-01",null,null);
        add(allHistory,"2022-04-30",expectedDeleteDates,today);
        add(allHistory,"2022-04-29",null,today);
        add(allHistory,"2022-04-02",expectedDeleteDates,today);
        add(allHistory,"2022-04-01",null,today);
        add(allHistory,"2021-04-02",expectedDeleteDates,today);
        add(allHistory,"2021-04-01",null,today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        assertEquals(expectedDeleteDates, deleteDates);
    }

    @Test
    public void weeklyOnly() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,30));
        grooming.isValid();
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=add(allHistory,"2022-05-01",null,null);
        add(allHistory,"2022-04-30",expectedDeleteDates,today);
        add(allHistory,"2022-04-29",null,today);
        add(allHistory,"2022-04-03",expectedDeleteDates,today);
        add(allHistory,"2022-04-02",null,today);
        add(allHistory,"2022-04-01",expectedDeleteDates,today);
        add(allHistory,"2021-04-02",expectedDeleteDates,today);
        add(allHistory,"2021-04-01",expectedDeleteDates,today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        assertEquals(expectedDeleteDates, deleteDates);
    }

    private Date add(List<Date> allHistory,
        String dateTxt,List<Date> expectedDeleteDates,Date today) throws ParseException{
        Date date=dateParser.parse(dateTxt);
        if(today!=null) {
            long diff=Grooming.diffInDays(today, date);
            //System.out.println(dateTxt+" day "+diff+" week "+diff/Grooming.DAYS_PER_WEEK+" month "+diff/Grooming.DAYS_PER_MONTH+" year "+diff/Grooming.DAYS_PER_YEAR);
        }
        allHistory.add(date);
        if(expectedDeleteDates!=null) {
            expectedDeleteDates.add(date);
        }
        return date;
    }

    @Test
    public void weeklyMonthlyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,30));
        grooming.setMonthly(new GroomingRange(30,null));
        grooming.isValid();
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=add(allHistory,"2022-05-01",null,null);
        add(allHistory,"2022-04-30",null,today);
        add(allHistory,"2022-04-03",expectedDeleteDates,today);
        add(allHistory,"2022-04-02",null,today);
        add(allHistory,"2022-04-01",null,today);
        add(allHistory,"2021-04-02",expectedDeleteDates,today);
        add(allHistory,"2021-04-01",null,today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        assertEquals(expectedDeleteDates, deleteDates);
    }

    @Test
    public void doWeeklyMonthlyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,29));
        grooming.setMonthly(new GroomingRange(29,null));
        grooming.isValid();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=dateParser.parse("2022-05-01");
        for(int i=0;i<60;i++) {
            List<Date> deleteDates=new ArrayList<Date>();
            allHistory.add(today);
            //print("---",allHistory,today);
            grooming.getFilesToDelete(today, allHistory, deleteDates);
            allHistory.removeAll(deleteDates);
            //print("",allHistory,today);
            today = addOneDay(today);
        }
        Date[] expected=new Date[] {dateParser.parse("2022-05-01"),dateParser.parse("2022-06-01"),dateParser.parse("2022-06-06"),dateParser.parse("2022-06-13"),dateParser.parse("2022-06-20"),dateParser.parse("2022-06-27"),dateParser.parse("2022-06-29")};
        assertArrayEquals(expected, allHistory.toArray(new Date[0]));
    }

    @Test
    public void doWeeklyYearlyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,29));
        grooming.setYearly(new GroomingRange(29,null));
        grooming.isValid();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=dateParser.parse("2022-05-01");
        for(int i=0;i<370;i++) {
            List<Date> deleteDates=new ArrayList<Date>();
            allHistory.add(today);
            //print("---",allHistory,today);
            grooming.getFilesToDelete(today, allHistory, deleteDates);
            allHistory.removeAll(deleteDates);
            //print("",allHistory,today);
            today = addOneDay(today);
            //System.out.println(today);
        }
        Date[] expected = new Date[] {dateParser.parse("2022-05-01"),dateParser.parse("2023-01-01"),dateParser.parse("2023-04-10"),dateParser.parse("2023-04-17"),dateParser.parse("2023-04-24"),dateParser.parse("2023-05-01"),dateParser.parse("2023-05-05")};
        assertArrayEquals(expected, allHistory.toArray(new Date[0]));
    }

    protected Date addOneDay(Date today) {
        java.util.Calendar c=Grooming.getCalendar();
        c.setTime(today);
        c.add(java.util.Calendar.DAY_OF_MONTH, 1);
        today=new Date(c.getTimeInMillis());
        return today;
    }

    private void print(String prefix,List<Date> allHistory,Date today) {
        StringBuffer all=new StringBuffer();
        String sep="";
        for(Date date:allHistory) {
            String txt=dateParser.format(date);
            all.append(sep);
            all.append(txt);
            long diff=Grooming.diffInDays(today, date);
            java.util.Calendar c=Grooming.getCalendar();
            c.setTime(date);
            all.append("("+c.get(java.util.Calendar.MONTH)+":"+c.get(java.util.Calendar.WEEK_OF_YEAR)+")");
            sep=" ";
        }
        System.out.println(prefix+all);
    }

    @Test
    public void weeklyMonthly() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,2));
        grooming.setMonthly(new GroomingRange(2,5));
        grooming.isValid();
    }

    @Test
    public void weeklyMonthlyMismatch() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,2));
        grooming.setMonthly(new GroomingRange(1,5));
        try {
            grooming.isValid();
            fail();
        } catch(IOException ignored) {}
    }

    @Test
    public void weeklyYearlyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,2));
        grooming.setYearly(new GroomingRange(2,null));
        grooming.isValid();
    }

    @Test
    public void weeklyYearly() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,2));
        grooming.setYearly(new GroomingRange(2,5));
        grooming.isValid();
    }

    @Test
    public void weeklyYearlyMismatch() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,2));
        grooming.setYearly(new GroomingRange(3,5));
        try {
            grooming.isValid();
            fail();
        } catch(IOException ignored) {}
    }

    @Test
    public void weeklyYearlyOpenEndMismatch() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,null));
        grooming.setYearly(new GroomingRange(29,null));
        try {
            grooming.isValid();
            fail();
        } catch(IOException ignored) {}
    }

    @Test
    public void yearlyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setYearly(new GroomingRange(0,null));
        grooming.isValid();
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=add(allHistory,"2022-05-01",null,null);
        add(allHistory,"2022-04-02",expectedDeleteDates,today);
        add(allHistory,"2022-04-01",null,today);
        add(allHistory,"2021-04-02",expectedDeleteDates,today);
        add(allHistory,"2021-04-01",null,today);
        add(allHistory,"2020-04-02",expectedDeleteDates,today);
        add(allHistory,"2020-04-01",null,today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        assertEquals(expectedDeleteDates, deleteDates);
    }

    @Test
    public void yearlyOnly() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setYearly(new GroomingRange(0,365));
        grooming.isValid();
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        Date today=add(allHistory,"2022-05-01",null,null);
        add(allHistory,"2022-04-02",expectedDeleteDates,today);
        add(allHistory,"2022-04-01",null,today);
        add(allHistory,"2021-04-02",expectedDeleteDates,today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        assertEquals(expectedDeleteDates, deleteDates);
    }
}
