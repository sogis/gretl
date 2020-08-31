package ch.so.agi.gretl.steps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.interlis2.av2geobau.impl.DxfUtil;
import org.interlis2.av2geobau.impl.DxfWriter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jts;
import ch.interlis.iox_j.jts.Iox2jtsException;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox_j.jts.Jts2iox;

import ch.interlis.ioxwkf.gpkg.GeoPackageReader;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class Gpkg2DxfStep {
    private GretlLogger log;
    private String taskName;

    private static final String COORD="COORD";
    private static final String MULTICOORD="MULTICOORD";
    private static final String POLYLINE="POLYLINE";
    private static final String MULTIPOLYLINE="MULTIPOLYLINE";
    private static final String MULTISURFACE="MULTISURFACE";

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
    
    public void execute(String gpkgFile, String outputDir) throws Exception {
        log.lifecycle(String.format("Start Gpkg2DxfStep(Name: %s GpkgFileName: %s OutputDir: %s)", taskName, gpkgFile,
                outputDir));
        
        // Get all geopackage tables that will be converted to dxf files.
        String sql = "SELECT \n" + 
                "    table_prop.tablename, \n" + 
                "    gpkg_geometry_columns.column_name,\n" + 
                "    gpkg_geometry_columns.srs_id AS crs,\n" + 
                "    gpkg_geometry_columns.geometry_type_name AS geometry_type_name,\n" + 
                "    classname.IliName AS classname,\n" + 
                "    attrname.SqlName AS dxf_layer_attr\n" + 
                "FROM \n" + 
                "    T_ILI2DB_TABLE_PROP AS table_prop\n" + 
                "    LEFT JOIN gpkg_geometry_columns\n" + 
                "    ON table_prop.tablename = gpkg_geometry_columns.table_name\n" + 
                "    LEFT JOIN T_ILI2DB_CLASSNAME AS classname\n" + 
                "    ON table_prop.tablename = classname.SqlName \n" + 
                "    LEFT JOIN ( SELECT ilielement, attr_name, attr_value FROM T_ILI2DB_META_ATTRS WHERE attr_name = 'dxflayer' ) AS meta_attrs \n" + 
                "    ON instr(meta_attrs.ilielement, classname) > 0\n" + 
                "    LEFT JOIN T_ILI2DB_ATTRNAME AS attrname \n" + 
                "    ON meta_attrs.ilielement = attrname.IliName \n" + 
                "WHERE\n" + 
                "    setting = 'CLASS'\n" + 
                "    AND \n" + 
                "    column_name IS NOT NULL";
        
        List<DxfLayerInfo> dxfLayers = new ArrayList<DxfLayerInfo>();
        String url = "jdbc:sqlite:" + gpkgFile;
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()) {
                    DxfLayerInfo dxfLayerInfo = new DxfLayerInfo();
                    dxfLayerInfo.setTableName(rs.getString("tablename"));
                    dxfLayerInfo.setGeomColumnName(rs.getString("column_name"));
                    dxfLayerInfo.setCrs(rs.getInt("crs"));
                    dxfLayerInfo.setGeometryTypeName(rs.getString("geometry_type_name"));
                    dxfLayerInfo.setClassName(rs.getString("classname"));
                    dxfLayerInfo.setDxfLayerAttr(rs.getString("dxf_layer_attr"));
                    dxfLayers.add(dxfLayerInfo);
                    log.info(dxfLayerInfo.getTableName());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        GeometryFactory geometryFactory = new GeometryFactory();

        for (DxfLayerInfo dxfLayerInfo : dxfLayers) {
            String tableName = dxfLayerInfo.getTableName();
            String geomColumnName = dxfLayerInfo.getGeomColumnName();
            int crs = dxfLayerInfo.getCrs();
            String geometryTypeName = dxfLayerInfo.getGeometryTypeName();
            String dxfLayerAttr = dxfLayerInfo.getDxfLayerAttr();
            
            String dxfFileName = Paths.get(outputDir, tableName + ".dxf").toFile().getAbsolutePath();
            java.io.Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dxfFileName), "ISO-8859-1")); 
            log.info("dxfFile: " + dxfFileName);
            
            try {
                writeBlocks(fw);
                fw.write(DxfUtil.toString(0, "SECTION"));
                fw.write(DxfUtil.toString(2, "ENTITIES"));
                
                GeoPackageReader reader = new GeoPackageReader(new File(gpkgFile), tableName);        
                IoxEvent event = reader.read();
                while (event instanceof IoxEvent) {
                    if (event instanceof ObjectEvent) {                
                        ObjectEvent iomObjEvent = (ObjectEvent) event;
                        IomObject iomObj = iomObjEvent.getIomObject();
                        
                        String layer;
                        if (dxfLayerAttr != null) {
                            layer = iomObj.getattrvalue(dxfLayerAttr);
                            layer = layer.replaceAll("\\s+","");
                        } else {
                            layer = "default";
                        }
                        IomObject iomGeom = iomObj.getattrobj(geomColumnName, 0);
                                                
                        Geometry jtsGeom;
                        if (iomGeom.getobjecttag().equals(MULTISURFACE)) {
                            jtsGeom = Iox2jts.multisurface2JTS(iomGeom, 0, crs);
                            
                            for (int i=0; i<jtsGeom.getNumGeometries(); i++) {
                                IomObject dxfObj = new Iom_jObject(DxfWriter.IOM_2D_POLYGON, null);
                                dxfObj.setobjectoid(iomObj.getobjectoid());
                                dxfObj.setattrvalue(DxfWriter.IOM_ATTR_LAYERNAME, layer);
                                
                                Polygon poly = (Polygon) jtsGeom.getGeometryN(i);
                                IomObject surface = Jts2iox.JTS2surface(poly);
                                dxfObj.addattrobj(DxfWriter.IOM_ATTR_GEOM, surface);
                                String dxfFragment = DxfWriter.feature2Dxf(dxfObj);
                                fw.write(dxfFragment);
                            }
                        } else if (iomGeom.getobjecttag().equals(MULTIPOLYLINE)) {
                            jtsGeom = Iox2jts.multipolyline2JTS(iomGeom, 0);
                            
                            for (int i=0; i<jtsGeom.getNumGeometries(); i++) {
                                IomObject dxfObj = new Iom_jObject(DxfWriter.IOM_2D_POLYLINE, null);
                                dxfObj.setobjectoid(iomObj.getobjectoid());
                                dxfObj.setattrvalue(DxfWriter.IOM_ATTR_LAYERNAME, layer);
                                
                                LineString line = (LineString) jtsGeom.getGeometryN(i);
                                IomObject polyline = Jts2iox.JTS2polyline(line);
                                dxfObj.addattrobj(DxfWriter.IOM_ATTR_GEOM, polyline);
                                String dxfFragment = DxfWriter.feature2Dxf(dxfObj);
                                fw.write(dxfFragment);
                            }                            
                        } else if (iomGeom.getobjecttag().equals(MULTICOORD)) {
                            jtsGeom = Iox2jts.multicoord2JTS(iomGeom);
                            
                            for (int i=0; i<jtsGeom.getNumGeometries(); i++) {
                                IomObject dxfObj = new Iom_jObject(DxfWriter.IOM_BLOCKINSERT, null);
                                dxfObj.setobjectoid(iomObj.getobjectoid());
                                dxfObj.setattrvalue(DxfWriter.IOM_ATTR_LAYERNAME, layer);
                                
                                Point point = (Point) jtsGeom.getGeometryN(i);
                                IomObject coord = Jts2iox.JTS2coord(point.getCoordinate());
                                dxfObj.addattrobj(DxfWriter.IOM_ATTR_GEOM, coord);
                                String dxfFragment = DxfWriter.feature2Dxf(dxfObj);
                                fw.write(dxfFragment);
                            }                            
                        } else if (iomGeom.getobjecttag().equals(POLYLINE)) {
                            CoordinateList coordList = Iox2jts.polyline2JTS(iomGeom, false, 0);
                            Coordinate[] coordArray = new Coordinate[coordList.size()];
                            coordArray = (Coordinate[]) coordList.toArray(coordArray);
                            jtsGeom = geometryFactory.createLineString(coordArray);
                            
                            IomObject dxfObj = new Iom_jObject(DxfWriter.IOM_2D_POLYLINE, null);
                            dxfObj.setobjectoid(iomObj.getobjectoid());
                            dxfObj.setattrvalue(DxfWriter.IOM_ATTR_LAYERNAME, layer);

                            IomObject polyline = Jts2iox.JTS2polyline((LineString)jtsGeom);
                            dxfObj.addattrobj(DxfWriter.IOM_ATTR_GEOM, polyline);
                            String dxfFragment = DxfWriter.feature2Dxf(dxfObj);
                            fw.write(dxfFragment);
                        } else if (iomGeom.getobjecttag().equals(COORD)) {
                            Coordinate coord = Iox2jts.coord2JTS(iomGeom);
                            jtsGeom = geometryFactory.createPoint(coord);
                            
                            IomObject dxfObj = new Iom_jObject(DxfWriter.IOM_BLOCKINSERT, null);
                            dxfObj.setobjectoid(iomObj.getobjectoid());
                            dxfObj.setattrvalue(DxfWriter.IOM_ATTR_LAYERNAME, layer);
                            dxfObj.setattrvalue(DxfWriter.IOM_ATTR_BLOCK, "GPBOL");
                            
                            IomObject iomCoord = Jts2iox.JTS2coord(coord);
                            dxfObj.addattrobj(DxfWriter.IOM_ATTR_GEOM, iomCoord);
                            String dxfFragment = DxfWriter.feature2Dxf(dxfObj);
                            fw.write(dxfFragment); 
                        } else {
                            continue;
                        }
                    }
                    event = reader.read();
                }
                            
                if (reader != null) {
                    reader.close();
                    reader = null;
                }

                fw.write(DxfUtil.toString(0, "ENDSEC"));
                fw.write(DxfUtil.toString(0, "EOF"));
            } finally{
                if(fw != null) {
                    fw.close();
                    fw=null;
                }
            }            
        }
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
