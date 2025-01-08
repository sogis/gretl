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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.ioxwkf.gpkg.GeoPackageReader;
import ch.interlis.ioxwkf.shp.ShapeReader;
import ch.interlis.ioxwkf.shp.ShapeWriter;

import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

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
            this.taskName = Gpkg2ShpStep.class.getSimpleName();
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
        Map<String, AttributeDescriptor[]> shpAttrsDescMap = new HashMap<String,AttributeDescriptor[]>();
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'");
                while (rs.next()) {
                    tableNames.add(rs.getString("tablename"));
                    log.lifecycle("tablename: " + rs.getString("tablename"));
                }
            }  catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }

            // Mapping von GPKG-Attribut-Descriptor zu SHP-Attribut-Descriptor.
            Map<String,String> stringTableNames = new HashMap<>();
            for (String tableName : tableNames) {
                List<GpkgAttributeDescriptor> gpkgAttrsDesc = GpkgAttributeDescriptor.getAttributeDescriptors(null, tableName, conn);

                // Ganz klar im Vergleich zur komplett schemalosen Variante (also Geometrie und String)
                // ist es mir hier nicht. Jedenfalls scheint es nicht zu funktionieren, wenn es kein
                // Geometrie-Attribut gibt. Aus diesem Grund faken wir eins.
                boolean geomColumnFound = false;
                for (GpkgAttributeDescriptor attrDescr : gpkgAttrsDesc) {
                    if (attrDescr.isGeometry()) {
                        geomColumnFound = true;
                    }
                }

                if (!geomColumnFound) {
                    GpkgAttributeDescriptor attr=new GpkgAttributeDescriptor();
                    attr.setDbColumnName("the_geom");
                    attr.setDbColumnType(Types.BLOB); // Es braucht einen DbColumnType. Weil aber "isGeometry()=true", spielt das im Ablauf keine Rolle. Siehe Code unten.
                    attr.setDbColumnTypeName("POINT");
                    attr.setCoordDimension(2);
                    attr.setSrId(2056);
                    attr.setDbColumnGeomTypeName("POINT"); // Braucht es, damit isGeometry()=true wird.
                    gpkgAttrsDesc.add(attr);
                }

                AttributeDescriptor shpAttrsDesc[] = new AttributeDescriptor[gpkgAttrsDesc.size()];
                for (int i=0; i<gpkgAttrsDesc.size(); i++) {
                    GpkgAttributeDescriptor gpkgAttrDesc = gpkgAttrsDesc.get(i);
                    AttributeTypeBuilder attributeBuilder = new AttributeTypeBuilder();
                    String attrName = gpkgAttrDesc.getIomAttributeName();
                    attributeBuilder.setName(attrName);
                    int dbColType = gpkgAttrDesc.getDbColumnType();

                    if(gpkgAttrDesc.isGeometry()) {
                        String geoColumnTypeName = gpkgAttrDesc.getDbColumnGeomTypeName();

                        if(geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_POINT)) {
                            attributeBuilder.setBinding(Point.class);
                        } else if(geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_MULTIPOINT)) {
                            attributeBuilder.setBinding(MultiPoint.class);
                        } else if(geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_LINESTRING) || geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_COMPOUNDCURVE)) {
                            attributeBuilder.setBinding(LineString.class);
                        } else if(geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_MULTILINESTRING) || geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_MULTICURVE)) {
                            attributeBuilder.setBinding(MultiLineString.class);
                        } else if(geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_POLYGON) || geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_CURVEPOLYGON)) {
                            attributeBuilder.setBinding(Polygon.class);
                        } else if(geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_MULTIPOLYGON) || geoColumnTypeName.equals(GpkgAttributeDescriptor.GEOMETRYTYPE_MULTISURFACE)) {
                            attributeBuilder.setBinding(MultiPolygon.class);
                        } else {
                            throw new IllegalStateException("unexpected geometry type "+geoColumnTypeName);
                        }

                        CoordinateReferenceSystem crs = null;

                        int srsId = gpkgAttrDesc.getSrId();
                        CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);
                        try {
                            crs = factory.createCoordinateReferenceSystem("EPSG:"+srsId);
                        } catch (NoSuchAuthorityCodeException e) {
                            throw new IoxException("coordinate reference: EPSG:"+srsId+" not found",e);
                        } catch (FactoryException e) {
                            throw new IoxException(e);
                        }
                        attributeBuilder.setCRS(crs);
                    } else if (dbColType == Types.INTEGER && gpkgAttrDesc.getDbColumnTypeName().equalsIgnoreCase("BOOLEAN")) {
                        // QGIS/OGR macht aus einem Boolean einen Integer.
                        // Geotools anscheinend einen Text (T/F).
                        // Verhalten muesste im ShpWriter geaendert werden.
                        attributeBuilder.setBinding(Boolean.class);
                    } else if(dbColType == Types.SMALLINT) {
                        attributeBuilder.setBinding(Integer.class);
                    } else if(dbColType == Types.TINYINT) {
                        attributeBuilder.setBinding(Integer.class);
                    } else if(dbColType == Types.INTEGER && !gpkgAttrDesc.getDbColumnTypeName().equalsIgnoreCase("BOOLEAN")) {
                        attributeBuilder.setBinding(Integer.class);
                    } else if(dbColType == Types.NUMERIC) {
                        attributeBuilder.setBinding(Double.class);
                    } else if(dbColType == Types.BIGINT) {
                        attributeBuilder.setBinding(Double.class);
                    } else if(dbColType == Types.FLOAT) {
                        attributeBuilder.setBinding(Double.class);
                    } else if(dbColType == Types.DOUBLE) {
                        attributeBuilder.setBinding(Double.class);
                    } else if(dbColType == Types.DATE || gpkgAttrDesc.getDbColumnTypeName().equalsIgnoreCase("DATE") || gpkgAttrDesc.getDbColumnTypeName().equalsIgnoreCase("DATETIME")) {
                        // In Sqlite ist DATE/DATETIME Text.
                        attributeBuilder.setBinding(java.util.Date.class);
                    } else {
                        attributeBuilder.setBinding(String.class);
                        stringTableNames.put(tableName, tableName);
                    }
                    attributeBuilder.setMinOccurs(0);
                    attributeBuilder.setMaxOccurs(1);
                    attributeBuilder.setNillable(true);
                    shpAttrsDesc[i] = attributeBuilder.buildDescriptor(attrName.toLowerCase());
                }

                shpAttrsDescMap.put(tableName, shpAttrsDesc);
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        // Convert (read -> write) tables
        for (String tableName : tableNames) {

            Settings settings = new Settings();
            settings.setValue(ShapeReader.ENCODING, "UTF8");

            ShapeWriter writer = new ShapeWriter(Paths.get(outputDir, tableName + ".shp").toFile(), settings);
            writer.setDefaultSridCode("2056");

            AttributeDescriptor[] attrsDesc = shpAttrsDescMap.get(tableName);
            writer.setAttributeDescriptors(attrsDesc);

            List<String> stringAttrs = new ArrayList<>();
            for (AttributeDescriptor attrDesc : attrsDesc) {
                if (attrDesc.getType().getBinding().equals(String.class)) {
                    stringAttrs.add(attrDesc.getName().getLocalPart());
                }
            }

            GeoPackageReader reader = new GeoPackageReader(new File(gpkgFile), tableName);

            IoxEvent event = reader.read();
            while (event instanceof IoxEvent) {
                if (event instanceof ObjectEvent) {
                    // Wenn der Wert eines Stringattributes nahe bei der maximalen Zeichenanzahl ist,
                    // kann es irgendwie zu einem Edgecase kommen "StringIndexOutOfBoundsException".
                    // Das Verkuerzen von zu langen Strings funktioniert eigentlich einwandfrei. 
                    // Wir verkuerzen nun selber mehr als notwendig (229) und fuegen auch zugleich
                    // den Hinweis hinzu, dass der Wert gekuerzt wurde. (Auch wenn er wohl nicht
                    // gelesen wird.)
                    // 2024-12-13: Scheint mir aktuellerer Geotools-Version nicht mehr ein Problem
                    // zu sein. Wir lassen es aber, da wir so klar machen, dass abgeschnitten wurde.
                    IomObject iomObj = ((ObjectEvent) event).getIomObject();
                    
                    for (String stringAttr : stringAttrs) {
                        String stringAttrValue = iomObj.getattrvalue(stringAttr);
                        if (stringAttrValue != null && stringAttrValue.length() > 240) {
                            String trimmedStringAttrValue = iomObj.getattrvalue(stringAttr).substring(0, 229) + " TRUNCATED!";
                            iomObj.setattrvalue(stringAttr, trimmedStringAttrValue);
                        }
                    }
                    writer.write(new ch.interlis.iox_j.ObjectEvent(iomObj));
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