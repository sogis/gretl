package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.junit.Test;
import ch.so.agi.gretl.util.IntegrationTestUtil;


public class Csv2ParquetTest {
    private static final Configuration testConf = new Configuration();

    @Test
    public void convertCsv_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Csv2Parquet");

        IntegrationTestUtil.executeTestRunner(projectDirectory, "convertData");

        // Validate result
        org.apache.hadoop.fs.Path resultFile = new org.apache.hadoop.fs.Path(Paths
                .get(projectDirectory + "/20230124_sap_Gebaeude.parquet").toString());
        ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(HadoopInputFile.fromPath(resultFile, testConf)).build();

        GenericRecord arecord = reader.read();
        assertEquals("Fernwärme", arecord.get("Energietraeger").toString());
    }
}
