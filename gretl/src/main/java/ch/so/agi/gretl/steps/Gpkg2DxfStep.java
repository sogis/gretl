package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.interlis2.av2geobau.impl.DxfUtil;

import ch.interlis.iox.IoxException;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class Gpkg2DxfStep {
    private GretlLogger log;
    private String taskName;

    public Gpkg2DxfStep() {
        this(null);
    }  
    
    public Gpkg2DxfStep(String taskName) {
        if (taskName == null) {
            taskName = Gpkg2DxfStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    public void execute(String gpkgFile, String dxfFile) throws IoxException {
        log.lifecycle(String.format("Start Gpkg2DxfStep(Name: %s GpkgFileName: %s DxfFileName: %s)", taskName, gpkgFile,
                dxfFile));
        
        // Get all geopackage tables that will be converted to shape file.
//        List<String> tableNames = new ArrayList<String>();
//        String url = "jdbc:sqlite:" + gpkgFile;
//        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
//            try (ResultSet rs = stmt
//                    .executeQuery("SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'")) {
//                while (rs.next()) {
//                    tableNames.add(rs.getString("tablename"));
//                    log.lifecycle("tablename: " + rs.getString("tablename"));
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//                throw new IllegalArgumentException(e.getMessage());
//            }
//        } catch (SQLException e) {
//            throw new IllegalArgumentException(e.getMessage());
//        }
        
        
        
        
    }
    
    private void writeBlocks(java.io.Writer fw) throws IOException { 
        // BLOCK (Symbole)             
        fw.write(DxfUtil.toString(0, "SECTION"));
        fw.write(DxfUtil.toString(2, "BLOCKS"));
        
        // GP Bolzen                
        fw.write(DxfUtil.toString(0, "BLOCK"));
        fw.write(DxfUtil.toString(8, "0"));
        fw.write(DxfUtil.toString(70, "0"));
        fw.write(DxfUtil.toString(10, "0.0"));
        fw.write(DxfUtil.toString(20, "0.0"));
        fw.write(DxfUtil.toString(30, "0.0"));
        fw.write(DxfUtil.toString(2, "GPBOL"));
        fw.write(DxfUtil.toString(0, "CIRCLE"));
        fw.write(DxfUtil.toString(8, "0"));
        fw.write(DxfUtil.toString(10, "0.0"));
        fw.write(DxfUtil.toString(20, "0.0"));
        fw.write(DxfUtil.toString(30, "0.0"));
        fw.write(DxfUtil.toString(40, "0.5"));
        fw.write(DxfUtil.toString(0, "ENDBLK"));
        fw.write(DxfUtil.toString(8, "0")); 

        fw.write(DxfUtil.toString(0, "ENDSEC"));
    }
    
    public class DxfLayerInfo {
        private String tableName;
        private String geomColumnName;
        private int crs;
        private String geometryTypeName;
        private String className;
        private String dxfLayerAttr;

        public String getTableName() {
            return tableName;
        }
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
        public String getGeomColumnName() {
            return geomColumnName;
        }
        public void setGeomColumnName(String geomColumnName) {
            this.geomColumnName = geomColumnName;
        }
        public int getCrs() {
            return crs;
        }
        public void setCrs(int crs) {
            this.crs = crs;
        }
        public String getGeometryTypeName() {
            return geometryTypeName;
        }
        public void setGeometryTypeName(String geometryTypeName) {
            this.geometryTypeName = geometryTypeName;
        }
        public String getClassName() {
            return className;
        }
        public void setClassName(String className) {
            this.className = className;
        }
        public String getDxfLayerAttr() {
            return dxfLayerAttr;
        }
        public void setDxfLayerAttr(String dxfLayerAttr) {
            this.dxfLayerAttr = dxfLayerAttr;
        }
    }    
}
