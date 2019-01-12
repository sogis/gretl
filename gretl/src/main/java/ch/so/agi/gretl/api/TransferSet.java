package ch.so.agi.gretl.api;

import ch.so.agi.gretl.steps.GeometryTransform;
import ch.so.agi.gretl.util.GretlException;

import java.io.File;
import java.util.HashMap;

public class TransferSet {

    private boolean deleteAllRows;
    private File inputSqlFile;
    private String outputQualifiedTableName;
    private HashMap<String, GeometryTransform> geoColumns;

    public TransferSet(String inputSqlFilePath, String outputQualifiedSchemaAndTableName, boolean outputDeleteAllRows,
            String[] geoColumns) {

        if (inputSqlFilePath == null || inputSqlFilePath.length() == 0)
            throw new IllegalArgumentException("inputSqlFilePath must not be null or empty");

        this.inputSqlFile = new File(inputSqlFilePath);

        if (outputQualifiedSchemaAndTableName == null || outputQualifiedSchemaAndTableName.length() == 0)
            throw new IllegalArgumentException("outputQualifiedTableName must not be null or empty");

        this.outputQualifiedTableName = outputQualifiedSchemaAndTableName;

        this.deleteAllRows = outputDeleteAllRows;

        initGeoColumnHash(geoColumns);
    }

    private void initGeoColumnHash(String[] colList) {
        geoColumns = new HashMap<String, GeometryTransform>();

        if (colList != null) {
            for (String colDef : colList) {
                if (colDef == null)
                    throw new GretlException("Geometry column definition array must not contain null values");

                GeometryTransform trans = GeometryTransform.createFromString(colDef);

                geoColumns.put(trans.getColNameUpperCase(), trans);
            }
        }
    }

    public TransferSet(String inputSqlFilePath, String outputQualifiedSchemaAndTableName, boolean outputDeleteAllRows) {
        this(inputSqlFilePath, outputQualifiedSchemaAndTableName, outputDeleteAllRows, null);
    }

    public boolean deleteAllRows() {
        return deleteAllRows;
    }

    public File getInputSqlFile() {
        return inputSqlFile;
    }

    public void setInputSqlFile(File inputSqlFile) {
        this.inputSqlFile = inputSqlFile;
    }

    public String getOutputQualifiedTableName() {
        return outputQualifiedTableName;
    }

    public boolean isGeoColumn(String colName) {
        return geoColumns.containsKey(colName.toUpperCase());
    }

    public String wrapWithGeoTransformFunction(String colName, String valuePlaceHolder) {
        String res = null;

        GeometryTransform trans = geoColumns.get(colName.toUpperCase());

        if (trans == null)
            throw new GretlException("Given colName was not defined / configured as geometry column");

        res = trans.wrapWithGeoTransformFunction(valuePlaceHolder);

        return res;
    }

    public String toString() {
        String colString = String.join(",", geoColumns.keySet());

        String res = String.format(
                "TransferSet( SqlSelectFile: %s, TargetTable: %s, DeleteTargetRows: %s, GeoColumns: %s)",
                inputSqlFile.getAbsolutePath(), outputQualifiedTableName, deleteAllRows, colString);

        return res;
    }
}
