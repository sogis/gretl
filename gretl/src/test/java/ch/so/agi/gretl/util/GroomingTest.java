package ch.so.agi.gretl.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.AbstractPublisherStepTest;
import ch.so.agi.gretl.steps.PublisherStep;
import ch.so.agi.gretl.util.Grooming;

public class GroomingTest {
    final public static String SRC_TEST_DATA = AbstractPublisherStepTest.SRC_TEST_DATA;

    protected GretlLogger log;
    private java.text.SimpleDateFormat dateParser=new java.text.SimpleDateFormat("yyyy-MM-dd");
    public GroomingTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    @Test
    public void readsimpleFile() throws Exception {
        Grooming grooming=PublisherStep.readGrooming(Paths.get(SRC_TEST_DATA).resolve("simpleGrooming.json"));
        Assert.assertEquals((Integer)0, grooming.getDaily().getFrom());
        Assert.assertEquals((Integer)1, grooming.getDaily().getTo());
        Assert.assertEquals((Integer)1, grooming.getWeekly().getFrom());
        Assert.assertEquals((Integer)4, grooming.getWeekly().getTo());
        Assert.assertEquals((Integer)4, grooming.getMonthly().getFrom());
        Assert.assertEquals((Integer)52, grooming.getMonthly().getTo());
        Assert.assertEquals((Integer)52, grooming.getYearly().getFrom());
        Assert.assertEquals(null, grooming.getYearly().getTo());
    }
    @Test
    public void dailyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setDaily(new GroomingRange(0,null));
        grooming.isValid();
        Date today=dateParser.parse("2022-05-01");
        List<Date> allHistory=new ArrayList<Date>();
        allHistory.add(dateParser.parse("2022-04-30"));
        allHistory.add(dateParser.parse("2022-04-01"));
        allHistory.add(dateParser.parse("2021-04-01"));
        allHistory.add(today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        Assert.assertEquals(0, deleteDates.size());
    }
    @Test
    public void dailyOnly() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setDaily(new GroomingRange(0,1));
        grooming.isValid();
        Date today=dateParser.parse("2022-05-01");
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        allHistory.add(dateParser.parse("2022-04-30"));
        allHistory.add(dateParser.parse("2022-04-01"));expectedDeleteDates.add(dateParser.parse("2022-04-01"));
        allHistory.add(dateParser.parse("2021-04-01"));expectedDeleteDates.add(dateParser.parse("2021-04-01"));
        allHistory.add(today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        Assert.assertEquals(expectedDeleteDates, deleteDates);
    }
    @Test
    public void weeklyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,null));
        grooming.isValid();
    }
    @Test
    public void weeklyOnly() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,10));
        grooming.isValid();
    }
    @Test
    public void weeklyMonthlyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setWeekly(new GroomingRange(0,1));
        grooming.setMonthly(new GroomingRange(1,null));
        grooming.isValid();
        Date today=dateParser.parse("2022-05-01");
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        allHistory.add(dateParser.parse("2022-04-30"));
        allHistory.add(dateParser.parse("2022-04-02"));
        allHistory.add(dateParser.parse("2022-04-01"));
        allHistory.add(dateParser.parse("2021-04-02"));expectedDeleteDates.add(dateParser.parse("2021-04-02"));
        allHistory.add(dateParser.parse("2021-04-01"));
        allHistory.add(today);expectedDeleteDates.add(today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        Assert.assertEquals(expectedDeleteDates, deleteDates);
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
            Assert.fail();
        }catch(IOException e) {
            
        }
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
            Assert.fail();
        }catch(IOException e) {
            
        }
    }
    @Test
    public void yearlyOpenEnd() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setYearly(new GroomingRange(0,null));
        grooming.isValid();
    }
    @Test
    public void yearlyOnly() throws Exception {
        Grooming grooming=new Grooming();
        grooming.setYearly(new GroomingRange(0,10));
        grooming.isValid();
        Date today=dateParser.parse("2022-05-01");
        List<Date> expectedDeleteDates=new ArrayList<Date>();
        List<Date> allHistory=new ArrayList<Date>();
        allHistory.add(dateParser.parse("2022-04-02"));expectedDeleteDates.add(dateParser.parse("2022-04-02"));
        allHistory.add(dateParser.parse("2022-04-01"));
        allHistory.add(dateParser.parse("2021-04-02"));expectedDeleteDates.add(dateParser.parse("2021-04-02"));
        allHistory.add(today);expectedDeleteDates.add(today);
        List<Date> deleteDates=new ArrayList<Date>();
        grooming.getFilesToDelete(today, allHistory, deleteDates);
        expectedDeleteDates.sort(null);
        Assert.assertEquals(expectedDeleteDates, deleteDates);
    }
}
