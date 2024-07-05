package ch.so.agi.gretl.testutil;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.steps.SqlExecutorStep;
import ch.so.agi.gretl.util.FileStylingDefinition;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.Collections;
import java.util.Objects;

public class TestUtil {
    public static final String PG_CONNECTION_URI = System.getProperty("gretltest_dburi_pg");
    public static final String PG_DB_NAME = "gretl";
    public static final String PG_DDLUSR_USR = "ddluser";
    public static final String PG_DDLUSR_PWD = "ddluser";
    public static final String PG_DMLUSR_USR = "dmluser";
    public static final String PG_DMLUSR_PWD = "dmluser";
    public static final String PG_READERUSR_USR = "readeruser";
    public static final String PG_READERUSR_PWD = "readeruser";
    public static final String PG_INIT_SCRIPT_PATH = "data/sql/init_postgresql.sql";
    public static final String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    public static final String CREATE_TEST_DB_SQL_PATH = "data/sql/create_test_db.sql";
    public static final String CLEAR_TEST_DB_SQL_PATH = "data/sql/clear_test_db.sql";
    public static final String INSERT_COLORS_COPY_DATA_SQL_PATH = "data/sql/insert_colors_copy_data.sql";
    public static final String COLORS_INSERT_DELETE_SQL_PATH = "data/sql/colors_insert_delete.sql";
    public static final String COLORS_UPDATE_FARBNAME_SQL_PATH = "data/sql/colors_update_farbname.sql";
    public static final String POSTGIS_VERSION_SQL_PATH = "data/sql/postgisversion.sql";
    public static final String AGGLOPROGRAMME_GPKG_PATH = "data/gpkg2shp/aggloprogramme.gpkg";
    public static final String ADMINISTRATIVE_EINSTELLUNGEN_GPKG_PATH = "data/gpkg2dxf/ch.so.agi_av_gb_administrative_einteilungen_2020-08-20.gpkg";
    public static final String SAP_GEBAEUDE_CSV_PATH = "data/csv2parquet/kantonale_gebaeude/20230124_sap_Gebaeude.csv";
    public static final String BEWILLIGTE_ERDWAERMEANLAGEN_CSV_PATH = "data/csv2parquet/bewilligte_erdwaermeanlagen/bewilligte_erdwaermeanlagen_excel_export.csv";
    public static final String DATE_DATATYPES_CSV_PATH = "data/csv2parquet/data_datatypes/date_datatypes.csv";
    public static final String BEWILLIGTE_ERDWAERMEANLAGEN_SEMIKOLON_HOCHKOMMA_CSV_PATH = "data/csv2parquet/bewilligte_erdwaermeanlagen/bewilligte_erdwaermeanlagen_semikolon_hochkomma.csv";
    public static final String PLANREGISTER_XML_PATH = "data/gzip/planregister.xml";
    public static final String AGI_ORTHOFOTO_META_TOML_PATH = "data/metapublisher/agi_orthofoto_1993_meta_pub/meta.toml";
    public static final String AFU_ABBAUSTELLEN_META_TOML_PATH = "data/metapublisher/afu_abbaustellen_pub/meta.toml";
    public static final String AGI_DM01SO_META_TOML_PATH = "data/metapublisher/agi_dm01so_pub/meta-dm01_so.toml";
    public static final String KANTONALE_GEBAEUDE_TOML_PATH = "data/ogdmetapublisher/kantonale_gebaeude/ch.so.hba.kantonale_gebaeude.toml";
    public static final String RASTER_GEOTIFF_SQL_PATH = "data/postgisrasterprocessor/prepare_raster_geotiff.sql";
    public static final String RASTER_AAIGRID_SQL_PATH = "data/postgisrasterprocessor/prepare_raster_aaigrid.sql";
    public static final String TARGET_TIF_PATH = "data/postgisrasterprocessor/target.tif";
    public static final String TARGET_ASC_PATH = "data/postgisrasterprocessor/target.asc";
    public static final String S3_BUCKET_DIR_PATH = "data/s3bucket2bucket";

    public static File createTempFile(TemporaryFolder folder, String content, String fileName) throws IOException {
        File sqlFile = folder.newFile(fileName);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(sqlFile))) {
            bw.write(content);
            return sqlFile;
        }
    }

    /**
     * Retrieves a file from the resources directory.
     *
     * <p>This method attempts to locate the resource specified by the given path and return it as a {@link File}.
     * The resource path must be non-null and point to an existing resource in the classpath. If the resource cannot
     * be found, an {@link IllegalArgumentException} is thrown.</p>
     *
     * @param resourcePath the path to the resource file, relative to the classpath.
     * @return a {@link File} object representing the resource.
     * @throws NullPointerException if {@code resourcePath} is null.
     * @throws IllegalArgumentException if the resource cannot be found at the specified path.
     * @throws Exception if an error occurs while converting the resource URL to a URI.
     */
    public static File getResourceFile(String resourcePath) throws Exception {
        Objects.requireNonNull(resourcePath);
        URL resourceUrl = TestUtil.class.getClassLoader().getResource(resourcePath);

        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found " + resourcePath);
        }

        return new File(resourceUrl.toURI());
    }

    public static void createTestDb(Connector connector, String resourceFile) throws Exception {
        try (Connection connection = connector.connect()) {
            connection.setAutoCommit(true);
            File inputFile = getResourceFile(resourceFile);
            FileStylingDefinition.checkForUtf8(inputFile);
            execute(connector, inputFile);
        }
    }

    public static void clearTestDb(Connector connector) throws Exception {
        try (Connection connection = connector.connect()) {
            connection.setAutoCommit(true);
            File inputFile = getResourceFile(CLEAR_TEST_DB_SQL_PATH);
            FileStylingDefinition.checkForUtf8(inputFile);
            execute(connector, inputFile);
        }
    }

    public static void execute(Connector connector, File inputFile) throws Exception {
        SqlExecutorStep step = new SqlExecutorStep();
        step.execute(connector, Collections.singletonList(inputFile));
    }
}