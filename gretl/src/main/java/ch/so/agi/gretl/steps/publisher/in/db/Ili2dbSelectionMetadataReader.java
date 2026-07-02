package ch.so.agi.gretl.steps.publisher.in.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.ehi.ili2db.base.DbNames;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.fromili.TransferFromIli;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;
import ch.ehi.ili2pg.PgCustomStrategy;
import ch.interlis.ilirepository.IliFiles;

class Ili2dbSelectionMetadataReader implements SelectionMetadataReader {
    private final Connection conn;
    private final Config config;

    Ili2dbSelectionMetadataReader(Connection conn, String dbSchema) {
        this.conn = conn;
        this.config = createConfig(conn, dbSchema);
    }

    @Override
    public List<String> getDatasets() throws Exception {
        return Ili2db.getDatasets(conn, config);
    }

    @Override
    public List<String> getModels() throws Exception {
        IliFiles iliFiles = TransferFromIli.readIliFiles(conn, config.getDbschema(), new PgCustomStrategy(),
                config.isVer3_export());
        List<String> models = new ArrayList<String>();
        for (Iterator<ch.interlis.ili2c.modelscan.IliFile> fileIt = iliFiles.iteratorFile(); fileIt.hasNext();) {
            ch.interlis.ili2c.modelscan.IliFile iliFile = fileIt.next();
            for (Iterator<ch.interlis.ili2c.modelscan.IliModel> modelIt = iliFile.iteratorModel(); modelIt
                    .hasNext();) {
                models.add(modelIt.next().getName());
            }
        }
        return models;
    }

    @Override
    public List<String> getTopics() throws Exception {
        return readDistinctBasketColumn(DbNames.BASKETS_TAB_TOPIC_COL);
    }

    @Override
    public List<String> getBaskets() throws Exception {
        return readDistinctBasketColumn(DbNames.T_ILI_TID_COL);
    }

    private List<String> readDistinctBasketColumn(String columnName) throws Exception {
        List<String> values = new ArrayList<String>();
        String sql = "SELECT DISTINCT " + columnName + " FROM " + getBasketTableName() + " WHERE " + columnName
                + " IS NOT NULL ORDER BY " + columnName;
        try (PreparedStatement statement = conn.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        }
        return values;
    }

    private String getBasketTableName() {
        if (config.getDbschema() == null) {
            return DbNames.BASKETS_TAB;
        }
        return config.getDbschema() + "." + DbNames.BASKETS_TAB;
    }

    private Config createConfig(Connection conn, String dbSchema) {
        Config config = new Config();
        new PgMain().initConfig(config);
        config.setDbschema(dbSchema);
        config.setJdbcConnection(conn);
        return config;
    }
}
