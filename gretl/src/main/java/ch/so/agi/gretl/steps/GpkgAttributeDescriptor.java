package ch.so.agi.gretl.steps;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import ch.interlis.iox.IoxException;

/**<b>AttributeDescriptor</b>
 * <p>
 * 
 * <b>The main task</b><br>
 * Describes an attribute and shows possibilities to set.<br>
 * <p>
 * 
 * <b>Create a new AttributeDescriptor</b><br>
 * <li>AttributeDescriptor attrDesc= new AttributeDescriptor()</li>
 * <p>
 * 
 * <b>(Optional) Setting possibilities</b><br>
 * <table border="1">
 * <tr>
 *   <th>Setting Name</th>
 *   <th>Description</th>
 *   <th>Example</th>
 * </tr>
 * <tr>
 *   <td>iomAttributeName</td>
 *   <td>
 *   	An Attribute of IomObject consists of an Attributename and a Value.
 *   	In this case, the Value could be a String or an Object.<p>
 *   	
 *  	example:
 *   	<li>IomObject iomObj.setattrvalue("attributeName", "attributeValue");</li>
 *   	<li>IomObject iomObj.addattrobj("AttributeName", "AttributeObject");</li>
 *   	<p>
 *		condition:
 *   	<li>If iomAttributeName not set, dbColumnName will be used.</li>
 *   	<li>If iomAttributeName is set, iomAttributeName will be used.</li>
 *   </td>
 *   <td>
 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
 *  	attrDesc.setIomAttributeName(String attributeName)
 *   </td>
 * </tr>
 * <tr>
 *   <td>dbColumnType</td>
 *   <td>
 *   	The type (integer, not the name) of the column in the data base table.<br>
 * 		requirements:<br>
 * 		<li>has to be a type of: java.sql.Types (see: Attachement)</li>
 *   </td>
 *   <td>
 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
 *  	attrDesc.setDbColumnType(Integer attributeType)
 *  </td>
 * </tr>
 * <tr>
 *   <td>dbColumnName</td>
 *   <td>The name (String) of the column in the data base table</td>
 *   <td>
 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
 *  	attrDesc.setDbColumnName(String attributeName)
 *  </td>
 * </tr>
 * <tr>
 *   <td>dbColumnTypeName</td>
 *   <td>The typeName (the name of the integer type) of the column in the data base table.<br>
 *   <p>
 *		dbColumnTypeNames:<br>
 *		<li>DBCOLUMN_TYPENAME_BOOL</li>
 *		<li>DBCOLUMN_TYPENAME_XML</li>
 *		<li>DBCOLUMN_TYPENAME_UUID</li>
 *		<li>DBCOLUMN_TYPENAME_GEOMETRY</li>
 *	 </td>
 *   <td>
 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
 *  	attrDesc.setDbColumnTypeName(String attributeTypeName)
 *  	<p>
 *  	attrDesc.setDbColumnTypeName(AttributeDescriptor.DBCOLUMN_TYPENAME_BOOL)<br>
 *  </td>
 * </tr>
 * <tr>
 *   <td>dbColumnGeomTypeName</td>
 *   <td>
 *   	The typeName (String) of the column of the data base table: geometry_columns.<br>
 * 		 <p>
 * 		 requirements:
 * 		 <li>isGeometry() has to be true</li>
 * 		 <p>
 * 		 dbColumnGeomTypeNames:<br>
 * 		 <li>GEOMETRYTYPE_MULTIPOLYGON</li>
 * 		 <li>GEOMETRYTYPE_POLYGON</li>
 *		 <li>GEOMETRYTYPE_MULTILINESTRING</li>
 * 		 <li>GEOMETRYTYPE_LINESTRING</li>
 * 		 <li>GEOMETRYTYPE_MULTIPOINT</li>
 * 		 <li>GEOMETRYTYPE_POINT</li>
 *	</td>
 *  <td>
 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
 *  	attrDesc.setDbColumnGeomTypeName(String dbColumnGeomTypeName)<br>
 *  	attrDesc.setDbColumnGeomTypeName(AttributeDescriptor.GEOMETRYTYPE_MULTIPOLYGON)<br>
 *  </td>
 * </tr>
 * <tr>
 *   <td>coordDimension</td>
 *   <td>The dimension of the coordinates that define this Geometry.
 *   	<p>
 *      requirements:<br>
 * 		<li>isGeometry() has to be true</li><br>
 * 		coordDimenstion can be found in table geometry_columns.
 *   </td>
 *   <td>
 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
 *  	attrDesc.setCoordDimension(Integer coordDimension)
 *  </td>
 * </tr>
 * <tr>
 *   <td>srId</td>
 *   <td>The srId is an integer that uniquely identifies the Spatial Referencing System (SRS) within the database.
 *   	<p>
 *	 	requirements:<br>
 *      <li>isGeometry() has to be true</li><br>
 *      srid can be found in table geometry_columns.
 *   </td>
 *   <td>
 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
 *  	attrDesc.setSrId(Integer srId)
 *  </td>
 * </tr>
 * <tr>
 *   <td>precision</td>
 *   <td>Precision is the number of digits in the not scaled value.</td>
 *   <td>
 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
 *  	attrDesc.setPrecision(Integer precision)
 *   </td>
 * </tr>
 * </table>
 * <p>
 * 
 * <b>Attachement</b><br>
 * <li><a href="https://www.esri.com/library/whitepapers/pdfs/shapefile.pdf">Shapespecification</a></li>
 * <li><a href="https://docs.oracle.com/javase/6/docs/api/java/sql/Types.html">java.sql.Types</a></li>
 */
public class GpkgAttributeDescriptor {
	private String dbColumnName=null;
	private String iomAttributeName=null;
	private Integer attributeType=null;
	private String attributeTypeName=null;
	private String dbColumnGeomTypeName=null;
	private Integer coordDimension=null;
	private Integer srId=null;
	private Integer precision=null;
	private Boolean mandatory=null;
	private String columnRemarks=null;
	private String targetTableName=null;
	private String referenceColumnName=null;
	private String attributeDefinition=null;

	public static final String JDBC_GETCOLUMNS_FKCOLUMNNAME="FKCOLUMN_NAME";
	public static final String JDBC_GETCOLUMNS_PKTABLENAME="PKTABLE_NAME";
	/** The typeName bool is an alias of boolean type.
	 * <p>
	 * get type:<br>
	 * <li>getDbColumnTypeName()</li>
	 */
	public final static String DBCOLUMN_TYPENAME_BOOL="bool";
	
	// JDBC/DB column type name if java.sql.Types.OTHER
	/** xml is a name of JDBC/DB column type.
	 * <p>
	 * requirements:
     * <li>has to be type of: 'java.sql.Types.OTHER'</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnTypeName()</li>
	 */
	public final static String DBCOLUMN_TYPENAME_XML="xml";
	/** uuid is a name of JDBC/DB column type.<br>
	 * the uuid (Universally Unique Identifier) references to the ISO 11578.
	 * <p>
	 * requirements:
     * <li>has to be type of: 'java.sql.Types.OTHER'</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnTypeName()</li>
	 */
	public final static String DBCOLUMN_TYPENAME_UUID="uuid";
	/** geometry is the name of JDBC/DB column type to identify the dataType as geometry.
	 * <p>
	 * requirements:
     * <li>has to be type of: 'java.sql.Types.OTHER'</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnTypeName()</li>
	 */
	public final static String DBCOLUMN_TYPENAME_GEOMETRY="geometry";
	// geometry types
	/** multiPolygon is the name of a type that is used for a multiPolygon.<br>
	 * multiPolygon is a collection of Polygons.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnGeomTypeName()</li>
	 */
	public final static String GEOMETRYTYPE_MULTIPOLYGON="MULTIPOLYGON";
	/** polygon is the name of a type that is used for a polygon.<br>
	 * a polygon is a shape with linear edges, which includes shell and may includes holes.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnGeomTypeName()</li>
	 */
	public final static String GEOMETRYTYPE_POLYGON="POLYGON";
	/** multiLineString is the name of a type that is used for a multiLineString.<br>
	 * a multiLineString could be one or more LineStrings.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnGeomTypeName()</li>
	 */
	public final static String GEOMETRYTYPE_MULTILINESTRING="MULTILINESTRING";
	/** lineString is the name of a type that is used for a lineString.<br>
	 * lineString is a sequence of line segments.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnGeomTypeName()</li>
	 */
	public final static String GEOMETRYTYPE_LINESTRING="LINESTRING";
	/** multiPoint is the name of a type that is used for a multiPoint.<br>
	 * multipoint is a geometric object consisting of one or more points.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnGeomTypeName()</li>
	 */
	public final static String GEOMETRYTYPE_MULTIPOINT="MULTIPOINT";
	/** point is the name of a type that is used for a point.<br>
	 * point is a geometric object consisting of one point.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li>
     * <p>
	 * get type:<br>
	 * <li>getDbColumnGeomTypeName()</li>
	 */
	public final static String GEOMETRYTYPE_POINT="POINT";
	
	// Falls die GPKG-Datei nicht mit --strokeArcs erzeugt wird, werden in der
	// Tabelle "gpkg_geometry_columns" im Attribut "geometry_type_name" nicht 
	// die bekannten Standardgeometrietypen verwendet, sondern ili/iox-spezifische
	// oder "ogc/iso curve"-Typen. Die Typen werden beim Schreibprozess (IoxWriter)
	// verwendet, wenn man setAttrDesc verwendet. Sie muessen vorgaengig vom Quelltyp
	// in den Zieltyp gemappt werden. Z.B. POLYGON (Gpkg) -> Polygon.class (Shp).

    public final static String GEOMETRYTYPE_COMPOUNDCURVE = "COMPOUNDCURVE"; // Linestring
    public final static String GEOMETRYTYPE_MULTICURVE = "MULTICURVE"; // MultiLinestring
	
	public final static String GEOMETRYTYPE_CURVEPOLYGON = "CURVEPOLYGON"; // Polygon
	public final static String GEOMETRYTYPE_MULTISURFACE = "MULTISURFACE"; // MultiPolygon
	
	private final static List<String> GEOMETRYTYPES = new ArrayList<String>() {{
        add(GEOMETRYTYPE_POINT);
        add(GEOMETRYTYPE_LINESTRING);
        add(GEOMETRYTYPE_POLYGON);
        add(GEOMETRYTYPE_MULTIPOINT);
        add(GEOMETRYTYPE_MULTILINESTRING);
        add(GEOMETRYTYPE_MULTIPOLYGON);
        add(GEOMETRYTYPE_COMPOUNDCURVE);
        add(GEOMETRYTYPE_MULTICURVE);
        add(GEOMETRYTYPE_CURVEPOLYGON);
        add(GEOMETRYTYPE_COMPOUNDCURVE);
        add(GEOMETRYTYPE_MULTISURFACE);
	}};

	private final static String GEOMCOLUMNS_COLUMN_TYPE="geometry_type_name";
	private final static String GEOMCOLUMNS_COLUMN_SRID="srs_id";
	private final static String GEOMCOLUMNS_COLUMN_DIMENSION="z";
	
	public final static String JDBC_GETCOLUMNS_REMARKS="REMARKS";
	public final static String JDBC_GETCOLUMNS_COLUMNNAME="COLUMN_NAME";
	public final static String JDBC_GETCOLUMNS_DATATYPE="DATA_TYPE";
	public final static String JDBC_GETCOLUMNS_TYPENAME="TYPE_NAME";
	public final static String JDBC_GETCOLUMNS_ISNULLABLE="IS_NULLABLE";
	public final static String JDBC_GETCOLUMNS_ISNULLABLE_YES="YES";
	public final static String JDBC_GETCOLUMNS_ISNULLABLE_NO="NO";
	
	/** table name that this the scope of a reference attribute (null if the DATA_TYPE isn't REF)
	 */
	public final static String JDBC_GETCOLUMNS_SCOPETABLE="SCOPE_TABLE";
	/** the COLUMN_SIZE column the specified column size for the given column.
	 * for numeric data, this is the maximum PRECISION.
	 */
	public final static String JDBC_GETCOLUMNS_PRECISION="COLUMN_SIZE";
	
	/**  <td>The name (String) of the column in the data base table</td>
	 *   <p>
	 *   <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setDbColumnName(String attributeName)
	 *   </td>
	 *   <p>
	 *   @return dbColumnName
	 */
	public String getDbColumnName() {
		return dbColumnName;
	}
	/**  <td>The name (String) of the column in the data base table</td>
	 *   <p>
	 *   <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setDbColumnName(String attributeName)
	 *   </td>
	 *   <p>
	 *   @param attributeName
	 */
	public void setDbColumnName(String attributeName) {
		this.dbColumnName = attributeName;
	}
	/**  <td>
	 *   	An Attribute of IomObject consists of an Attributename and a Value.
	 *   	In this case, the Value could be a String or an Object.<p>
	 *   	
	 *  	example:
	 *   	<li>IomObject iomObj.setattrvalue("attributeName", "attributeValue");</li>
	 *   	<li>IomObject iomObj.addattrobj("AttributeName", "AttributeObject");</li>
	 *   	<p>
	 *		condition:
	 *   	<li>If iomAttributeName not set, dbColumnName will be used.</li>
	 *   	<li>If iomAttributeName is set, iomAttributeName will be used.</li>
	 *   </td>
	 *   <p>
	 *   <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setIomAttributeName(String attributeName)
	 *   </td>
	 *   @return iomAttributeName if !=null or dbColumnName
	 */
	public String getIomAttributeName() {
		return iomAttributeName==null? dbColumnName : iomAttributeName;
	}
	/**  <td>
	 *   	An Attribute of IomObject consists of an Attributename and a Value.
	 *   	In this case, the Value could be a String or an Object.<p>
	 *   	
	 *  	example:
	 *   	<li>IomObject iomObj.setattrvalue("attributeName", "attributeValue");</li>
	 *   	<li>IomObject iomObj.addattrobj("AttributeName", "AttributeObject");</li>
	 *   	<p>
	 *		condition:
	 *   	<li>If iomAttributeName not set, dbColumnName will be used.</li>
	 *   	<li>If iomAttributeName is set, iomAttributeName will be used.</li>
	 *   </td>
	 *   <p>
	 *   <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setIomAttributeName(String attributeName)
	 *   </td>
	 *   @param attributeName
	 */
	public void setIomAttributeName(String attributeName) {
		this.iomAttributeName = attributeName;
	}
	/** get the type of JDBC/DB column.
	 * <p>
	 * requirements:
     * <li>has to be a type of: <a href="https://docs.oracle.com/javase/6/docs/api/java/sql/Types.html">java.sql.Types</a></li>
     * <p>
	 * @return attributeType
	 */
	public Integer getDbColumnType() {
		return attributeType;
	}
	/** set data base column type.
	 * <p>
	 * requirements:
     * <li>has to be a type of: <a href="https://docs.oracle.com/javase/6/docs/api/java/sql/Types.html">java.sql.Types</a></li>
     * <p>
	 * @param attributeType
	 */
	public void setDbColumnType(Integer attributeType) {
		this.attributeType = attributeType;
	}
	/** <td>The typeName (the name of the integer type) of the column in the data base table.<br>
	 *  <p>
	 *		dbColumnTypeNames:<br>
	 *		<li>DBCOLUMN_TYPENAME_BOOL</li>
	 *		<li>DBCOLUMN_TYPENAME_XML</li>
	 *		<li>DBCOLUMN_TYPENAME_UUID</li>
	 *		<li>DBCOLUMN_TYPENAME_GEOMETRY</li>
	 *	</td>
	 *	<p>
	 *  <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setDbColumnTypeName(String attributeTypeName)
	 *  	<p>
	 *  	attrDesc.setDbColumnTypeName(AttributeDescriptor.DBCOLUMN_TYPENAME_BOOL)<br>
	 *  </td>
	 *  <p>
	 *  requirements:
     *  <li>isGeometry() has to be true</li>
     *  <p>
	 *  @return attributeTypeName
	 */
	public String getDbColumnTypeName() {
		return attributeTypeName;
	}
	/** <td>The typeName (the name of the integer type) of the column in the data base table.<br>
	 *  <p>
	 *		dbColumnTypeNames:<br>
	 *		<li>DBCOLUMN_TYPENAME_BOOL</li>
	 *		<li>DBCOLUMN_TYPENAME_XML</li>
	 *		<li>DBCOLUMN_TYPENAME_UUID</li>
	 *		<li>DBCOLUMN_TYPENAME_GEOMETRY</li>
	 *	</td>
	 *	<p>
	 *  <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setDbColumnTypeName(String attributeTypeName)
	 *  	<p>
	 *  	attrDesc.setDbColumnTypeName(AttributeDescriptor.DBCOLUMN_TYPENAME_BOOL)<br>
	 *  </td>
	 *  <p>
	 *  requirements:
     *  <li>isGeometry() has to be true</li>
     *  <p>
	 *  @param attributeTypeName
	 */
	public void setDbColumnTypeName(String attributeTypeName) {
		this.attributeTypeName = attributeTypeName;
	}
	/** <td>
	 *   	The typeName (String) of the column of the data base table: geometry_columns.<br>
	 * 		 <p>
	 * 		 requirements:
	 * 		 <li>isGeometry() has to be true</li>
	 * 		 <p>
	 * 		 dbColumnGeomTypeNames:<br>
	 * 		 <li>GEOMETRYTYPE_MULTIPOLYGON</li>
	 * 		 <li>GEOMETRYTYPE_POLYGON</li>
	 *		 <li>GEOMETRYTYPE_MULTILINESTRING</li>
	 * 		 <li>GEOMETRYTYPE_LINESTRING</li>
	 * 		 <li>GEOMETRYTYPE_MULTIPOINT</li>
	 * 		 <li>GEOMETRYTYPE_POINT</li>
	 *	 </td>
	 *	 <p>
	 *   <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setDbColumnGeomTypeName(String dbColumnGeomTypeName)<br>
	 *  	attrDesc.setDbColumnGeomTypeName(AttributeDescriptor.GEOMETRYTYPE_MULTIPOLYGON)<br>
	 *   </td>
     * 	 @return dbColumnGeomTypeName
	 */
	public String getDbColumnGeomTypeName() {
		return dbColumnGeomTypeName;
	}
	/**  <td>
	 *   	The typeName (String) of the column of the data base table: geometry_columns.<br>
	 * 		 <p>
	 * 		 requirements:
	 * 		 <li>isGeometry() has to be true</li>
	 * 		 <p>
	 * 		 dbColumnGeomTypeNames:<br>
	 * 		 <li>GEOMETRYTYPE_MULTIPOLYGON</li>
	 * 		 <li>GEOMETRYTYPE_POLYGON</li>
	 *		 <li>GEOMETRYTYPE_MULTILINESTRING</li>
	 * 		 <li>GEOMETRYTYPE_LINESTRING</li>
	 * 		 <li>GEOMETRYTYPE_MULTIPOINT</li>
	 * 		 <li>GEOMETRYTYPE_POINT</li>
	 *	 </td>
	 *	 <p>
	 *   <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setDbColumnGeomTypeName(String dbColumnGeomTypeName)<br>
	 *  	attrDesc.setDbColumnGeomTypeName(AttributeDescriptor.GEOMETRYTYPE_MULTIPOLYGON)<br>
	 *   </td>
	 *   <p>
     * 	 @param dbColumnGeomTypeName
	 */
	public void setDbColumnGeomTypeName(String dbColumnGeomTypeName) {
		this.dbColumnGeomTypeName = dbColumnGeomTypeName;
	}
	/** get the coordinate dimension.<br>
	 * the dimension of the coordinates that define this Geometry.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li>
     * coordDimenstion can be found in table geometry_columns.
     * @return coordDimension
	 */
	public Integer getCoordDimension() {
		return coordDimension;
	}
	/** set the coordinate dimension.<br>
	 * the dimension of the coordinates that define this Geometry.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li><br>
     * coordDimenstion can be found in table geometry_columns.
     * @param coordDimension
	 */
	public void setCoordDimension(Integer coordDimension) {
		this.coordDimension = coordDimension;
	}
	/** get the srid.<br>
	 * the srId is an integer value that uniquely identifies the Spatial Referencing System (SRS) within the database.
	 * <p>
	 * requirements:
     * <li>isGeometry() has to be true</li><br>
     * srid can be found in table geometry_columns.
     * @return srId
	 */
	public Integer getSrId() {
		return srId;
	}
	/** <td>The srId is an integer that uniquely identifies the Spatial Referencing System (SRS) within the database.
	 *   	<p>
	 *	 	requirements:<br>
	 *      <li>isGeometry() has to be true</li><br>
	 *      srid can be found in table geometry_columns.
	 *  </td>
	 *  <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setSrId(Integer srId)
	 *  </td>
     * @param srId
	 */
	public void setSrId(Integer srId) {
		this.srId = srId;
	}
	/**  <td>get the designated column's specified column size. For numeric data, this is the maximum precision.
	 *   </td>
	 *   <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setPrecision(Integer precision)
	 *   </td>
	 * @return precision
	 */
	public Integer getPrecision() {
		return precision;
	}
	/**  <td>get the designated column's specified column size. For numeric data, this is the maximum precision.
	 *   </td>
	 *   <td>
	 *  	AttributeDescriptor attrDesc= new AttributeDescriptor()<br>
	 *  	attrDesc.setPrecision(Integer precision)
	 *   </td>
	 * @param precision
	 */
	public void setPrecision(Integer precision) {
		this.precision = precision;
	}
	
	/** add geometry data to geometry attribute in attribute descriptors.
	 * @param schemaName
	 * @param tableName
	 * @param attributeDesc
	 * @param db
	 * @return final list of attribute descriptors
	 * @throws SQLException 
	 * @throws IoxException 
	 */
	// Im Gegensatz zur PostGIS liefert GPKG eigentlich schon den Geometrietyp mit ResultSetMetaData. Es wird aber auch noch der SRID
	// und die Dimension benoetigt.
	public static List<GpkgAttributeDescriptor> addGeomDataToAttributeDescriptors(String schemaName, String tableName, List<GpkgAttributeDescriptor> attributeDesc, Connection db) throws SQLException, IoxException {
		for(GpkgAttributeDescriptor attr:attributeDesc) {
			if(GEOMETRYTYPES.contains(attr.getDbColumnTypeName())) {
				ResultSet tableInDb =null;
				StringBuilder queryBuild=new StringBuilder();
				queryBuild.append("SELECT "+GEOMCOLUMNS_COLUMN_DIMENSION+","+GEOMCOLUMNS_COLUMN_SRID+","+ GEOMCOLUMNS_COLUMN_TYPE+" FROM gpkg_geometry_columns WHERE ");
				queryBuild.append("table_name='"+tableName+"';"); 
				try {
					Statement stmt = db.createStatement();
					tableInDb=stmt.executeQuery(queryBuild.toString());
				} catch (SQLException e) {
					throw new SQLException(e);
				}
				
				tableInDb.next();
				
				// PostGIS verwaltet die Dimension. GPKG nur, ob ein z-Wert vorhanden ist.
				// 2 plus 0 oder 1 ergibt die Dimension.
				attr.setCoordDimension(2 + tableInDb.getInt(GEOMCOLUMNS_COLUMN_DIMENSION));
				attr.setSrId(tableInDb.getInt(GEOMCOLUMNS_COLUMN_SRID));
				attr.setDbColumnGeomTypeName(tableInDb.getString(GEOMCOLUMNS_COLUMN_TYPE));
			}
		}
		return attributeDesc;
	}
	
	/** create selection to table inside schema, create and return a list of attribute descriptors.
	 * @param schemaName
	 * @param tableName
	 * @param db
	 * @return list of attribute descriptors.
	 * @throws IoxException
	 */
	public static List<GpkgAttributeDescriptor> getAttributeDescriptors(String schemaName, String tableName, Connection db) throws IoxException {
		List<GpkgAttributeDescriptor> attrs=new ArrayList<GpkgAttributeDescriptor>();
		PreparedStatement ps=null;
		ResultSet rs =null;
		StringBuilder queryBuild=new StringBuilder();
		queryBuild.append("SELECT * FROM ");
		if(schemaName!=null) {
			queryBuild.append(schemaName+".");
		}
		queryBuild.append(tableName+" WHERE 1<>1;");
		try {
			ps=db.prepareStatement(queryBuild.toString());
			ps.clearParameters();
			rs = ps.executeQuery();
			if(rs==null) {
				throw new IoxException("table "+schemaName+"."+tableName+" not found");
			}
		} catch (SQLException e) {
			throw new IoxException(e);
		}
		DatabaseMetaData md;
		try {
			md = db.getMetaData();
		} catch (SQLException e2) {
			throw new IoxException(e2);
		}
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			rs = md.getColumns(null, schemaName, tableName, "%");
			for(int k=1;k<rsmd.getColumnCount()+1;k++) {
				rs.next();
				// create attr descriptor
				GpkgAttributeDescriptor attr=new GpkgAttributeDescriptor();
				attr.setColumnRemarks(rs.getString(JDBC_GETCOLUMNS_REMARKS));
				attr.setPrecision(rs.getInt(JDBC_GETCOLUMNS_PRECISION));
				attr.setDbColumnName(rs.getString(JDBC_GETCOLUMNS_COLUMNNAME));
				attr.setDbColumnType(rs.getInt(JDBC_GETCOLUMNS_DATATYPE));
				attr.setDbColumnTypeName(rs.getString(JDBC_GETCOLUMNS_TYPENAME));
				// YES: can include NULLs, else NO.
				String nullable = rs.getString(JDBC_GETCOLUMNS_ISNULLABLE);
				if(nullable.equals(JDBC_GETCOLUMNS_ISNULLABLE_YES)) {
					attr.setMandatory(false);
				}else {
					attr.setMandatory(true);
				}
				attrs.add(attr);
			}
		} catch (SQLException e) {
			throw new IoxException(e);
		}
		try {
			addGeomDataToAttributeDescriptors(schemaName, tableName, attrs, db);
		}catch(SQLException e) {
			throw new IoxException(e);
		}
		return attrs;
	}
	
	/** check if this is a geometry type.
     * <p>
	 * get type:
	 * <li>getDbColumnGeomTypeName()</li>
	 * <p>
	 * @return true if datatype is a geometry, false if not.
	 */
	public boolean isGeometry() {
        // GPKG meldet "VARCHAR"=12 fuer Geometrie. Wird nicht mit in die Bedingung genommen.
		return attributeTypeName!=null && GEOMETRYTYPES.contains(attributeTypeName);
	}
	/** an attribute is mandatory if the column is defined as not null.
	 * @return true if attribute is mandatory, else false.
	 */
	public Boolean isMandatory() {
		return mandatory;
	}
	/** an attribute is mandatory if the column is defined as not null.
	 * @param mandatory true if attribute is mandatory, else false.
	 */
	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}
	/** is the documentation of this attribute.
	 * @return the columnRemarks
	 */
	public String getColumnRemarks() {
		return columnRemarks;
	}
	/** is the documentation of this attribute.
	 * @param columnRemarks the documentation to set
	 */
	public void setColumnRemarks(String columnRemarks) {
		this.columnRemarks = columnRemarks;
	}
	
	/** an attribute is a reference if the column references to a target class.
	 * @return true if attribute is a reference, else false.
	 */
	public boolean isReference() {
		if(getTargetTableName()!=null) {
			return true;
		}
		return false;
	}
	
	/** the target table of the reference.
	 * @return the target table name
	 */
	public String getTargetTableName() {
		return targetTableName;
	}

	/** the table of the target reference of the foreign key.
	 * @param targetTableName the referencedTableName to set
	 */
	public void setTargetTableName(String targetTableName) {
		this.targetTableName = targetTableName;
	}

	/** the name of the column with the reference to the target table.
	 * @return the reference column name to the target table.
	 */
	public String getReferenceColumnName() {
		return referenceColumnName;
	}

	/** the name of the column with the reference to the target table.
	 * @param referenceColumnName the reference column name to the target table.
	 */
	public void setReferenceColumnName(String referenceColumnName) {
		this.referenceColumnName = referenceColumnName;
	}
	/** the definition of the attribute.
	 * @return the attributeDefinition.
	 */
	public String getAttributeTypeDefinition() {
		return attributeDefinition;
	}
	/** the definition of the attribute.
	 * @param attributeDefinition the attributeDefinition to set.
	 */
	public void setAttributeTypeDefinition(String attributeDefinition) {
		this.attributeDefinition = attributeDefinition;
	}
}