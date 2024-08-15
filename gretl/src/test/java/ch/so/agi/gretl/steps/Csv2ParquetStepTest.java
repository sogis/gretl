package ch.so.agi.gretl.steps;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.testutil.TestUtil;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.interlis2.validator.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class Csv2ParquetStepTest {

    private static final Configuration testConf = new Configuration();

    @TempDir
    public Path folder;

    @Test
    public void encoding_iso_8859_1_Ok() throws Exception {
        // Prepare
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE, IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER);
        settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, null);            
        settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, ";");            
        settings.setValue(CsvReader.ENCODING, "ISO-8859-1");
        settings.setValue(Validator.SETTING_MODELNAMES, "SO_HBA_Gebaeude_20230111");

        File csvFile = TestUtil.getResourceFile(TestUtil.SAP_GEBAEUDE_CSV_PATH);
        
        // Run
        Csv2ParquetStep csv2parquetStep = new Csv2ParquetStep();
        csv2parquetStep.execute(csvFile.toPath(), folder, settings);

        // Validate
        org.apache.hadoop.fs.Path resultFile = new org.apache.hadoop.fs.Path(Paths
                .get(folder.toString(), FilenameUtils.getBaseName(csvFile.toPath().toString()) + ".parquet").toString());

        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(HadoopInputFile.fromPath(resultFile, testConf)).build()
        ) {
            GenericRecord arecord = reader.read();
            assertEquals("Fernwärme", arecord.get("Energietraeger").toString());
        }
    }
    
    @Test
    public void date_datatypes_handling_Ok() throws Exception {
        // Prepare
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE, IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER);
        settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, null);            
        settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, ";");            
        settings.setValue(Validator.SETTING_MODELNAMES, "Date_202306016");

        File csvFile = TestUtil.getResourceFile(TestUtil.DATE_DATATYPES_CSV_PATH);
        
        // Run
        Csv2ParquetStep csv2parquetStep = new Csv2ParquetStep();
        csv2parquetStep.execute(csvFile.toPath(), folder, settings);

        // Validate
        org.apache.hadoop.fs.Path resultFile = new org.apache.hadoop.fs.Path(Paths
                .get(folder.toString(), FilenameUtils.getBaseName(csvFile.toPath().toString()) + ".parquet").toString());

        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(HadoopInputFile.fromPath(resultFile, testConf)).build()
        ) {
            GenericRecord arecord = reader.read();
            assertEquals("1", arecord.get("id").toString());

            LocalDate resultDate = Instant.ofEpochSecond((int)arecord.get("aDate") * 24 * 60 * 60).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate expectedDate = LocalDate.parse("2023-06-24", DateTimeFormatter.ISO_LOCAL_DATE);
            assertEquals(expectedDate, resultDate);

            // FIXME Falls parquet-avro fix i.O. (auch atZone muss nach systemDefault geändert werden).
            LocalDateTime resultDateTime = Instant.ofEpochMilli((long)arecord.get("aDatetime")).atZone(ZoneOffset.UTC).toLocalDateTime();
            LocalDateTime expectedDateTime = LocalDateTime.parse("1977-09-16T04:46:12"); // Das "lokale" Datum wird nach UTC beim Schreiben umgewandelt. Ist kompliziert... Siehe Code.
            assertEquals(expectedDateTime, resultDateTime);

            LocalTime resultTime = Instant.ofEpochMilli(((Number)arecord.get("aTime")).longValue()).atZone(ZoneOffset.UTC).toLocalTime();
            LocalTime expectedTime = LocalTime.parse("15:51:41"); // Siehe datetime. Aber noch kein Bugfix vorhanden. Geht vielleicht gar nicht, weil AVRO nur Time _ohne_ Zone kennt.
            assertEquals(expectedTime, resultTime);

            GenericRecord nextRecord = reader.read();
            assertNull(nextRecord);
        }
    }
    
    @Test
    public void model_set_Ok() throws Exception {
        // Prepare
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE, IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER);
        settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, null);            
        settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, ";");            
        settings.setValue(Validator.SETTING_MODELNAMES, "SO_AFU_Bewilligte_Erdwaermeanlagen_20230616");

        File csvFile = TestUtil.getResourceFile(TestUtil.BEWILLIGTE_ERDWAERMEANLAGEN_CSV_PATH);
        
        // Run
        Csv2ParquetStep csv2parquetStep = new Csv2ParquetStep();
        csv2parquetStep.execute(csvFile.toPath(), folder, settings);

        // Validate
        org.apache.hadoop.fs.Path resultFile = new org.apache.hadoop.fs.Path(Paths
                .get(folder.toString(), FilenameUtils.getBaseName(csvFile.toPath().toString()) + ".parquet").toString());

        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(HadoopInputFile.fromPath(resultFile, testConf)).build()
        ) {
            int recordCount = 0;
            GenericRecord arecord = reader.read();
            GenericRecord firstRecord = null;
            GenericRecord lastRecord = null;
            while(arecord != null) {
                if (recordCount == 0) {
                    firstRecord = arecord;
                }
                recordCount++;

                lastRecord = arecord;
                arecord = reader.read();
            }

            assertEquals(31, recordCount);
            assertNotNull(firstRecord);
            assertEquals(1991, firstRecord.get("jahr"));
            assertNull(firstRecord.get("internet_clicks_durchschnitt_pro_monat"));
            assertEquals(2021, lastRecord.get("jahr"));
            assertEquals(999, lastRecord.get("internet_clicks_durchschnitt_pro_monat"));
        }
    }

    @Test
    public void no_model_set_Ok() throws Exception {
        // Prepare
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE, IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER);
        settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, null);            
        settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, ";");

        File csvFile = TestUtil.getResourceFile(TestUtil.BEWILLIGTE_ERDWAERMEANLAGEN_CSV_PATH);
        
        // Run
        Csv2ParquetStep csv2parquetStep = new Csv2ParquetStep();
        csv2parquetStep.execute(csvFile.toPath(), folder, settings);
        
        // Validate
        org.apache.hadoop.fs.Path resultFile = new org.apache.hadoop.fs.Path(Paths
                .get(folder.toString(), FilenameUtils.getBaseName(csvFile.toPath().toString()) + ".parquet").toString());

        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(HadoopInputFile.fromPath(resultFile, testConf)).build()
        ) {
            int recordCount = 0;
            GenericRecord arecord = reader.read();
            GenericRecord firstRecord = null;
            GenericRecord lastRecord = null;
            while(arecord != null) {
                if (recordCount == 0) {
                    firstRecord = arecord;
                }
                recordCount++;

                lastRecord = arecord;
                arecord = reader.read();
            }

            assertEquals(31, recordCount);
            assertNotNull(firstRecord);
            assertTrue(firstRecord.hasField("durchschnittlicher_oelpreis_pro_1000_liter"));
            assertTrue((firstRecord.get("durchschnittlicher_oelpreis_pro_1000_liter") == null));
            assertEquals("999", (lastRecord.get("internet_clicks_durchschnitt_pro_monat").toString()));
        }
    }

    @Test
    public void custom_config_and_no_model_set_Ok() throws Exception {
        // Prepare
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE, IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER);
        settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, "'");            
        settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, ";");            
        
        File csvFile = TestUtil.getResourceFile(TestUtil.BEWILLIGTE_ERDWAERMEANLAGEN_SEMIKOLON_HOCHKOMMA_CSV_PATH);
                
        // Run
        Csv2ParquetStep csv2parquetStep = new Csv2ParquetStep();
        csv2parquetStep.execute(csvFile.toPath(), folder, settings);

        // Validate
        org.apache.hadoop.fs.Path resultFile = new org.apache.hadoop.fs.Path(Paths
                .get(folder.toString(), FilenameUtils.getBaseName(csvFile.toPath().toString()) + ".parquet").toString());

        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(HadoopInputFile.fromPath(resultFile, testConf)).build()
        ) {
            int recordCount = 0;
            GenericRecord arecord = reader.read();
            GenericRecord firstRecord = null;
            GenericRecord lastRecord = null;
            while(arecord != null) {
                if (recordCount == 0) {
                    firstRecord = arecord;
                }
                recordCount++;

                lastRecord = arecord;
                arecord = reader.read();
            }

            assertEquals(31, recordCount);
            assertNotNull(firstRecord);
            assertTrue(firstRecord.hasField("durchschnittlicher_oelpreis_pro_1000_liter"));
            assertTrue((firstRecord.get("durchschnittlicher_oelpreis_pro_1000_liter") == null));
            assertEquals("999", (lastRecord.get("internet_clicks_durchschnitt_pro_monat").toString()));
        }
    }
}
