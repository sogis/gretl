package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;

/**
 * Test for the Class DbConnector
 */
public class DbConnectorTest {
    private GretlLogger log;
    private Connection con = null;


    public DbConnectorTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void connectToDerbyDb() throws Exception {

        DbConnector x = new DbConnector();
        try {
            con = x.connect("jdbc:derby:memory:myInMemDB;create=true", null, null);

            Assert.assertFalse("connectToDerbyDb-Test could not connect to database", con.isClosed());

        } catch (Exception e){
            throw new GretlException("Could not connect to database");
        } finally {
            if (con!=null){
                con.close();
            }
        }
    }

    @Test
    public void connectionAutoCommit() throws Exception {

        DbConnector x = new DbConnector();
        try {
            con =x.connect("jdbc:derby:memory:myInMemDB;create=true", null, null);

            Assert.assertFalse("connectionAutoCommit has auto commit on", con.getAutoCommit());

        } catch (Exception e) {
            throw new GretlException("Auto Commit on");
        }finally {
            if (con!=null){
                con.close();
            }
        }
    }
}