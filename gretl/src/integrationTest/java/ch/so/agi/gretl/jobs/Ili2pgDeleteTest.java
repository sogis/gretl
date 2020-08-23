package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;

public class Ili2pgDeleteTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Test
    public void deleteDataset_Ok() throws Exception {
        Connection con = null;
        try {            
            
            System.out.println("url: " + postgres.getJdbcUrl());
            
            // create schema, import dataset and delete dataset
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};
            IntegrationTestUtil.runJob("src/integrationTest/jobs/Ili2pgDeleteDataset", gvs);
            
            
            
            
            // check results
            con = IntegrationTestUtilSql.connectPG(postgres);
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT count(*) AS anzahl FROM dm01.lfp3");

            if(!rs.next()) {
                fail();
            }

            assertTrue(rs.getInt(1)==0);
            
            if(rs.next()) {
                fail();
            }            
        } finally {
            IntegrationTestUtilSql.closeCon(con);
        }
    }

    private Config createConfig() {
        Config settings = new Config();
        new PgMain().initConfig(settings);
        return settings;
    }
}
