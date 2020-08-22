package ch.so.agi.gretl.steps;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.ioxwkf.gpkg.GeoPackageReader;
import ch.interlis.ioxwkf.shp.ShapeWriter;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class Gpgk2ShpStep {
    private GretlLogger log;
    private String taskName;

    public Gpgk2ShpStep() {
        this(null);
    }

    public Gpgk2ShpStep(String taskName) {
        if (taskName == null) {
            taskName = Gpgk2ShpStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    public void execute(String gpkgFile, String outputDir) throws IoxException {
        log.lifecycle(String.format("Start Gpgk2ShpStep(Name: %s GpkgFileName: %s OutputDir: %s)", taskName, gpkgFile,
                outputDir));

        // Get all geopackage tables that will be converted to shape file.
        List<String> tableNames = new ArrayList<String>();
        String url = "jdbc:sqlite:" + gpkgFile;
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt
                    .executeQuery("SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'")) {
                while (rs.next()) {
                    tableNames.add(rs.getString("tablename"));
                    log.lifecycle("tablename: " + rs.getString("tablename"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        // Convert (read -> write) tables
        for (String tableName : tableNames) {
            ShapeWriter writer = new ShapeWriter(Paths.get(outputDir, tableName + ".shp").toFile());
            writer.setDefaultSridCode("2056");

            GeoPackageReader reader = new GeoPackageReader(new File(gpkgFile), tableName);
            IoxEvent event = reader.read();
            while (event instanceof IoxEvent) {
                if (event instanceof ObjectEvent) {
                    writer.write(event);
                }
                event = reader.read();
            }

            writer.write(new EndBasketEvent());
            writer.write(new EndTransferEvent());

            if (writer != null) {
                writer.close();
                writer = null;
            }
            if (reader != null) {
                reader.close();
                reader = null;
            }
        }
    }

}
