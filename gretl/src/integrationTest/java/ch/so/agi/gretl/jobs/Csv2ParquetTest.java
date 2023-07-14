package ch.so.agi.gretl.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.junit.Test;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;


public class Csv2ParquetTest {
    private static final Configuration testConf = new Configuration();

    @Test
    public void convertCsv_Ok() throws Exception {        
        // Run GRETL task
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Csv2Parquet", gvs);
                
        // Validate result
        org.apache.hadoop.fs.Path resultFile = new org.apache.hadoop.fs.Path(Paths
                .get("src/integrationTest/jobs/Csv2Parquet/20230124_sap_Gebaeude.parquet").toString());
        ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(HadoopInputFile.fromPath(resultFile, testConf)).build();

        GenericRecord arecord = reader.read();
        assertEquals("Fernwärme", arecord.get("Energietraeger").toString());
    }
}
