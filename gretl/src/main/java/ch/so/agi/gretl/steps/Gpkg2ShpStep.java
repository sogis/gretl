package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

public class Gpkg2ShpStep {
    private GretlLogger log;
    private String taskName;
    
    private static final String PRJ_CONTENT = "PROJCS[\"CH1903+_LV95\",GEOGCS[\"GCS_CH1903+\",DATUM[\"D_CH1903+\",SPHEROID[\"Bessel_1841\",6377397.155,299.1528128]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Hotine_Oblique_Mercator_Azimuth_Center\"],PARAMETER[\"False_Easting\",2600000.0],PARAMETER[\"False_Northing\",1200000.0],PARAMETER[\"Scale_Factor\",1.0],PARAMETER[\"Azimuth\",90.0],PARAMETER[\"Longitude_Of_Center\",7.43958333333333],PARAMETER[\"Latitude_Of_Center\",46.9524055555556],UNIT[\"Meter\",1.0]]";

    public Gpkg2ShpStep() {
        this(null);
    }

    public Gpkg2ShpStep(String taskName) {
        if (taskName == null) {
            taskName = Gpkg2ShpStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    public void execute(String gpkgFile, String outputDir) throws IoxException, FileNotFoundException {
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
            
            /* QGIS kann die Shapefiles nicht anzeigen. Sie koennen zwar geladen werden aber man sieht 
            * die Geometrien nie (die Attribute schon). Es liegt an einer prj-File-Inkompatibilitaet.
            * Aus diesem Grund wird es ueberschrieben mit einem Inhalt, der von einem QGIS-Shapefile 
            * stammt.
            */
            PrintWriter prw = new PrintWriter(Paths.get(outputDir, tableName + ".prj").toFile().getAbsolutePath());
            prw.println(PRJ_CONTENT);          
            prw.close();
        }
    }
}
